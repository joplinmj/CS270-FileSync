package edu.vanderbilt.cs283.filesync.server.worker;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import edu.vanderbilt.cs283.filesync.common.Command;
import edu.vanderbilt.cs283.filesync.common.CommandHolder;
import edu.vanderbilt.cs283.filesync.common.FileIndexElement;
import edu.vanderbilt.cs283.filesync.util.Log;

public final class ClientHandler implements Runnable, Closeable {

	private final ServerCallback mCallback;
	private final Map<String, ServerIndexElement> mHandshakeIndex;

	private final Socket mConnection;
	private final ObjectOutputStream mOut;
	private final ObjectInputStream mIn;
	private final String mId;

	private final BlockingQueue<CommandHolder> mOutgoingCommandQueue = new LinkedBlockingQueue<CommandHolder>();

	public ClientHandler(final Socket connection, final ServerCallback callback,
			final Map<String, ServerIndexElement> handshakeIndex) throws IOException, ClassNotFoundException {

		mCallback = callback;
		mHandshakeIndex = handshakeIndex;

		mConnection = connection;
		mOut = new ObjectOutputStream(connection.getOutputStream());
		mIn = new ObjectInputStream(connection.getInputStream());

		final String user = (String) mIn.readObject();
		final String thisUrl = (String) mIn.readObject();
		mCallback.registerMeta(user, thisUrl);

		final String localDir = (String) mIn.readObject();
		mId = sanitizeId(mConnection.getInetAddress().getHostAddress() + "@" + localDir);

		System.out.println("Connected " + mId);
	}

	public BlockingQueue<CommandHolder> getOutgoingCommandQueue() {
		return mOutgoingCommandQueue;
	}

	public String getIdentifier() {
		return mId;
	}

	@Override
	public void close() throws IOException {
		mOut.close();
		mIn.close();
		mConnection.close();
	}

	private void handshake() throws IOException, ClassNotFoundException {
		@SuppressWarnings("unchecked")
		final Map<String, FileIndexElement> clientIndex = (Map<String, FileIndexElement>) mIn.readObject();
		Log.log(getIdentifier() + "->" + clientIndex);

		{
			final Map<String, FileIndexElement> handshakeIndex = new HashMap<String, FileIndexElement>();
			for (final Entry<String, ServerIndexElement> entry : mHandshakeIndex.entrySet()) {
				handshakeIndex.put(entry.getKey(), entry.getValue().getFileIndexElement());
			}
			mOut.writeObject(handshakeIndex);
		}

		final List<CommandHolder> commands = new ArrayList<CommandHolder>();
		// Go through the files the client has and see if we need to delete or
		// update them.
		for (final String localFile : clientIndex.keySet()) {
			// If server doesn't have this file in the index at all then ignore
			if (!mHandshakeIndex.containsKey(localFile))
				continue;

			final FileIndexElement clientElement = clientIndex.get(localFile);
			final boolean isDelete = mHandshakeIndex.get(localFile).isDelete();
			final FileIndexElement serverElement = mHandshakeIndex.get(localFile).getFileIndexElement();

			if (!isDelete) {
				// If the server has the file
				if (clientElement.getFileHash().equals(serverElement.getFileHash())) {
					// and the files are the same, ignore
					continue;
				} else {
					// If the files are different then send an update only if
					// the server's newer.
					if (serverElement.getLastModifiedDate() > clientElement.getLastModifiedDate()) {
						commands.add(new CommandHolder(Command.update(serverElement), mHandshakeIndex.get(localFile)
								.getServerCopy()));
						continue;
					}

					// If the client's version is newer than he will send us an
					// update after the
					// handshake.
				}
			} else {
				// If the server has the file deleted
				// then delete it from the client if the client has an older or
				// same version
				if (serverElement.getLastModifiedDate() >= clientElement.getLastModifiedDate()) {
					commands.add(new CommandHolder(Command.delete(clientElement), null));
					continue;
				}
			}
		}

		// Go through the files the client doesn't have and send them to him
		final Set<String> missingFiles = new HashSet<String>(mHandshakeIndex.keySet());
		missingFiles.removeAll(clientIndex.keySet());
		for (final String localMissingFile : missingFiles) {
			if (mHandshakeIndex.get(localMissingFile).isDelete())
				continue;
			commands.add(new CommandHolder(Command.update(mHandshakeIndex.get(localMissingFile).getFileIndexElement()),
					mHandshakeIndex.get(localMissingFile).getServerCopy()));
		}

		mOut.writeInt(commands.size());
		for (final CommandHolder c : commands) {
			c.command.send(mOut, c.source);
		}
		mOut.flush();
	}

	@Override
	public void run() {
		try {
			handshake();

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						while (true) {
							final CommandHolder holder = mOutgoingCommandQueue.take();
							holder.command.send(mOut, holder.source);
						}
					} catch (IOException | InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}).start();

			while (true) {
				final Command command = (Command) mIn.readObject();
				Log.log(getIdentifier() + "->" + command);

				final FileIndexElement element = command.getFileIndexElement();

				if (command.isDelete()) {
					mCallback.onDelete(getIdentifier(), element);
				} else {
					final File dest = mCallback.makeFile(getIdentifier(), element);
					command.read(mIn, dest);
					mCallback.onUpdate(getIdentifier(), element, dest);
				}
			}

		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			mCallback.unregisterHandler(this);
		}
	}

	private static String sanitizeId(final String id) {
		return id.replace('.', '_').replace(':', '_').replace('\\', '_');
	}
}
