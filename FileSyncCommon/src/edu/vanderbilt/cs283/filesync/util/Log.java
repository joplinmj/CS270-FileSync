package edu.vanderbilt.cs283.filesync.util;

public class Log {

	private Log() {
	}

	public static synchronized void log(final String msg) {
		System.out.print(Thread.currentThread().getName());
		System.out.print(" > ");
		System.out.println(msg);
	}

	public static synchronized void format(final String format, final Object... args) {
		log(String.format(format, args));
	}

}
