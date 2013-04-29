package edu.vanderbilt.cs283.filesync.server.worker;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import edu.vanderbilt.cs283.filesync.common.Command;
import edu.vanderbilt.cs283.filesync.common.CommandHolder;
import edu.vanderbilt.cs283.filesync.common.FileIndexElement;
import edu.vanderbilt.cs283.filesync.util.Log;

public class Server implements Runnable, ServerCallback {

	public static void main(final String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("usage: java " + Server.class.getCanonicalName() + " <port> <directory>");
			return;
			// args = new String[] { "5000", "D:\\FileSync\\server" };
		}

		new Server(Integer.parseInt(args[0]), new File(args[1])).run();
	}

	private final ServerSocket mServerSocket;
	private final Map<String, ClientHandler> mHandlerMap = Collections
			.synchronizedMap(new HashMap<String, ClientHandler>());
	private final ExecutorService mHandlerExecutor = Executors.newCachedThreadPool();

	private final Map<String, ServerIndexElement> mIndex = Collections
			.synchronizedMap(new HashMap<String, ServerIndexElement>());

	private final File mDirectory;
	private final AtomicReference<String> mUser = new AtomicReference<String>();
	private final AtomicReference<String> mThisUrl = new AtomicReference<String>();

	private final static Executor mJoplinsExecutor = Executors.newSingleThreadExecutor();

	public Server(final int port, final File directory) throws IOException {
		if (!directory.isDirectory())
			throw new IllegalArgumentException();

		mDirectory = directory;
		mServerSocket = new ServerSocket(port);
	}

	@Override
	public void run() {
		try {
			while (true) {
				final Socket connection = mServerSocket.accept();
				// Note that this constructor blocks on a bit of IO. It may be
				// wise to move it off the acceptor thread
				// but that would complicate handler registration so we'll do it
				// here for now.
				synchronized (mIndex) {
					final ClientHandler handler = new ClientHandler(connection, this,
							new HashMap<String, ServerIndexElement>(mIndex));
					if (mHandlerMap.put(handler.getIdentifier(), handler) != null)
						throw new IllegalStateException("Handler already exists: " + handler.getIdentifier());

					mHandlerExecutor.execute(handler);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public File makeFile(final String handlerId, final FileIndexElement element) throws IOException {
		return File.createTempFile(element.getFilename() + "." + element.getFileHash(), null, mDirectory);
	}

	@Override
	public void onDelete(final String handlerId, final FileIndexElement element) {
		try {
			synchronized (mIndex) {
				final ServerIndexElement serverElement = mIndex.get(element.getFilename());
				if (serverElement != null && !serverElement.isDelete()) {
					mIndex.put(element.getFilename(), new ServerIndexElement(handlerId, element, true, null));

					mJoplinsExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								JoplinsUpdate.onUpdate(false, mUser.get(), handlerId, element.getLastModifiedDate(),
										element.getFilename(), null, null);
							} catch (RuntimeException e) {
								e.printStackTrace();
								throw e;
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
					});

				}

				final Command outgoing = Command.delete(element);
				for (final Entry<String, ClientHandler> entry : mHandlerMap.entrySet()) {
					if (entry.getKey().equals(handlerId))
						continue;

					entry.getValue().getOutgoingCommandQueue().put(new CommandHolder(outgoing, null));
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpdate(final String handlerId, final FileIndexElement element, final File copy) {
		try {
			synchronized (mIndex) {
				final ServerIndexElement serverElement = mIndex.get(element.getFilename());
				if (serverElement == null || !serverElement.getFileHash().equals(element.getFileHash())) {
					mIndex.put(element.getFilename(), new ServerIndexElement(handlerId, element, false, copy));

					mJoplinsExecutor.execute(new Runnable() {
						@Override
						public void run() {
							try {
								JoplinsUpdate.onUpdate(false, mUser.get(), handlerId, element.getLastModifiedDate(),
										element.getFilename(), element.getFileHash(),
										mThisUrl.get() + "/" + copy.getName());
							} catch (RuntimeException e) {
								e.printStackTrace();
								throw e;
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
					});
				}

				final Command outgoing = Command.update(element);
				for (final Entry<String, ClientHandler> entry : mHandlerMap.entrySet()) {
					if (entry.getKey().equals(handlerId))
						continue;

					entry.getValue().getOutgoingCommandQueue().put(new CommandHolder(outgoing, copy));
				}
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void unregisterHandler(final ClientHandler handler) {
		mHandlerMap.remove(handler.getIdentifier());
		try {
			handler.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void registerMeta(String user, String thisUrl) {
		Log.format("registerMeta(%s, %s)", user, thisUrl);
		mUser.set(user);
		mThisUrl.set(thisUrl);
	}
}
