package edu.vanderbilt.cs283.filesync.server.worker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import edu.vanderbilt.cs283.filesync.util.Log;

public final class JoplinsUpdate {

	private JoplinsUpdate() {
		throw new AssertionError("Jesse wanted this to be a static callback.");
	}

	/**
	 * This method is guaranteed to be called from its own, single-threaded
	 * thread pool
	 */
	public static void onUpdate(final boolean isDelete, final String user, final String client, final long time,
			final String filename, final String hash, final String globalUrl) throws Exception {
		Class.forName("com.mysql.jdbc.Driver");
	Properties connectionProps = new Properties();
	connectionProps.put("user", "jesse");
	connectionProps.put("password", "12345");
	Connection conn = DriverManager.getConnection("jdbc:" + "mysql" + "://" +  "ec2-54-235-31-37.compute-1.amazonaws.com" +
					   ":" + "3306" + "/", connectionProps);
	System.out.println("Connected to database");
	//Log.log("Connected to database");
	//java.util.Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
	//java.sql.Timestamp timestamp = new java.sql.Timestamp(date.getTime());
	java.sql.Statement stmt = null;
	
	String query = null;
	if(!isDelete){
		query = "INSERT INTO fileshare.files (path,name,user) VALUES ('"+globalUrl+"','"+filename+"','"+user+"')";
	} 
	stmt = conn.createStatement();
	
	stmt.execute(query);

	Log.format("isDelete: %s,  user: %s, client: %s, time: %d, file: %s, hash: %s, url: %s", isDelete, user,
			   client, time, filename, hash, globalUrl);
	}
}
