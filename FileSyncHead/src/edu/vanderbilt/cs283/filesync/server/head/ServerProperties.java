package edu.vanderbilt.cs283.filesync.server.head;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ServerProperties {

	private static final String PROP_URL = "https://dl.dropboxusercontent.com/s/186so6ky7m64avu/server.properties?token_hash=AAFqJekVfEhRxNWNT9bFWhBxZcJvIjKyEU01QseC5bacEw&dl=1";

	public static final String AWS_ACCESS_KEY;
	public static final String AWS_SECRET_KEY;
	public static final String AWS_ENDPOINT;

	public static final String AWS_RUN_INSTANCE_TYPE;
	public static final String AWS_RUN_IMAGE_ID;
	public static final String AWS_RUN_SECURITY_GROUP_ID;
	public static final String AWS_RUN_KEY_NAME;

	public static final String LOCAL_SCRIPT_URL;
	public static final String SETUP_SCRIPT_URL;
	public static final String FILE_SYNC_PEM_URL;
	public static final String LOCAL_SCRIPT_EXECUTE;

	public static final int HEAD_PORT;

	static {
		try {
			final Properties p = new Properties();
			p.load(new URL(PROP_URL).openStream());

			AWS_ACCESS_KEY = p.getProperty("AWS_ACCESS_KEY");
			AWS_SECRET_KEY = p.getProperty("AWS_SECRET_KEY");
			AWS_ENDPOINT = p.getProperty("AWS_ENDPOINT");

			AWS_RUN_INSTANCE_TYPE = p.getProperty("AWS_RUN_INSTANCE_TYPE");
			AWS_RUN_IMAGE_ID = p.getProperty("AWS_RUN_IMAGE_ID");
			AWS_RUN_SECURITY_GROUP_ID = p.getProperty("AWS_RUN_SECURITY_GROUP_ID");
			AWS_RUN_KEY_NAME = p.getProperty("AWS_RUN_KEY_NAME");

			LOCAL_SCRIPT_URL = p.getProperty("LOCAL_SCRIPT_URL");
			SETUP_SCRIPT_URL = p.getProperty("SETUP_SCRIPT_URL");
			FILE_SYNC_PEM_URL = p.getProperty("FILE_SYNC_PEM_URL");
			LOCAL_SCRIPT_EXECUTE = p.getProperty("LOCAL_SCRIPT_EXECUTE");

			HEAD_PORT = Integer.parseInt(p.getProperty("HEAD_PORT"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private ServerProperties() {
	}
}
