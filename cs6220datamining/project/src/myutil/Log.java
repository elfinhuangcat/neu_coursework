package myutil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
	private String logPath;
	public Log(String logPath) {
		this.logPath = logPath;
	}
	public static void addLogToThisPath(String log, String path) {
		System.out.print(log);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(path, true));// append
			bw.write(log);
			bw.close();
		} catch (IOException e1) {
			System.out.println("ERROR - LOG ERROR");
			e1.printStackTrace();
			System.exit(1);
		} 
	}
	public void addLog(String log) {
		System.out.print(log);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(this.logPath, true));// append
			bw.write(log);
			bw.close();
		} catch (IOException e1) {
			System.out.println("ERROR - LOG ERROR");
			e1.printStackTrace();
			System.exit(1);
		}
	}
}