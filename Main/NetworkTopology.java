package Main;

/*
 * define your topology here !!
 */

public class NetworkTopology {

	public static String[] serverIP = {}; // define all servers in topology here, in the form of "ip:port", e.g., "127.0.0.1:10001"

	public static String[] mailboxIP = {}; // define all mailboxes in topology here, in the form of "ip:port", e.g., "127.0.0.1:60001"

	public static String[] path = {}; // define all possible paths here, from an entry point to a mailbox. Each server is denoted as the index 
	//in the array 'serverIP', and each mailbox is denoted as the 'm-' + index in the array 'mailboxIP'. e.g., 1-2-3-m1.

}
