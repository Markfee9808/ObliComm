package Thread;

import java.net.Socket;
import java.util.ArrayList;

import Main.NetworkTopology;
import Sync.FindSocketAndSendSync;
import Sync.GetMessageTokenSync;

public class ClientSendMsgThread implements Runnable {

	String clientSendMsgThreadName; // the name of clientSendMsgThread, e.g., clientSendMsgThread1

	String clientName;

	private GetMessageTokenSync gmts; // the synchronized class of getting one message token

	private FindSocketAndSendSync fsass; // the synchronized class of finding a socket in socketPool and using it to
											// send one message

	String msgContentInCipher;

	ArrayList<Socket> clientSocketPool;

	ArrayList<String> clientMsgReceiveStorage;

	/*
	 * constructor of clientSendMsgThread
	 */

	public ClientSendMsgThread(String threadName, String hostName, GetMessageTokenSync gmts,
			ArrayList<Socket> clientSocketPool, FindSocketAndSendSync fsass, String msgContentInCipher,
			ArrayList<String> clientMsgReceiveStorage) {
		this.clientSendMsgThreadName = threadName;
		this.clientName = hostName;
		this.gmts = gmts;
		this.fsass = fsass;
		this.clientSocketPool = clientSocketPool;
		this.msgContentInCipher = msgContentInCipher;
		this.clientMsgReceiveStorage = clientMsgReceiveStorage;
	}

	@Override
	public void run() {
		while (true) {
			int msgTokenID = gmts.getToken();
			if (msgTokenID == 0) {
				break;
			} else {
				String msgPath = NetworkTopology.path[(int) (1 + Math.random() * NetworkTopology.path.length) - 1];
				int serverIndex = Integer.parseInt(msgPath.split("-")[0]);
				String serverHost = NetworkTopology.serverIP[serverIndex - 1].split(":")[0];
				int destServerPort = Integer.parseInt(NetworkTopology.serverIP[serverIndex - 1].split(":")[1]);
				try {
					fsass.findSocketAndSendMsg(clientSendMsgThreadName, clientName, clientSocketPool,
							destServerPort, serverHost, true, "" + msgTokenID,
							msgContentInCipher, msgPath, 0, 1, "" + System.currentTimeMillis(),
							clientMsgReceiveStorage, false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
