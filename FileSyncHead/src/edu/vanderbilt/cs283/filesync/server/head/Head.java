package edu.vanderbilt.cs283.filesync.server.head;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;

import edu.vanderbilt.cs283.filesync.util.IOUtils;
import edu.vanderbilt.cs283.filesync.util.Log;

public class Head {

	private static File downloadFile(final String filename, final String url) throws IOException {
		final FileOutputStream out = new FileOutputStream(filename);
		final InputStream in = new URL(url).openStream();

		IOUtils.copy(in, out);
		out.close();
		in.close();

		return new File(filename);
	}

	public static void main(String[] args) throws Exception {
		downloadFile("local.sh", ServerProperties.LOCAL_SCRIPT_URL).setExecutable(true);
		downloadFile("setup.sh", ServerProperties.SETUP_SCRIPT_URL).setExecutable(true);
		downloadFile("FileSync.pem", ServerProperties.FILE_SYNC_PEM_URL);

		SafeMap clientIpMap = new SafeMap();
		ExecutorService executor = Executors.newCachedThreadPool();
		ServerSocket serverSocket = new ServerSocket(ServerProperties.HEAD_PORT);
		while (!executor.isShutdown()) {
			executor.execute(new ClientHandler(serverSocket.accept(), clientIpMap));
		}

		serverSocket.close();
	}
}

class ClientHandler implements Runnable {

	Socket socket;
	SafeMap clientIpMap;

	ClientHandler(Socket newSock, SafeMap mainMap) {
		socket = newSock;
		clientIpMap = mainMap;
	}

	public void run() {
		String client = "";
		try {
			ObjectInputStream socketReader = new ObjectInputStream(socket.getInputStream());
			ObjectOutputStream socketWriter = new ObjectOutputStream(socket.getOutputStream());
			client = (String) socketReader.readObject();

			Log.format("User <%s> connected.", client);
			if (clientIpMap.safeContainsKey(client)) {
				while (clientIpMap.safeGet(client).equals("")) { // This can be
																	// done
																	// better
					Thread.sleep(1000); // wait->notify is better?
				}
				socketWriter.writeObject((clientIpMap.safeGet(client)));
				socketWriter.flush();
			} else {
				clientIpMap.safePut(client, "");
				String clientIp = initialize(client);
				clientIpMap.safePut(client, clientIp);
				socketWriter.writeObject((clientIpMap.safeGet(client)));
				socketWriter.flush();
			}
			Log.format("Returned <%s> to user <%s>.", clientIpMap.safeGet(client), client);
			socketWriter.close();
			socketReader.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Instance getInstance(String instanceId, AmazonEC2 ec2) {

		List<Instance> instances = new ArrayList<Instance>();

		for (Reservation r : ec2.describeInstances().getReservations()) {
			instances.addAll(r.getInstances());
		}

		for (Instance i : instances) {
			if (i.getInstanceId().equals(instanceId)) {
				return i;
			}
		}
		return null;
	}

	public static void connect(final String user, final Instance instance) throws Exception {
		try {
			final ProcessBuilder myCommand = new ProcessBuilder(ServerProperties.LOCAL_SCRIPT_EXECUTE,
					instance.getPublicDnsName());

			final String correct = "Welcome";
			String firstLine = null;

			Process process = null;
			while (firstLine == null || !firstLine.startsWith(correct)) {
				if (process != null)
					process.destroy();

				Thread.sleep(5000);
				Log.format("%s: executing %s %s", user, ServerProperties.LOCAL_SCRIPT_EXECUTE,
						instance.getPublicDnsName());
				process = myCommand.start();

				firstLine = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
			}
			Log.format("%s: connected.", user);
			Log.format("%s: setting up...", user);
			process.waitFor();
			Log.format("%s: setup finished.", user);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static String initialize(final String username) {

		final AWSCredentials credentials = new BasicAWSCredentials(ServerProperties.AWS_ACCESS_KEY,
				ServerProperties.AWS_SECRET_KEY);
		final AmazonEC2 ec2 = new AmazonEC2Client(credentials);

		ec2.setEndpoint(ServerProperties.AWS_ENDPOINT);

		RunInstancesRequest runRequest = new RunInstancesRequest();
		runRequest.withInstanceType(ServerProperties.AWS_RUN_INSTANCE_TYPE)
				.withImageId(ServerProperties.AWS_RUN_IMAGE_ID)
				.withSecurityGroupIds(ServerProperties.AWS_RUN_SECURITY_GROUP_ID)
				.withKeyName(ServerProperties.AWS_RUN_KEY_NAME).withMinCount(1).withMaxCount(1);

		RunInstancesResult result = ec2.runInstances(runRequest);
		Instance instance = result.getReservation().getInstances().get(0);

		Log.format("%s: instance created.", username);
		String instanceId = result.getReservation().getInstances().get(0).getInstanceId();
		ec2.createTags(new CreateTagsRequest(Arrays.asList(instanceId), Arrays.asList(new Tag("Name", "FileSync-"
				+ username))));
		String status = "";
		try {
			while (!status.equals("running")) {
				Thread.sleep(15000);
				if (getInstance(instanceId, ec2).equals(null))
					continue;
				status = getInstance(instanceId, ec2).getState().getName();

				Log.format("%s: instance status: %s.", username, status);
			}
			instance = getInstance(instanceId, ec2);

			connect(username, instance);

			return getInstance(instanceId, ec2).getPublicDnsName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}