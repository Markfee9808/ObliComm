package Main;

import java.io.BufferedReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import Crypto.PossionNumberGenerator;
import Sync.FindSocketAndSendSync;
import Sync.GetMessageTokenSync;
import Sync.WriteLatencyToFileSync;
import Thread.ClientSendMsgThread;
import Thread.LatencyCalcThread;

public class Client implements Runnable {

	/*
	 * The information needed for each client
	 */

	String clientName; // the name of the client, e.g., Client1

	int roundTime = 1; // the period time of a round (in seconds)

	int roundNum = 0; // the round number of client

	Thread clientThread; // the client thread

	double possionSamplePara = 10.0; // the parameter of possion sample (the average number of samples)

	SimpleDateFormat df = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]: "); // the format of time tag

	ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3); // the periodic parallel thread
	
	int delayTime = 3; // the time waiting for client construction

	/*
	 * The information needed for each communication round in client
	 */

	int msgGeneratedNumberInRound; // the number of message generated in a round

	int msgGeneratedNumberInTotal = 0; // the total number of message generated in client lifecycle

	int msgReceivedNumberInTotal = 0; // the total number of message received in client lifecycle

	int msgID; // the unique identity of message, from 1 to infinity

	int clientSendMsgThreadNum = 10; // the number of client sending message threads

	int clientLatencyCalcThreadNum = 10; // the number of client calculating message latency

	String msgName; // the unique name of message (format: clientName_msgID), e.g., Client1_123

	Socket socket; // the socket of communication

	BufferedReader br; // the read buffer of socket

	String info; // the information read from socket

	/*
	 * The Storage needed for messages and sockets
	 */

	ArrayList<String> clientMsgReceivedStorage = new ArrayList<String>(); // the arraylist for storing all received
																			// messages

	ArrayList<String> clientMsgProcessQueue = new ArrayList<String>(); // the arraylist for storing all processed
																		// messages in a round

	public ArrayList<Socket> clientSocketPool = new ArrayList<Socket>(); // the arraylist for storing all established
																			// sockets

	/*
	 * Message Structure: MsgID + MsgContent + Client + Server1 + Server2 + Server3
	 * + Mailbox + roundID + forward/backward + timestamp MsgID: the index of
	 * message; MsgContent: the content of message; Client, Server1, Server2,
	 * Server3, Mailbox: the ports of relays in message path from the client to the
	 * mailbox (including client and mailbox itself); roundID: the current location
	 * of message; forward/backward: the direction of message transmission
	 * (1-forward and 0-backward); timestamp: the sending time of the message;
	 */

	String msgContentInCipher = "8Rn3YEgUUD7IYyWU9klCNyqCJiuW6yFCS5aOVrIxysjV8FZXZpF75zPkw22u2sG8";

	/*
	 * Constructor of client class
	 */

	Client(String clientIP, int clientID) {
		this.clientName = "Client/" + clientIP + ":" + clientID;
		System.out.println(df.format(new Date()) + "Initializing " + clientName);
	}

	@Override
	public void run() {

		/*
		 * For each round, roundNum plus one
		 */
		
		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			public void run() {
				roundNum++;
			}
		}, delayTime, roundTime, TimeUnit.SECONDS);

		/*
		 * For each round, the client generates some messages and sends them
		 */

		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			public void run() {
				msgGeneratedNumberInRound = PossionNumberGenerator.poissonSample(possionSamplePara);
				msgGeneratedNumberInTotal += msgGeneratedNumberInRound;
				System.out.println(df.format(new Date()) + "Round " + roundNum + " " + clientName + " generates "
						+ msgGeneratedNumberInRound + " messages, and " + msgGeneratedNumberInTotal
						+ " messages totally.");
				ClientSendMsgThread[] csmt = new ClientSendMsgThread[clientSendMsgThreadNum];
				GetMessageTokenSync sync = new GetMessageTokenSync(msgGeneratedNumberInRound, msgID);
				for (int i = 0; i < csmt.length; i++) {
					FindSocketAndSendSync send = new FindSocketAndSendSync();
					csmt[i] = new ClientSendMsgThread("clientSendMsgThread " + i, clientName, sync, clientSocketPool,
							send, msgContentInCipher, clientMsgReceivedStorage);
					new Thread(csmt[i]).start();
				}
				msgID += msgGeneratedNumberInRound;
			}
		}, delayTime, roundTime, TimeUnit.SECONDS);

		/*
		 * For each round, the client receives amount of messages and calculates the
		 * latency of them using the timestamp, then writes latency to the file
		 */

		scheduledThreadPool.scheduleAtFixedRate(new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				synchronized (clientMsgReceivedStorage) {
					clientMsgProcessQueue = (ArrayList<String>) clientMsgReceivedStorage.clone();
					clientMsgReceivedStorage.clear();
				}
				if (clientMsgProcessQueue.size() > 0) {
					msgReceivedNumberInTotal += clientMsgProcessQueue.size();
					System.out.println(df.format(new Date()) + "Round " + roundNum + " " + clientName + " receives "
							+ clientMsgProcessQueue.size() + " messages, and " + msgReceivedNumberInTotal
							+ " messages totally.");
					LatencyCalcThread[] dt = new LatencyCalcThread[clientLatencyCalcThreadNum];
					for (int i = 0; i < dt.length; i++) {
						WriteLatencyToFileSync wfs = new WriteLatencyToFileSync();
						dt[i] = new LatencyCalcThread("latencyCalcThread " + i, wfs, clientMsgProcessQueue);
						new Thread(dt[i]).start();
					}
				} else {
					System.out.println(df.format(new Date()) + "Round " + roundNum + " " + clientName
							+ " receives 0 messages, and " + msgReceivedNumberInTotal + " messages totally.");
				}
			}
		}, delayTime + roundTime, roundTime, TimeUnit.SECONDS); // Start from the second round	

	}

	public void start() {
		clientThread = new Thread(this, clientName);
		clientThread.start();
		System.out.println(df.format(new Date()) + "Starting " + clientName);
	}
	
	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println("Wrong Parameters! Running Format: java -jar + [ObliComm Path] + [1] + [IP Address] + [ThreadNumber]");
			System.exit(0);
		} else {
			Client[] c = new Client[Integer.parseInt(args[2])];
			for(int i = 0; i < c.length; i++) {
				c[i] = new Client(args[1], i);
				c[i].start();
			}
		}
	}
}