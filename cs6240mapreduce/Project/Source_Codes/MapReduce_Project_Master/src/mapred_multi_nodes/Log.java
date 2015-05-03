package mapred_multi_nodes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This Log class can also be used by users to create their own logs.
 * @author yaxin
 *
 */
public class Log {
    private File logFile;
    
    public Log(String logFilePath) {
        this.logFile = new File(logFilePath);
    }
    
    public void writeLog(String str) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true));
            bw.write(str + "\n");
            System.out.println(str);
            bw.close();
        } catch (IOException e) {
            System.out.println("ERROR - Log: CANNOT OPEN LOG FILE!!!");
            e.printStackTrace();
        }
    }
}