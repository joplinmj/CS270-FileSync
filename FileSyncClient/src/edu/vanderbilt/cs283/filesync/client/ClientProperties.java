package edu.vanderbilt.cs283.filesync.client;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public final class ClientProperties {

	private static final String PROP_URL = "https://dl.dropboxusercontent.com/s/p60sv0vbdfy5otx/client.properties?token_hash=AAFEr9XK5XAyfkxsRQb6W6ZZcg_w7-sGMcyg-Rc1HBgpyw&dl=1";
	
	public static final String SERVER_HEAD_HOST;
	public static final int SERVER_HEAD_PORT;
	public static final int SERVER_WORKER_PORT;

	public static final long DIRECTORY_MONITOR_DELAY_TIME;

	static {
		try {
			final Properties p = new Properties();
			p.load(new URL(PROP_URL).openStream());

			SERVER_HEAD_HOST = p.getProperty("SERVER_HEAD_HOST");
			SERVER_HEAD_PORT = Integer.parseInt(p.getProperty("SERVER_HEAD_PORT"));
			SERVER_WORKER_PORT = Integer.parseInt(p.getProperty("SERVER_WORKER_PORT"));

			DIRECTORY_MONITOR_DELAY_TIME = Long.parseLong(p.getProperty("DIRECTORY_MONITOR_DELAY_TIME"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ClientProperties() {
	}
}
