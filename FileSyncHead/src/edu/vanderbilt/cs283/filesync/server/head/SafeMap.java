package edu.vanderbilt.cs283.filesync.server.head;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class SafeMap {
	private Map<String, String> clientIpMap;
	
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock read  = readWriteLock.readLock();
	private final Lock write = readWriteLock.writeLock();
	
	SafeMap(){
		clientIpMap = new HashMap<String, String>();
	}
	
	public String putIfAbsent(String key, String value) {
	   if (!clientIpMap.containsKey(key))
	       return clientIpMap.put(key, value);
	   else
	       return clientIpMap.get(key);
	}
	public boolean safeContainsKey(String clientName) {
		read.lock();
		boolean result = clientIpMap.containsKey(clientName);
		read.unlock();
		return result;
	}
	
	public void safePut(String clientName, String ip){
		write.lock();
		clientIpMap.put(clientName, ip);
		write.unlock();
	}
	
	public String safeGet(String clientName){
		read.lock();
		String result = clientIpMap.get(clientName);
		read.unlock();
		return result;
	}
}