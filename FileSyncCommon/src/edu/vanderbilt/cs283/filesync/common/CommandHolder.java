package edu.vanderbilt.cs283.filesync.common;

import java.io.File;

public final class CommandHolder {

	public final Command command;
	public final File source;

	public CommandHolder(final Command cmd, final File src) {
		command = cmd;
		source = src;
	}
}
