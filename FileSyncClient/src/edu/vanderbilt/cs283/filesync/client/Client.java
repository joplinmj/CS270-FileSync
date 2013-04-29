package edu.vanderbilt.cs283.filesync.client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import edu.vanderbilt.cs283.filesync.client.FileChangeRecord.FileChangeType;
import edu.vanderbilt.cs283.filesync.common.Command;
import edu.vanderbilt.cs283.filesync.common.FileIndexElement;
import edu.vanderbilt.cs283.filesync.util.IOUtils;
import edu.vanderbilt.cs283.filesync.util.Log;

public class Client implements Runnable {

	public static void main(final String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("usage: " + Client.class.getCanonicalName() + " <username> <directory>");
			return;
		}

		final String username = args[0];
		final String directory = args[1];

		new Client(username, directory).run();
	}

	private final String mUsername;
	private final Path mDirectory;
	private final DirectoryMonitor mMonitor;

	private ObjectOutputStream mOut;
	private ObjectInputStream mIn;
	private final ConcurrentMap<String, FileIndexElement> mServerIndex = new ConcurrentHashMap<String, FileIndexElement>();

	public Client(final String user, final String dir) throws IOException {
		mUsername = user;
		mDirectory = Paths.get(dir);
		mMonitor = new DirectoryMonitor(mDirectory);
	}

	@SuppressWarnings("unchecked")
	private void handshake() throws IOException, ClassNotFoundException {
		mOut.writeObject(mDirectory.toString());
		// Send the server our local index and fire fake events for all files we
		// have. This will prime the blocking queue with files that the server
		// will need. Most of the events will be dismissed once we start the
		// reactor loop.
		final Map<String, FileIndexElement> localIndex = createFileIndexAndFireLocalEvents();
		mOut.writeObject(localIndex);
		mOut.flush();

		mServerIndex.putAll((Map<String, FileIndexElement>) mIn.readObject());

		// The server will reply with some commands that we must execute in
		// order to be half-synced (we will have all the files it has and will
		// have deleted the files that we shouldn't have but he won't have all
		// of our new files).
		final int commandCount = mIn.readInt();
		for (int i = 0; i < commandCount; ++i) {
			processServerCommand((Command) mIn.readObject());
		}
	}

	private void processServerCommand(final Command command) throws IOException {
		final FileIndexElement element = command.getFileIndexElement();
		final File localDest = mDirectory.resolve(element.getFilename()).toFile();
		// Update the server index
		if (command.isDelete()) {
			mServerIndex.remove(element.getFilename());
			localDest.delete();
			return;
		}

		// This is an update, download the file
		mServerIndex.put(element.getFilename(), element);
		command.read(mIn, localDest);
	}

	@Override
	public void run() {
		// Figure out the redirect
		String workerHost = null;

		try {
			Log.log("Looking up a redirect...");
			final Socket socket = new Socket(ClientProperties.SERVER_HEAD_HOST, ClientProperties.SERVER_HEAD_PORT);
			final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
			final ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
			out.writeObject(mUsername);
			out.flush();
			workerHost = (String) in.readObject();
			out.close();
			in.close();
			socket.close();

			Log.format("Redirecting to %s...", workerHost);
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		try {
			final Socket connection = new Socket(workerHost, ClientProperties.SERVER_WORKER_PORT);
			mOut = new ObjectOutputStream(connection.getOutputStream());
			mIn = new ObjectInputStream(connection.getInputStream());

			mOut.writeObject(mUsername);
			mOut.writeObject(workerHost);
			mOut.flush();
			Log.format("Sent identifier <%s,  %s>", mUsername, workerHost);

			handshake();
			Executors.newSingleThreadExecutor().execute(mMonitor);

			// Now we're ready to start reacting to the file system events and
			// listening to the server actively
			Executors.newSingleThreadExecutor().execute(mServerHandler);
			mFileEventHandler.run();

			// This won't execute since the above run() is an event loop.
			connection.close();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, FileIndexElement> createFileIndexAndFireLocalEvents() throws IOException {
		final Map<String, FileIndexElement> index = new HashMap<String, FileIndexElement>();

		for (final File file : mDirectory.toFile().listFiles()) {
			if (IOUtils.isHiddenFile(file) || !file.isFile())
				continue;

			final FileIndexElement element = new FileIndexElement(file);
			index.put(element.getFilename(), element);
			mMonitor.fireUpdateEvent(file.toPath());
		}

		return index;
	}

	private final Runnable mServerHandler = new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					processServerCommand((Command) mIn.readObject());
				} catch (ClassNotFoundException | IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	};

	private final Runnable mFileEventHandler = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					final FileChangeRecord record = mMonitor.getResultBlockingQueue().take();
					// If this is a delete event and the server doesn't know
					// about this file, ignore it
					if (record.changeType == FileChangeType.DELETED && !mServerIndex.containsKey(record.filename))
						continue;

					final FileIndexElement element = record.toFileIndexElement();
					final Command command = record.changeType == FileChangeType.DELETED ? Command.delete(element)
							: Command.update(element);

					// If the server knew about this file and it's a delete then
					// send a delete command
					if (mServerIndex.containsKey(record.filename) && command.isDelete()) {
						command.send(mOut, null);
						continue;
					}

					// If this is an update and the hash is different than the
					// server's or the server doesn't know about it then send it
					// TODO - This may be wrong.
					final FileIndexElement serversVersion = mServerIndex.get(record.filename);
					if (serversVersion == null
							|| (serversVersion.getLastModifiedDate() < record.timestamp && !serversVersion
									.getFileHash().equals(record.hash))) {

						command.send(mOut, record.temporaryCopy);
					}

					record.temporaryCopy.delete();
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	};
}
