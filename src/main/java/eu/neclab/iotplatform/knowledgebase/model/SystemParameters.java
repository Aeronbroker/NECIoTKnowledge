/*******************************************************************************
 * Copyright (c) 2016, NEC Europe Ltd.
 * All rights reserved.
 * 
 * Authors:
 *          * NEC IoT Platform Team - iotplatform@neclab.eu
 *          * Gurkan Solmaz - gurkan.solmaz@neclab.eu
 *          * Flavio Cirillo - flavio.cirillo@neclab.eu
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 1. Redistributions of source code must retain the above copyright 
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above 
 * copyright notice, this list of conditions and the following disclaimer 
 * in the documentation and/or other materials provided with the 
 * distribution.
 * 3. All advertising materials mentioning features or use of this 
 * software must display the following acknowledgment: This 
 * product includes software developed by NEC Europe Ltd.
 * 4. Neither the name of NEC nor the names of its contributors may 
 * be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY NEC ''AS IS'' AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN 
 * NO EVENT SHALL NEC BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH 
 * DAMAGE.
 ******************************************************************************/

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
