package Sync;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/*
 * Write the calculated latency into local file (the file only exists in root path of hosts running client)
 */

public class WriteLatencyToFileSync {
	
	public void writeFile(String content) {
		synchronized (WriteLatencyToFileSync.class) {
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("latency"), true));
				writer.write(content + '\n');
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
