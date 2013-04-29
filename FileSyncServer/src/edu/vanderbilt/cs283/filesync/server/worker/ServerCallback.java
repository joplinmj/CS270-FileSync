package edu.vanderbilt.cs283.filesync.server.worker;

import java.io.File;
import java.io.IOException;

import edu.vanderbilt.cs283.filesync.common.FileIndexElement;

public interface ServerCallback {

	public File makeFile(String handlerId, FileIndexElement element) throws IOException;

	public void onDelete(String handlerId, FileIndexElement element);

	public void onUpdate(String handlerId, FileIndexElement element, File copy);

	public void unregisterHandler(ClientHandler handler);

	public void registerMeta(String user, String thisUrl);

}
