package Main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Sync.FindSocketAndSendSync;
import Thread.ForwardMsgReceiveThread;
import Thread.ServerDecryptAndSendMsgThread;

public class Server implements Runnable {

	/*
	 * The information needed for each server
	 */

	String serverName; // the name of the server, e.g., Server1
	
	String serverIP;

	int serverPort; // the port of server

	int roundTime = 1; // the period time of a round (in milliseconds)

	int roundNum = 0; // the round number of server

	public String msgPassword = "123"; // the password of message encryption and decryption

	ServerSocket serverSocket;

	Thread serverThread; // the server thread

	SimpleDateFormat df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: "); // the format of time tag

	ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3); // the periodic parallel thread

	/*
	 * The information needed for each communication round in server
	 */

	int msgReceivedNumberInTotal = 0; // the total number of message received in server lifecycle (including forward
										// and backward)
	
	int msgReceivedNumberInSecond = 0;

	int serverDecryptAndSendMsgThreadNum = 10; // the number of server decrypting and sending message threads

	/*
	 * The Storage needed for messages and sockets
	 */

	ArrayList<String> serverMsgReceivedStorage = new ArrayList<String>(); // the arraylist for storing all received
																			// messages

	public ArrayList<String> serverMsgProcessQueue = new ArrayList<String>(); // the arraylist for storing all processed
																				// messages in a round

	ArrayList<Socket> serverSocketPool = new ArrayList<Socket>(); // the arraylist for storing all established sockets

	/*
	 * Constructor of server class
	 */

	Server(String serverIP, int port) {
		this.serverName = "Server/" + serverIP + ":" + port;
		this.serverIP = serverIP;
		this.serverPort = port;
		System.out.println(df.format(new Date()) + "Initializing server: " + serverName);
	}

	@Override
	public void run() {
		ForwardMsgReceiveThread fmrt = new ForwardMsgReceiveThread(serverSocket, serverSocketPool,
				serverMsgReceivedStorage);
		fmrt.start();

		/*
		 * For each round, roundNum plus one
		 */

		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			public void run() {
				roundNum++;
				System.out.println(df.format(new Date()) + "Round " + roundNum + " " + serverName + " receives "
							+ msgReceivedNumberInSecond + " messages, and " + msgReceivedNumberInTotal
							+ " messages totally.");
				msgReceivedNumberInSecond = 0;
			}
		}, 0, roundTime, TimeUnit.SECONDS);

		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				synchronized (serverMsgReceivedStorage) {
					serverMsgProcessQueue = (ArrayList<String>) serverMsgReceivedStorage.clone();
					serverMsgReceivedStorage.clear();
				}
				if (serverMsgProcessQueue.size() > 0) {	
					msgReceivedNumberInTotal += serverMsgProcessQueue.size();	
					msgReceivedNumberInSecond += serverMsgProcessQueue.size();	
					ServerDecryptAndSendMsgThread[] dt = new ServerDecryptAndSendMsgThread[serverDecryptAndSendMsgThreadNum];
					for (int i = 0; i < dt.length; i++) {
						FindSocketAndSendSync send = new FindSocketAndSendSync();
						dt[i] = new ServerDecryptAndSendMsgThread("ServerDecryptAndSendMsgThread " + i, serverName,
								send, serverMsgProcessQueue, serverSocketPool, msgPassword, serverMsgReceivedStorage);
						new Thread(dt[i]).start();
					}
				}
			}
		}, roundTime, 1, TimeUnit.MILLISECONDS);
	}

	public void start() {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(serverIP, serverPort));
			System.out.println(df.format(new Date()) + "Creating server: " + serverName);

		} catch (IOException e) {
			e.printStackTrace();
		}
		serverThread = new Thread(this, serverName);
		serverThread.start();
		System.out.println(df.format(new Date()) + "Starting server: " + serverName);
	}
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Wrong Parameters! Running Format: java -classpath + [ObliComm Path] + [2] + [IP Address] + [port] + [roundTime]");
			System.exit(0);
		} else {
			Server s = new Server(args[1], Integer.parseInt(args[2]));
			s.start();
		}
	}
}
