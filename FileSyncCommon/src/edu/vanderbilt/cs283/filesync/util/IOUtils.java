package edu.vanderbilt.cs283.filesync.util;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class IOUtils {

	private static final String HIDDEN_PREFIX = ".DEBUG_HIDDEN.";

	private IOUtils() {
	}

	public static void closeSilently(final Closeable c) {
		try {
			if (c != null)
				c.close();
		} catch (IOException ignored) {
		}
	}

	public static String computeHash(final File file) throws IOException {
		final FileInputStream in = new FileInputStream(file);
		try {
			final MessageDigest digest = MessageDigest.getInstance("MD5");
			final byte[] buffer = new byte[1024];
			int read = -1;

			while ((read = in.read(buffer)) != -1)
				digest.update(buffer, 0, read);

			final byte[] hash = digest.digest();
			final StringBuilder builder = new StringBuilder(hash.length * 2);

			for (final byte b : hash) {
				final String hex = Integer.toHexString(0xff & b);
				if (hex.length() == 1)
					builder.append("0");
				builder.append(hex);
			}

			if (builder.length() != hash.length * 2)
				throw new AssertionError(builder.length() + " vs " + hash.length * 2 + " hex digits");

			return builder.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError("MD5 not available", e);
		} finally {
			closeSilently(in);
		}
	}

	public static void copy(final InputStream in, final OutputStream out) throws IOException {
		final byte[] buffer = new byte[1024];
		int read = -1;

		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}

		out.flush();
	}

	public static void copy(final InputStream in, final OutputStream out, long count) throws IOException {
		final byte[] buffer = new byte[1024];
		int read = -1;

		while (count != 0) {
			read = in.read(buffer, 0, Math.min((int) count, buffer.length));
			if (read == -1)
				throw new EOFException();

			out.write(buffer, 0, read);
			count -= read;
		}

		out.flush();
	}

	public static void copyFile(final File sourceFile, final File destinationFile) throws IOException {
		FileInputStream in = null;
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(destinationFile);
			in = new FileInputStream(sourceFile);
			copy(in, out);
		} finally {
			closeSilently(in);
			closeSilently(out);
		}
	}

	public static boolean isHiddenFile(final Path path) {
		return isHiddenFile(path.toFile());
	}

	public static boolean isHiddenFile(final File file) {
		return file.getName().startsWith(HIDDEN_PREFIX);
	}

	public static File makeTemporaryHiddenFile(final File directory) throws IOException {
		return makeTemporaryHiddenFile(null, directory);
	}

	public static File makeTemporaryHiddenFile(final String customPrefix, final File directory) throws IOException {
		final String prefix = customPrefix == null ? HIDDEN_PREFIX : HIDDEN_PREFIX + customPrefix + ".";
		final File temp = File.createTempFile(prefix, null, directory);
		temp.deleteOnExit();
		return temp;
	}
}
