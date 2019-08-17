package Main;

/*
 * This is the entry point of the jar package
 */
public class Main {

	public static void main(String[] args) {
		if(args.length != 3) {
			System.out.println(
					"Wrong Parameters! Running Format: java -jar + [ObliComm Path] + [1(Client) / 2(Server) / 3(MailBox)] + "
							+ "[1([IP Address] + [ThreadNumber]) / 2([IP Address] + [port]) / 3([IP Address] + [port])]");
			System.exit(0);
		} else {
			if (args[0].equals("1")) {
				Client[] c = new Client[Integer.parseInt(args[2])];
				for (int i = 0; i < c.length; i++) {
					c[i] = new Client(args[1], i);
					c[i].start();
				}
			} else if (args[0].equals("2")) {
				Server s = new Server(args[1], Integer.parseInt(args[2]));
				s.start();
			} else if (args[0].equals("3")) {
				Mailbox m = new Mailbox(args[1], Integer.parseInt(args[2]));
				m.start();
			}
		}
	}
}
