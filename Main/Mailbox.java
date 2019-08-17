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
import Thread.MailBoxSendMsgThread;

public class Mailbox implements Runnable {

	/*
	 * The information needed for each server
	 */

	String mailBoxName; // the name of the server, e.g., Server1
	
	String mailBoxIP;

	int mailBoxPort; // the port of server

	int roundTime = 1; // the period time of a round (in milliseconds)

	int roundNum = 0; // the round number of server

	public String msgPassword = "123"; // the password of message encryption and decryption

	ServerSocket mailBoxSocket;

	Thread mailBoxThread; // the server thread

	SimpleDateFormat df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: "); // the format of time tag

	ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3); // the periodic parallel thread

	/*
	 * The information needed for each communication round in server
	 */

	int msgReceivedNumberInTotal = 0; // the total number of message received in MailBox lifecycle
	
	int msgReceivedNumberInSecond = 0;

	int mailBoxEncryptAndSendMsgThreadNum = 10; // the number of thread of encrypt

	/*
	 * The Storage needed for messages and sockets
	 */

	ArrayList<String> mailBoxMsgReceivedStorage = new ArrayList<String>(); // the arraylist for storing all received
																			// messages

	public ArrayList<String> mailBoxMsgProcessQueue = new ArrayList<String>(); // the arraylist for storing all
																				// processed
																				// messages in a round

	ArrayList<Socket> mailBoxSocketPool = new ArrayList<Socket>(); // the arraylist for storing all established sockets

	/*
	 * Constructor of mailBox class
	 */

	Mailbox(String mailBoxIP, int port) {
		this.mailBoxName = "MailBox/" + mailBoxIP + ":" + port;
		this.mailBoxIP = mailBoxIP;
		this.mailBoxPort = port;
		System.out.println(df.format(new Date()) + "Initializing mailbox: " + mailBoxName);
	}

	@Override
	public void run() {
		ForwardMsgReceiveThread fmrt = new ForwardMsgReceiveThread(mailBoxSocket, mailBoxSocketPool,
				mailBoxMsgReceivedStorage);
		fmrt.start();

		/*
		 * For each round, roundNum plus one
		 */

		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			public void run() {
				roundNum++;
				System.out.println(df.format(new Date()) + "Round " + roundNum + " " + mailBoxName + " receives "
							+ msgReceivedNumberInSecond + " messages, and " + msgReceivedNumberInTotal
							+ " messages totally.");
				msgReceivedNumberInSecond = 0;
			}
		}, 0, roundTime, TimeUnit.SECONDS);

		/*
		 * For each round, the mailbox receives messages, change the
		 * forward/backward tag to 0, then returns back the message
		 */

		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				synchronized (mailBoxMsgReceivedStorage) {
					mailBoxMsgProcessQueue = (ArrayList<String>) mailBoxMsgReceivedStorage.clone();
					mailBoxMsgReceivedStorage.clear();
				}
				if (mailBoxMsgProcessQueue.size() > 0) {
					msgReceivedNumberInTotal += mailBoxMsgProcessQueue.size();
					msgReceivedNumberInSecond += mailBoxMsgProcessQueue.size();
					MailBoxSendMsgThread[] dt = new MailBoxSendMsgThread[10];
					for (int i = 0; i < dt.length; i++) {
						FindSocketAndSendSync send = new FindSocketAndSendSync();
						dt[i] = new MailBoxSendMsgThread("receiveThread" + i, mailBoxName, send, mailBoxMsgProcessQueue,
								mailBoxSocketPool, mailBoxMsgReceivedStorage);
						new Thread(dt[i]).start();
					}
				}
			}
		}, roundTime, 1, TimeUnit.MILLISECONDS);
	}

	public void start() {
		try {
			mailBoxSocket = new ServerSocket();
			mailBoxSocket.bind(new InetSocketAddress(mailBoxIP, mailBoxPort));
			System.out.println(df.format(new Date()) + "Creating " + mailBoxName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		mailBoxThread = new Thread(this, mailBoxName);
		mailBoxThread.start();
		System.out.println(df.format(new Date()) + "Starting " + mailBoxName);
	}
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Wrong Parameters! Running Format: java -classpath + [ObliComm Path] + [3] + [IP Address] + [port]");
			System.exit(0);
		} else {
			Mailbox m1 = new Mailbox(args[1], Integer.parseInt(args[2]));
			m1.start();
		}	
	}
}
