package Sync;

public class GetMessageTokenSync {
	
	int msgNum;
	
	int msgID;
	
	public GetMessageTokenSync(int msgNum, int msgID) {
		this.msgNum = msgNum;
		this.msgID = msgID;
	}
	
	public int getToken() {
		
		synchronized (GetMessageTokenSync.class) {
			if (msgNum > 0) {
				msgNum--;
				msgID++;
				return msgID;
			} else {
				return 0;
			}
		}
	}
}
