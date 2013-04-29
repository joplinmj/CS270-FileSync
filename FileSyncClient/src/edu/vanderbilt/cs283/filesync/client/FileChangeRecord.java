package edu.vanderbilt.cs283.filesync.client;

import java.io.File;
import java.io.IOException;

import edu.vanderbilt.cs283.filesync.common.FileIndexElement;
import edu.vanderbilt.cs283.filesync.util.IOUtils;

/**
 * Note: the equal and hashCode methods for this class only look at the filename
 * member. This is done so that we may use a newer FileChangeRecord object to
 * remove an older one with the same filename from a Collection.
 * 
 * Note: the compareTo method compares by timestamps only. This is used so that
 * older objects are prioritized over newer objects.
 * 
 */
public final class FileChangeRecord implements Comparable<FileChangeRecord> {

	public static enum FileChangeType {
		UPDATED, DELETED
	}

	public final String filename;
	public final FileChangeType changeType;
	public final long timestamp;
	public final File temporaryCopy;
	public final String hash;

	public FileChangeRecord(final String name, final long time, final File copy) throws IOException {
		filename = name;
		timestamp = time;
		temporaryCopy = copy;
		changeType = temporaryCopy == null ? FileChangeType.DELETED : FileChangeType.UPDATED;
		hash = temporaryCopy == null ? null : IOUtils.computeHash(temporaryCopy);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof FileChangeRecord)
			return ((FileChangeRecord) obj).filename.equals(filename);
		return false;
	}

	@Override
	public int hashCode() {
		return filename.hashCode();
	}

	@Override
	public int compareTo(final FileChangeRecord o) {
		return Long.compare(timestamp, o.timestamp);
	}

	public FileIndexElement toFileIndexElement() {
		return new FileIndexElement(filename, hash, temporaryCopy == null ? -1 : temporaryCopy.length(), timestamp);
	}
}
