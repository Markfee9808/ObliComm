package Thread;

import java.util.ArrayList;

import Sync.WriteLatencyToFileSync;

public class LatencyCalcThread implements Runnable {
	
	String name;
	
	private WriteLatencyToFileSync wfs;
	
	ArrayList<String> msgProcessQueue;

	public LatencyCalcThread(String name, WriteLatencyToFileSync wfs, ArrayList<String> msgProcessQueue) {
		this.name = name;
		this.wfs = wfs;	
		this.msgProcessQueue = msgProcessQueue;
	}

	public void run() {
		while (true) {
			String cipher = null;
			synchronized (msgProcessQueue) {
				if (msgProcessQueue.size() > 0) {
					cipher = msgProcessQueue.get(0);
					msgProcessQueue.remove(0);
				} else {
					break;
				}
			}
			if (cipher != null) {
				String[] contentAndAddr = cipher.split("-");
				// System.out.println("The latency of message " + contentAndAddr[0] + " is "
				// + (System.currentTimeMillis() -
				// Long.parseLong(contentAndAddr[contentAndAddr.length - 1])));
				wfs.writeFile(Long.toString(
						System.currentTimeMillis() - Long.parseLong(contentAndAddr[contentAndAddr.length - 1])));
			}
		}
	}
}
