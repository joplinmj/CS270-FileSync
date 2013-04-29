package edu.vanderbilt.cs283.filesync.client;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.vanderbilt.cs283.filesync.util.IOUtils;
import edu.vanderbilt.cs283.filesync.util.Log;

public class DirectoryMonitor implements Runnable {

	private static final long DELAY_TIME = ClientProperties.DIRECTORY_MONITOR_DELAY_TIME;
	private static final TimeUnit DELAY_TIME_UNIT = TimeUnit.MILLISECONDS;

	private final WatchService mWatcher;
	private final Path mDirectory;

	private volatile boolean mIsRunning = false;

	/**
	 * Once a file update event successfully executes (doesn't get canceled by a
	 * more recent event), it will be enqueued here for publication. The
	 * networking component will receive events from here. Newly enqueued events
	 * will also remove any events for the same file that are still on the
	 * queue. This farther reduces multiple events since events would be more
	 * rapidly enqueued than dequeued by the networking component.
	 */
	private final BlockingQueue<FileChangeRecord> mResultQueue = new PriorityBlockingQueue<FileChangeRecord>();

	/**
	 * Changes to files will be delayed via this executor. If new file changes
	 * come in to a file already on this executor, the previous changes will be
	 * canceled. This will prevent multiple update events for the same files.
	 */
	private final ScheduledExecutorService mDelayedExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime()
			.availableProcessors());

	/**
	 * A future for the currently delayed tasks will be mapped here so that they
	 * may be canceled when a new update comes in. This map is thread-confined
	 * so no need for synchronization.
	 */
	private final Map<Path, Future<?>> mDelayedFutures = new HashMap<Path, Future<?>>();

	public DirectoryMonitor(final Path directory) throws IOException {
		if (directory == null)
			throw new NullPointerException();
		final File dirFile = directory.toFile();
		if (!dirFile.exists())
			throw new IllegalArgumentException(directory + " does not exist");
		if (!dirFile.isDirectory())
			throw new IllegalArgumentException(directory + " is not a directory");

		mDirectory = directory;
		mWatcher = FileSystems.getDefault().newWatchService();

		mDirectory.register(mWatcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
	}

	public BlockingQueue<FileChangeRecord> getResultBlockingQueue() {
		return mResultQueue;
	}

	/**
	 * Inner class. Not static so that we can access the blocking result queue.
	 */
	private class DelayEvent implements Callable<Void> {

		private final Path mPath;
		private final long time;
		private final boolean isDelete;

		public DelayEvent(final Path path, final boolean delete) {
			mPath = path;
			time = System.currentTimeMillis();
			isDelete = delete;
		}

		@Override
		public Void call() throws Exception {

			boolean keepCopyFile = false;
			File copyFile = null;
			try {
				Log.format("Executing %s", mPath);
				final File sourceFile = mPath.toFile();

				if (!isDelete) {
					copyFile = IOUtils.makeTemporaryHiddenFile(sourceFile.getName(), mDirectory.toFile());
					IOUtils.copyFile(sourceFile, copyFile);
				}

				if (Thread.currentThread().isInterrupted()) {
					Log.format("Canceling %s due to interrupt", mPath);
					return null;
				}
				// Log.log("Before make record");
				final FileChangeRecord record = new FileChangeRecord(sourceFile.getName(), time, copyFile);
				// Log.log("After make record");

				if (Thread.currentThread().isInterrupted()) {
					Log.format("Canceling %s due to interrupt", mPath);
					return null;
				}

				// Log.log("Before remove()");
				if (mResultQueue.remove(record)) {
					// Log.format("Removed %s from result queue", mPath);
				} else {
					// Log.format("Didn't remove %s from result queue", mPath);
				}

				Log.format("Adding %s to result queue for %s.", mPath, isDelete ? "DELETION" : "UPDATE");
				mResultQueue.put(record);
				keepCopyFile = true;
				return null;
			} finally {
				if (!keepCopyFile && copyFile != null)
					copyFile.delete();
				Log.format("Done with %s", mPath);
			}
		}
	}

	private void processEvent(final WatchEvent<Path> event) {
		final Path path = mDirectory.resolve(event.context());
		fireEvent(path, event.kind() == ENTRY_DELETE);
	}

	private void fireEvent(final Path path, final boolean shouldDelete) {
		if (mIsRunning && !Thread.holdsLock(this))
			throw new IllegalStateException("run() already invoked and this thread doesn't hold the lock.");

		synchronized (this) {
			if (IOUtils.isHiddenFile(path.toFile()))
				return;

			final Future<?> current = mDelayedFutures.get(path);
			if (current != null && !current.isDone()) {
				if (current.cancel(true))
					Log.format("Successfully canceled %s", path);
				else
					Log.format("Attempted to cancel %s", path);
			}

			final Future<?> newFuture = mDelayedExecutor.schedule(new DelayEvent(path, shouldDelete), DELAY_TIME,
					DELAY_TIME_UNIT);
			mDelayedFutures.put(path, newFuture);
		}
	}

	public void fireDeleteEvent(final Path path) {
		fireEvent(path, true);
	}

	public void fireUpdateEvent(final Path path) {
		fireEvent(path, false);
	}

	@Override
	public synchronized void run() {
		mIsRunning = true;
		try {
			while (runLoop())
				;
		} finally {
			mIsRunning = false;
			mDelayedExecutor.shutdown();
		}
	}

	private boolean runLoop() {
		final WatchKey key;
		try {
			key = mWatcher.take();
		} catch (InterruptedException e) {
			return false;
		}

		for (final WatchEvent<?> e : key.pollEvents()) {
			logEvent(e);
			if (e.kind() == OVERFLOW)
				continue;

			processEvent(castEvent(e));
		}

		return key.reset();
	}

	@SuppressWarnings("unchecked")
	private WatchEvent<Path> castEvent(final WatchEvent<?> e) {
		return (WatchEvent<Path>) e;
	}

	private void logEvent(final WatchEvent<?> e) {
		final WatchEvent.Kind<?> kind = e.kind();
		if (kind == OVERFLOW) {
			Log.format("%s x%d", kind.name(), e.count());
		} else {
			final WatchEvent<Path> event = castEvent(e);
			if (IOUtils.isHiddenFile(event.context()))
				return;
			final Path file = mDirectory.resolve(event.context());
			Log.format("%s x%d: %s", event.kind().name(), event.count(), file);
		}
	}
}
