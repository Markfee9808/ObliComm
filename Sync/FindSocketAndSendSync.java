package Sync;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Thread.BackwardMsgReceiveThread;
import Crypto.ExponentialNumberGenerator;

/*
 * This is the core of message sending
 */

public class FindSocketAndSendSync {

	SimpleDateFormat df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: ");
	
	double meanDelay = 0.333333; // in milliseconds

	public void findSocketAndSendMsg(String threadName, String hostName, ArrayList<Socket> socketPool,
			int destServerPort, String serverHost, boolean isClient, String msgTokenID, String msgContentInCipher,
			String msgPath, int roundID, int isForward, String timeStamp, ArrayList<String> msgReceiveStorage, boolean isMailbox)
			throws IOException {
		synchronized (FindSocketAndSendSync.class) {
			boolean findSocket = false;
			if (socketPool.size() != 0) {
				for (int i = 0; i < socketPool.size(); i++) {
					if (socketPool.get(i).getPort() == destServerPort
							&& socketPool.get(i).getInetAddress().getHostAddress().equals(serverHost)
							&& socketPool.get(i).isConnected()) {
						String message;
						if (isClient == true) {
							message = hostName + "_" + msgTokenID + "-" + msgContentInCipher + "-"
									+ socketPool.get(i).getLocalAddress().getHostAddress() + ":"
									+ socketPool.get(i).getLocalPort() + "-" + msgPath + "-" + roundID + "-" + isForward
									+ "-" + timeStamp; // construct
														// the
														// message
							sendMsg(socketPool.get(i), message);
						} else if (isMailbox == true) {
							message = msgTokenID + "-" + msgContentInCipher + "-" + msgPath + "-" + roundID + "-"
									+ isForward + "-" + timeStamp; // construct the message
							sendMsg(socketPool.get(i), message);
						} else {
							message = msgTokenID + "-" + msgContentInCipher + "-" + msgPath + "-" + roundID + "-"
									+ isForward + "-" + timeStamp; // construct the message
							long delay = (long)(ExponentialNumberGenerator.exponentialSample(meanDelay) * 1000);
							delayAndSendMsg(socketPool.get(i), message, delay);
						}
						
						// System.out.println(df.format(new Date()) + threadName + " in " + hostName + "
						// sends a message " + message);
						findSocket = true;
						break;
					}
				}
				if (findSocket == false) {
					Socket socket = new Socket(serverHost, destServerPort);
					String message;
					if (isClient == true) {
						message = hostName + "_" + msgTokenID + "-" + msgContentInCipher + "-"
								+ socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort() + "-"
								+ msgPath + "-" + roundID + "-" + isForward + "-" + timeStamp; // construct the
																								// message
						sendMsg(socket, message);
					} else if (isMailbox == true) {
						message = msgTokenID + "-" + msgContentInCipher + "-" + msgPath + "-" + roundID + "-"
								+ isForward + "-" + timeStamp; // construct the message
						sendMsg(socket, message);
					} else {
						message = msgTokenID + "-" + msgContentInCipher + "-" + msgPath + "-" + roundID + "-"
								+ isForward + "-" + timeStamp; // construct the message
						long delay = (long)(ExponentialNumberGenerator.exponentialSample(meanDelay) * 1000);
						delayAndSendMsg(socket, message, delay);
					}
					// System.out.println(df.format(new Date()) + threadName + " in " + hostName + "
					// sends a message " + message);
					socketPool.add(socket);
					new Thread(new BackwardMsgReceiveThread(socket, msgReceiveStorage)).start();
				}
			} else {
				Socket socket = new Socket(serverHost, destServerPort);
				String message;
				if (isClient == true) {
					message = hostName + "_" + msgTokenID + "-" + msgContentInCipher + "-"
							+ socket.getLocalAddress().getHostAddress() + ":" + socket.getLocalPort() + "-" + msgPath
							+ "-" + roundID + "-" + isForward + "-" + timeStamp; // construct the message
					sendMsg(socket, message);
				} else if (isMailbox == true) {
					message = msgTokenID + "-" + msgContentInCipher + "-" + msgPath + "-" + roundID + "-" + isForward
							+ "-" + timeStamp; // construct the message
					sendMsg(socket, message);
				} else {
					message = msgTokenID + "-" + msgContentInCipher + "-" + msgPath + "-" + roundID + "-" + isForward
							+ "-" + timeStamp; // construct the message
					long delay = (long)(ExponentialNumberGenerator.exponentialSample(meanDelay) * 1000);
					delayAndSendMsg(socket, message, delay);
				}
				// System.out.println(df.format(new Date()) + threadName + " in " + hostName + "
				// sends a message " + message);
				socketPool.add(socket);
				new Thread(new BackwardMsgReceiveThread(socket, msgReceiveStorage)).start();
			}
			findSocket = false;
		}

	}

	/*
	 * send message without delay (e.g., use in client sending and mailbox replying)
	 */
	
	public void sendMsg(Socket socket, String message) throws IOException {
		BufferedWriter bufout = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		bufout.write(message + '\n');
		bufout.flush();
	}
	
	/*
	 * send message with delay (e.g., use in server sending)
	 */
	
	public void delayAndSendMsg(Socket socket, String message, long delay) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {	
				try {
					BufferedWriter bufout = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
					bufout.write(message + '\n');
					bufout.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}			
			}
		}, delay);
	}
}
