package mapred_two_nodes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * To store config information.
 * @author yaxin
 *
 */
public class Configuration {
	//splitSize -the size of each split. Default is 64MB.
	private int splitSize = 64;
	private final String SEP = "\t";
	private final String MASTER_IDENTIFIER = "MASTER";
	
	//slaves - stores the info about all living slaves.
	private ArrayList<ConfigItem> slaves;
	//master - stores the info about master
	private ConfigItem master;
	
	public Configuration() {
		this.slaves = null;
	}
	/**
	 * User should call this constructor.
	 * Each line of the configuration comprises:
	 * slave_hostname slave_ip_addr port_num private_key_full_path login_user_name
	 * (separated by tabs)
	 * @param pathToConfigFile path to the configuration file. (Tab separated)
	 * @throws IOException 
	 */
	public Configuration(String pathToConfigFile) throws IOException {
		this.master = new ConfigItem();
		this.slaves = new ArrayList<ConfigItem>();
		BufferedReader br = new BufferedReader(new FileReader(pathToConfigFile));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] item = line.split(this.SEP);
			//TODO In the future, the master will also hold a listener.
			if (item[0].equals(this.MASTER_IDENTIFIER)) {
				master.setHostname(MASTER_IDENTIFIER);
				master.setIpAddr(item[1]);
				master.setPort(Integer.parseInt(item[2]));
				master.setPrivateKeyPath(item[3]);
				master.setLoginUser(item[4]);
			}
			else {
				// Slave item
				slaves.add(new ConfigItem(item[0], item[1], 
						Integer.parseInt(item[2]), item[3], item[4]));
			}
		}
		br.close();
	}
	
	public int getSplitSize() {
		return splitSize;
	}

	public void setSplitSize(int splitSize) {
		this.splitSize = splitSize;
	}

	public ArrayList<ConfigItem> getSlaves() {
		return slaves;
	}
	protected ConfigItem getMaster() {
		return master;
	}
}

/**
 * 
 * @author yaxin
 */
class ConfigItem {
	private String hostname;
	private String ipAddr; // For socket connection
	private int port; // For socket connection
	private String privateKeyPath; // For ssh and scp
	private String loginUser; // For ssh and scp
	
	public ConfigItem() {
		this.hostname = null;
		this.ipAddr = null;
		this.port = -250;
		this.privateKeyPath = null;
		this.loginUser = null;
	}
	
	public ConfigItem(String hostname, String ipAddr, int port, String pKey, String user) {
		this.hostname = hostname;
		this.ipAddr = ipAddr;
		this.port = port;
		this.privateKeyPath = pKey;
		this.loginUser = user;
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