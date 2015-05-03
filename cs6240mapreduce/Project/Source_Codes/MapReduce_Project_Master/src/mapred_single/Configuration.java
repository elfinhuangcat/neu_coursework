package mapred_single;

import java.util.ArrayList;

public class Configuration {
	//splitSize -the size of each split. Default is 64MB.
	private int splitSize = 64;
	
	//slaves - stores the info about all living slaves.
	private ArrayList<ConfigItem> slaves = new ArrayList<ConfigItem>();
	/**
	 * 
	 * @param pathToConfigFile path to the configuration file. (Tab separated)
	 * 
	 * Each line of the configuration comprises:
	 * slave_hostname slave_ip_addr slave_mem(GB)
	 * 
	 */
	public Configuration(String pathToConfigFile) {
		slaves.add(new ConfigItem());
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

	private class ConfigItem {
		private String hostname;
		private String ipAddr;
		private int mem;
		
		protected ConfigItem() {
			hostname = "localhost";
			ipAddr = "127.0.0.1";
			mem = 1;
		}

		protected String getHostname() {
			return hostname;
		}

		protected void setHostname(String hostname) {
			this.hostname = hostname;
		}

		protected String getIpAddr() {
			return ipAddr;
		}

		protected void setIpAddr(String ipAddr) {
			this.ipAddr = ipAddr;
		}

		protected int getMem() {
			return mem;
		}

		protected void setMem(int mem) {
			this.mem = mem;
		}
	}
}