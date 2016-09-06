package eu.neclab.iotplatform.knowledgebase.model;

public class SystemParameters {

	private String hsqldb_port;
	private String sparql_port;
	private String sparql_URL;

	
	private String hsqldb_name;
	private String hsqldb_directory;
	
	private String hsqldb_username;
	private String hsqldb_password;
	
	private double subscription_time_interval; // in seconds
	private int max_cache_entries;

	public SystemParameters(String hsqldb_port, String sparql_port, String sparql_url, String hsqldb_username, 
			String hsqldb_password,String hsqldb_name, String hsqldb_directory, double subscribeTime, int maxCacheEntries) {
		super();
		this.hsqldb_port = hsqldb_port;
		this.sparql_port = sparql_port;
		this.hsqldb_username = hsqldb_username;
		this.hsqldb_password = hsqldb_password;
		this.hsqldb_name = hsqldb_name;
		this.hsqldb_directory = hsqldb_directory;
		this.sparql_URL = sparql_url;
		this.subscription_time_interval = subscribeTime;
		this.max_cache_entries = maxCacheEntries;
	}

	public String getHsqldb_port() {
		return hsqldb_port;
	}

	public String getSparql_port() {
		return sparql_port;
	}

	public String getSparql_URL() {
		return sparql_URL;
	}

	public String getHsqldb_name() {
		return hsqldb_name;
	}

	public String getHsqldb_directory() {
		return hsqldb_directory;
	}

	public String getHsqldb_username() {
		return hsqldb_username;
	}

	public String getHsqldb_password() {
		return hsqldb_password;
	}

	public double getSubscription_time_interval() {
		return subscription_time_interval;
	}

	public int getMax_cache_entries() {
		return max_cache_entries;
	}


	
	
}
