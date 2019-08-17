package Thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ForwardMsgReceiveThread extends Thread {

	ServerSocket serverSocket;

	ArrayList<Socket> socketPool;

	ArrayList<String> serverMsgReceivedStorage;

	Socket socket;

	BufferedReader br;

	String info;

	public ForwardMsgReceiveThread(ServerSocket serverSocket, ArrayList<Socket> socketPool,
			ArrayList<String> serverMsgReceivedStorage) {
		this.serverSocket = serverSocket;
		this.socketPool = socketPool;
		this.serverMsgReceivedStorage = serverMsgReceivedStorage;
	}

	public void run() {
		while (true) {
			try {
				socket = serverSocket.accept();
				if (!socketPool.contains(socket)) {
					socketPool.add(socket);
					new Thread(new BackwardMsgReceiveThread(socket, serverMsgReceivedStorage)).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
