package Thread;

import java.net.Socket;
import java.util.ArrayList;

import Main.NetworkTopology;
import Sync.FindSocketAndSendSync;

public class MailBoxSendMsgThread implements Runnable {

	String mailBoxSendMsgThreadName;

	String mailBoxName;

	FindSocketAndSendSync send;

	ArrayList<String> mailBoxMsgReceiveStorage;

	ArrayList<String> mailBoxMsgProcessQueue;

	ArrayList<Socket> mailBoxSocketPool;

	public MailBoxSendMsgThread(String name, String mailBoxName, FindSocketAndSendSync send,
			ArrayList<String> mailBoxMsgProcessQueue, ArrayList<Socket> mailBoxSocketPool,
			ArrayList<String> mailBoxMsgReceiveStorage) {
		this.mailBoxSendMsgThreadName = name;
		this.mailBoxName = mailBoxName;
		this.send = send;
		this.mailBoxMsgProcessQueue = mailBoxMsgProcessQueue;
		this.mailBoxSocketPool = mailBoxSocketPool;
		this.mailBoxMsgReceiveStorage = mailBoxMsgReceiveStorage;
	}

	@Override
	public void run() {
		while (true) {
			String cipher = null;
			synchronized (mailBoxMsgProcessQueue) {
				if (mailBoxMsgProcessQueue.size() > 0) {
					cipher = mailBoxMsgProcessQueue.get(0);
					mailBoxMsgProcessQueue.remove(0);
				} else {
					break;
				}
			}
			if (cipher != null) {
				String[] contentAndAddr = cipher.split("-");
				String msgPath = contentAndAddr[2];
				for (int j = 3; j < (contentAndAddr.length - 3); j++) {
					msgPath += "-";
					msgPath += contentAndAddr[j];
				}
				int serverIndex = Integer.parseInt(contentAndAddr[contentAndAddr.length - 5]);
				String serverHost = NetworkTopology.serverIP[serverIndex - 1].split(":")[0];
				int destServerPort = Integer.parseInt(NetworkTopology.serverIP[serverIndex - 1].split(":")[1]);
				try {
					send.findSocketAndSendMsg(mailBoxSendMsgThreadName, mailBoxName, mailBoxSocketPool, destServerPort,
							serverHost, false, contentAndAddr[0], contentAndAddr[1], msgPath,
							Integer.parseInt(contentAndAddr[contentAndAddr.length - 3]), 0,
							contentAndAddr[contentAndAddr.length - 1], mailBoxMsgReceiveStorage, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
