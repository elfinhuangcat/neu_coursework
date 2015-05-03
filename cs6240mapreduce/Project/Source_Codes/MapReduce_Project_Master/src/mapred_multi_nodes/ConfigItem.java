package mapred_multi_nodes;

/**
 * 
 * @author yaxin
 */
public class ConfigItem {
    private String hostname;
    private String ipAddr; // For socket connection
    private int port; // For socket connection
    private String privateKeyPath; // For ssh and scp
    private String loginUser; // For ssh and scp
    private String rootDir; // workspace directory absolute path
    
    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public ConfigItem() {
        this.hostname = null;
        this.ipAddr = null;
        this.port = -250;
        this.privateKeyPath = null;
        this.loginUser = null;
        this.rootDir = null;
    }
    
    public ConfigItem(String hostname, String ipAddr, 
            int port, String pKey, String user, String rootDir) {
        this.hostname = hostname;
        this.ipAddr = ipAddr;
        this.port = port;
        this.privateKeyPath = pKey;
        this.loginUser = user;
        this.rootDir = rootDir;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getLoginUser() {
        return loginUser;
    }

    public void setLoginUser(String loginUser) {
        this.loginUser = loginUser;
    }

    public String getIpAddr() {
        return this.ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }        
}