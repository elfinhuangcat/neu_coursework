package mapred_multi_nodes;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;


    /**
     * Split File 
     * acknowledgement : mapreduce split file api; 
     * http://www.javabeat.net/java-split-merge-files/
     * @author Xin Fang
     * 
     */
public class SplitFile {
    private String FILE_NAME;
    private long BYTES_PER_SPLIT;
    private String OUTPUT_NAME;
    
   
    /** Constructs a split with host and cached-blocks information
      *
      * @param fileName the file name
      * @param bytesPerSplit the size of each part file after splitting, in Bytes.
     *  @param outptuName out put path name
      */
    public SplitFile(String fileName, long bytesPerSplit, String outputName) throws Exception{
        this.FILE_NAME =fileName;
        this.BYTES_PER_SPLIT= bytesPerSplit;
        this.OUTPUT_NAME = outputName;
        RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "r");
        long sourceFileSize = raf.length();
        long remainByte = sourceFileSize%BYTES_PER_SPLIT;
        int numSplits = 0;
        int maxReadBufferSize = 8 * 1024; //8KB

        if(remainByte == 0){
             numSplits = (int)(sourceFileSize/BYTES_PER_SPLIT);
             for(int chunkNum=1; chunkNum <= numSplits; chunkNum++) {
                 readWrite(raf,BYTES_PER_SPLIT,maxReadBufferSize,chunkNum); 
             }
        }else{
             numSplits = (int)(sourceFileSize/BYTES_PER_SPLIT)+1;
             for(int chunkNum=1; chunkNum <= numSplits; chunkNum++) {
                 if(chunkNum != numSplits ){
                     readWrite(raf,BYTES_PER_SPLIT,maxReadBufferSize,chunkNum); 
                 }else{
                     readWrite(raf,remainByte,maxReadBufferSize,chunkNum); 
                 }
             }   
        }
    }
    private void readWrite(RandomAccessFile raf, long byteToread, int maxReadBufferSize, int chunkNum) throws IOException {
        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(OUTPUT_NAME+chunkNum));

        if(byteToread > maxReadBufferSize) {
            long numReads = byteToread/maxReadBufferSize;
            long numRemainingRead = byteToread % maxReadBufferSize;
            for(int i=0; i<numReads; i++) {
                readWriteHelper(raf, bw, maxReadBufferSize);
            }
            if(numRemainingRead > 0) {
                readWriteHelper(raf, bw, numRemainingRead);
            }
            
        }else {
            readWriteHelper(raf, bw, byteToread);
        }
        bw.close();
    }
    
    static void readWriteHelper(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException  {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        } 
    }
  
}
