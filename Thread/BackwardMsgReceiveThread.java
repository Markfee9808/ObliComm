package Thread;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class BackwardMsgReceiveThread implements Runnable {

	Socket socket;

	BufferedReader br;

	String info;

	ArrayList<String> serverMsgReceivedStorage;

	public BackwardMsgReceiveThread(Socket socket, ArrayList<String> serverMsgReceivedStorage) {
		this.socket = socket;
		this.serverMsgReceivedStorage = serverMsgReceivedStorage;
	}

	@Override
	public void run() {
		try {
			InputStream is = socket.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			while ((info = br.readLine()) != null) {
				synchronized (serverMsgReceivedStorage) {
					serverMsgReceivedStorage.add(info);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
