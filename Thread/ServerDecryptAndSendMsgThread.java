package Thread;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import Crypto.AesDecryption;
import Crypto.AesEncryption;
import java.util.Base64;
import Main.NetworkTopology;
import Sync.FindSocketAndSendSync;

public class ServerDecryptAndSendMsgThread implements Runnable {

	String serverDecryptAndSendMsgThreadName;

	String serverName;

	String msgPassword;

	FindSocketAndSendSync send;

	ArrayList<String> serverMsgReceiveStorage;

	ArrayList<String> serverMsgProcessQueue; // the arraylist for storing all processed messages in a round

	ArrayList<Socket> socketPool;

	SimpleDateFormat df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: ");

	public ServerDecryptAndSendMsgThread(String serverDecryptAndSendMsgThreadName, String serverName,
			FindSocketAndSendSync send, ArrayList<String> serverMsgProcessQueue, ArrayList<Socket> socketPool,
			String msgPassword, ArrayList<String> serverMsgReceiveStorage) {
		this.serverDecryptAndSendMsgThreadName = serverDecryptAndSendMsgThreadName;
		this.serverName = serverName;
		this.send = send;
		this.serverMsgProcessQueue = serverMsgProcessQueue;
		this.socketPool = socketPool;
		this.msgPassword = msgPassword;
		this.serverMsgReceiveStorage = serverMsgReceiveStorage;
	}

	public void run() {
		while (true) {
			String cipher = null;
			synchronized (serverMsgProcessQueue) {
				if (serverMsgProcessQueue.size() > 0) {
					cipher = serverMsgProcessQueue.get(0);
					serverMsgProcessQueue.remove(0);
				}
			}
			if (cipher != null) {
				String[] contentAndAddr = cipher.split("-");
				if (contentAndAddr[contentAndAddr.length - 2].equals("1")) {
					String cipherAfterDecryption = new String(
							AesDecryption.decrypt(Base64.getDecoder().decode(contentAndAddr[1]), msgPassword));
					String msgPath = contentAndAddr[2];
					for (int j = 3; j < (contentAndAddr.length - 3); j++) {
						msgPath += "-";
						msgPath += contentAndAddr[j];
					}
					int roundID = Integer.parseInt(contentAndAddr[contentAndAddr.length - 3]) + 1;
					if(3 + roundID != contentAndAddr.length - 4) {
						int serverIndex = Integer.parseInt(contentAndAddr[3 + roundID]);
						String serverHost = NetworkTopology.serverIP[serverIndex - 1].split(":")[0];
						int destServerPort = Integer.parseInt(NetworkTopology.serverIP[serverIndex - 1].split(":")[1]);
						try {
							send.findSocketAndSendMsg(serverDecryptAndSendMsgThreadName, serverName, socketPool,
									destServerPort, serverHost, false, contentAndAddr[0],
									cipherAfterDecryption, msgPath, roundID, 1, contentAndAddr[contentAndAddr.length - 1],
									serverMsgReceiveStorage, false);
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						}
					} else {
						int mailboxIndex = Integer.parseInt(contentAndAddr[3 + roundID].substring(1));
						String mailboxHost = NetworkTopology.mailboxIP[mailboxIndex - 1].split(":")[0];
						int destMailboxPort = Integer.parseInt(NetworkTopology.mailboxIP[mailboxIndex - 1].split(":")[1]);
						try {
							send.findSocketAndSendMsg(serverDecryptAndSendMsgThreadName, serverName, socketPool,
									destMailboxPort, mailboxHost, false, contentAndAddr[0],
									cipherAfterDecryption, msgPath, roundID, 1, contentAndAddr[contentAndAddr.length - 1],
									serverMsgReceiveStorage, false);
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						}
					}
				} else {
					String cipherAfterEncryption = Base64.getEncoder().encodeToString(AesEncryption.encrypt(contentAndAddr[1], msgPassword));
					String msgPath = contentAndAddr[2];
					for (int j = 3; j < (contentAndAddr.length - 3); j++) {
						msgPath += "-";
						msgPath += contentAndAddr[j];
					}
					int roundID = Integer.parseInt(contentAndAddr[contentAndAddr.length - 3]) - 1;
					if(2 + roundID != 2) {
						int serverIndex = Integer.parseInt(contentAndAddr[2 + roundID]);
						String serverHost = NetworkTopology.serverIP[serverIndex - 1].split(":")[0];
						int destServerPort = Integer.parseInt(NetworkTopology.serverIP[serverIndex - 1].split(":")[1]);
						try {
							send.findSocketAndSendMsg(serverDecryptAndSendMsgThreadName, serverName, socketPool,
									destServerPort, serverHost, false, contentAndAddr[0],
									cipherAfterEncryption, msgPath, roundID, 0, contentAndAddr[contentAndAddr.length - 1],
									serverMsgReceiveStorage, false);
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						}
					} else {
						String clientHost = contentAndAddr[2].split(":")[0];
						int destClientPort = Integer.parseInt(contentAndAddr[2].split(":")[1]);
						try {
							send.findSocketAndSendMsg(serverDecryptAndSendMsgThreadName, serverName, socketPool,
									destClientPort, clientHost, false, contentAndAddr[0],
									cipherAfterEncryption, msgPath, roundID, 0, contentAndAddr[contentAndAddr.length - 1],
									serverMsgReceiveStorage, false);
						} catch (NumberFormatException | IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
