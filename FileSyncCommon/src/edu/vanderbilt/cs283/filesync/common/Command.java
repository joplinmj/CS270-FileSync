package edu.vanderbilt.cs283.filesync.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import edu.vanderbilt.cs283.filesync.util.IOUtils;
import edu.vanderbilt.cs283.filesync.util.Log;

public final class Command implements Serializable {

	private static final long serialVersionUID = 1160380682777295543L;

	public static Command delete(final FileIndexElement element) {
		return new Command(element, true);
	}

	public static Command update(final FileIndexElement element) {
		return new Command(element, false);
	}

	private final FileIndexElement mFileInfo;
	private final boolean mIsDelete;

	private Command(final FileIndexElement element, final boolean isDelete) {
		mFileInfo = element;
		mIsDelete = isDelete;
	}

	@Override
	public boolean equals(final Object obj) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int hashCode() {
		throw new UnsupportedOperationException("Command is not hashable");
	}

	public FileIndexElement getFileIndexElement() {
		return mFileInfo;
	}

	public boolean isDelete() {
		return mIsDelete;
	}

	public void send(final ObjectOutputStream out, final File src) throws IOException {
		out.writeObject(this);
		if (!isDelete()) {
			out.writeLong(mFileInfo.getFileLength());
			final FileInputStream in = new FileInputStream(src);
			IOUtils.copy(in, out);
			in.close();
		}

		out.flush();
	}

	public void read(final ObjectInputStream in, final File dest) throws IOException {
		if (isDelete())
			throw new IllegalStateException();

		Log.log("Command.read() about to read a file of length: " + getFileIndexElement().getFileLength());
		final long length = in.readLong();
		final FileOutputStream out = new FileOutputStream(dest);
		IOUtils.copy(in, out, length);
		out.close();
	}

	@Override
	public String toString() {
		return "Command{" + (isDelete() ? "DELETE" : "UPDATE") + " " + mFileInfo + "}";
	}
}
