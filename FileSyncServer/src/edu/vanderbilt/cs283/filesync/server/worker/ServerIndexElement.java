package edu.vanderbilt.cs283.filesync.server.worker;

import java.io.File;
import java.util.Arrays;

import edu.vanderbilt.cs283.filesync.common.FileIndexElement;

public final class ServerIndexElement {

	private final FileIndexElement mElement;
	private final boolean mIsDelete;
	private final File mServerCopy;
	private final String mClientId;

	public ServerIndexElement(final String clientId, final FileIndexElement e, final boolean isDelete,
			final File serverCopy) {
		mElement = e;
		mIsDelete = isDelete;
		mServerCopy = serverCopy;
		mClientId = clientId;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ServerIndexElement))
			return false;

		final ServerIndexElement o = (ServerIndexElement) obj;
		return mElement.equals(o.mElement) && mIsDelete == o.mIsDelete && mClientId.equals(o.mClientId);
	}

	@Override
	public int hashCode() {
		// I'm lazy
		return Arrays.hashCode(new Object[] { mElement, mIsDelete, mClientId });
	}
	
	public boolean isDelete() {
		return mIsDelete;
	}
	
	public File getServerCopy() {
		return mServerCopy;
	}
	
	public FileIndexElement getFileIndexElement() {
		return mElement;
	}

	public String getFilename() {
		return mElement.getFilename();
	}

	public String getFileHash() {
		return mElement.getFileHash();
	}

	public long getFileLength() {
		return mElement.getFileLength();
	}

	public long getLastModifiedDate() {
		return mElement.getLastModifiedDate();
	}

	@Override
	public String toString() {
		// TODO;
		return super.toString();
	}
}
