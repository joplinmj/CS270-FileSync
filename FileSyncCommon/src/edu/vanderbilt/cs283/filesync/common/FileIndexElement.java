package edu.vanderbilt.cs283.filesync.common;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import edu.vanderbilt.cs283.filesync.util.IOUtils;

public final class FileIndexElement implements Serializable {

	private static final long serialVersionUID = 6261180333712220585L;

	private final String mFilename;
	private final String mHash;
	private final long mLength;
	private final long mTimestamp;

	public FileIndexElement(final File file) throws IOException {
		mFilename = file.getName();
		mHash = IOUtils.computeHash(file);
		mLength = file.length();
		mTimestamp = file.lastModified();
	}

	public FileIndexElement(final String filename, final String hash, final long length, final long timestamp) {
		mFilename = filename;
		mHash = hash;
		mLength = length;
		mTimestamp = timestamp;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof FileIndexElement))
			return false;

		final FileIndexElement o = (FileIndexElement) obj;

		return mFilename.equals(o.mFilename) && mHash.equals(o.mHash) && mLength == o.mLength
				&& mTimestamp == o.mTimestamp;
	}

	@Override
	public int hashCode() {
		// I'm lazy
		return Arrays.hashCode(new Object[] { mFilename, mHash, mLength, mTimestamp });
	}

	public String getFilename() {
		return mFilename;
	}

	public String getFileHash() {
		return mHash;
	}

	public long getFileLength() {
		return mLength;
	}

	public long getLastModifiedDate() {
		return mTimestamp;
	}

	@Override
	public String toString() {
		return "FileIndexElement{" + mFilename + ", " + mLength + " bytes, " + mHash + "}";
	}
}
