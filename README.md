# ObliComm

ObliComm is a modular anonymous communication (AC) framework that protects the privacy of message contents and message metadata. Users can customize communication network in different topologies and test their latency. ObliComm consists of three components:

 - User: send messages at a specified frequency, and receive replies from mailbox
 - Server: decrypt and delay received messages, forward and backward transmission
 - Mailbox: after receiving a message/request, send a reply back to user

## Deployment Request

Java JDK version 1.8 ([online available](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html))

## How to Run?

ObliComm supports both on Windows and Linux OS, and has two deployment modes: stand-alone mode (i.e., run three components on a single host) and distributed mode (i.e., run three components on different hosts).

**Step 1**: Configure a static IP for each host involving in anonymous communication.

**Step 2**: Copy ObliComm to the directory with execute right in each host.

**Step 3**: Define the network topology in file *"~/Main/NetworkTopology.java"*.
> **Note**:   For stand-alone mode, you can use *localhost* (i.e., 127.0.0.1) and choose different ports for servers and mailboxes.
 
**Step 4**: Compile ObliComm and get a jar package.
> **Note**:  For Linux OS, you need to use the following key generation codes in *"~/Crypto/AesDecryption.java"* to handle BadPaddingException. 
> `SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG"); secureRandom.setSeed(password.getBytes()); kgen.init(128, secureRandom);` While for Windows OS, use `kgen.init(128, new SecureRandom(password.getBytes()));` instead. See [here](https://stackoverflow.com/questions/8049872/given-final-block-not-properly-padded) for more information.

**Step 5**: Run the jar package using the command ''`java -jar + [ObliComm Path] + [1(Client) / 2(Server) / 3(MailBox)] + [1([IP Address] + [ThreadNumber]) / 2([IP Address] + [port]) / 3([IP Address] + [port])]`'', and you will find a *latency* file in the root directory of hosts running clients.

## Demo Video

A 3-minute video has been uploaded to show our demo ([online available](https://github.com/Markfee9808/ObliComm/blob/master/Demo%20Video.mp4)).

## Contact us

Pengfei Wu, School of Software and Microelectronics, Peking University, Beijing, China

e-mail: wpf9808@pku.edu.cn

## See also

 - [Vuvuzela](https://github.com/vuvuzela/vuvuzela)
 - [Stadium](https://github.com/nirvantyagi/stadium)
 - [Loopix](https://github.com/UCL-InfoSec/loopix)
 
