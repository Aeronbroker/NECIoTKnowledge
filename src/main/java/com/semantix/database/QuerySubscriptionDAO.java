/*******************************************************************************
 * Copyright (c) 2015, NEC Europe Ltd.
 * All rights reserved.
 * 
 * Authors:
 *          * Salvatore Longo - salvatore.longo@neclab.eu
 *          * Tobias Jacobs - tobias.jacobs@neclab.eu
 *          * Flavio Cirillo - flavio.cirillo@neclab.eu
 *          * Raihan Ul-Islam
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
package com.semantix.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.springframework.stereotype.Service;

import com.semantix.model.SystemParameters;
import com.semantix.util.CommonMethods;

/**
 * Implementation of {@link Query subscriptions}
 *  based on a HyperSqlDb Server.
 */
@Service
public class QuerySubscriptionDAO {

	private static Logger logger = Logger
			.getLogger(QuerySubscriptionDAO.class);

	private  String username;
	private  String password;

	private String NAME_DB;

	private String port;
	private String URICONNECTION;

    Cache<Object, Object> cache;

	
	public QuerySubscriptionDAO(SystemParameters sp) throws FileNotFoundException {
		this.username = sp.getHsqldb_username();
		this.password = sp.getHsqldb_password();
		this.port = sp.getHsqldb_port();
		this.NAME_DB = sp.getHsqldb_name();
		URICONNECTION = "jdbc:hsqldb:hsql://localhost:" + port
			+ "/";
		
		cache = new DefaultCacheManager().getCache();
		
		boolean queryTableExists= checkTableExists("QUERY");
		if(!queryTableExists){
			resetDB();
			//createSchema("SemantixSchema");
			// if query table does not exists, subscription also cannot exist (subscriptions are linked to queries)
			createInitialQueryTable();	
			createInitialSubscriptionTable();
		}	
		registerInitialQueries("SparqlQueries.txt");
		initializeCache();
		
	}

	
	private void initializeCache() {

		// Add a entry
		cache.put("key", "value");
		
		cache.putIfAbsent("key", "newValue");

		// Validate the entry is now in the cache
		System.out.println(cache.size());
		System.out.println(cache.containsKey("key"));
		// Remove the entry from the cache
		Object v = cache.remove("key");
		// Validate the entry is no longer in the cache
		System.out.println("Value: " + v);
	}



	private void registerInitialQueries(String fileName) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(fileName));
        
    	while (sc.hasNext()) {
    		String s  = sc.next(); 
    		if(s.equalsIgnoreCase("QueryName:")){
    			String queryName = sc.next();
    			String query = "";
    			while(true){
    				String tmp = sc.nextLine();
    				if(tmp.equalsIgnoreCase("EndQuery")) break; // end of query
    				else if(tmp.equalsIgnoreCase("")) continue;
    				// query continues, add the new line
    				query+= " " + tmp;
    			}
    			String existingQuery = getQuery(queryName);
    			if(CommonMethods.isNullOrEmpty(existingQuery)){
        			registerQuery(query, queryName);
    			}
    		}
    	}
    	sc.close();
	}


	public String getQuery(String queryName) {
		String returnValue = null;
		PreparedStatement stmt = null;
		Connection c = null;
		ResultSet resultSet = null;
		try {

				Class.forName("org.hsqldb.jdbc.JDBCDriver");

			c = DriverManager.getConnection(URICONNECTION + NAME_DB, username,
					password);

			stmt = c.prepareStatement("SELECT QUERY FROM QUERY WHERE NAME=? AND IS_DELETED=FALSE");
			
			stmt.setString(1, queryName);
			resultSet = stmt.executeQuery();
			
			if(resultSet.next())	
				returnValue = resultSet.getString(1);

		} catch (SQLException e) {
			logger.info("SQLException",e);
		} catch (Exception e) {
			logger.info("ERROR: failed to load HSQLDB JDBC driver.",e);

		} finally {
			try {
				if (resultSet != null) {
					resultSet.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException e) {
				logger.info("SQL Exception", e);
			}

		}

		return returnValue;
	}
	
	public void createInitialQueryTable() {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			c = DriverManager.getConnection(URICONNECTION + NAME_DB, username,
					password);
			
			stmt = c.prepareStatement("CREATE TABLE IF NOT EXISTS QUERY(id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
					+ "name VARCHAR(128) UNIQUE, query VARCHAR(512),"
					+ "create_date DATETIME, is_deleted BOOLEAN);");
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			logger.info("SQLException",e);
		} catch (Exception e) {
			logger.info("ERROR: failed to load HSQLDB JDBC driver.",e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException e) {
				logger.info("SQL Exception", e);
			}
		}
	}

	
	public void createInitialSubscriptionTable() {

		Connection c = null;
		PreparedStatement stmt = null, stmt2=null;
		try {


			Class.forName("org.hsqldb.jdbc.JDBCDriver");


			c = DriverManager.getConnection(URICONNECTION + NAME_DB, username,
					password);
			
			stmt = c.prepareStatement("CREATE TABLE IF NOT EXISTS SUBSCRIPTION  (id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, "
					+ "subscribed_query_id INTEGER, check_interval_in_sec INTEGER, subscriber_server_URL VARCHAR(256),"
					+ "create_date DATETIME, is_deleted BOOLEAN );");
			stmt.executeUpdate();
			stmt2 = c.prepareStatement("ALTER TABLE SUBSCRIPTION ADD CONSTRAINT subscription_fk FOREIGN KEY (subscribed_query_id) REFERENCES QUERY (id) ON DELETE CASCADE;");
			stmt2.executeUpdate();



			
		} catch (SQLException e) {
			logger.info("SQLException",e);
		} catch (Exception e) {
			logger.info("ERROR: failed to load HSQLDB JDBC driver.",e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (stmt2 != null) {
					stmt2.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException e) {
				logger.info("SQL Exception", e);
			}
		}
	}

	

	
	private boolean checkTableExists(String tableName) {
		boolean retVal = false;
		Connection c = null;
		PreparedStatement stmt = null;
		ResultSet result = null;
		
		try {


			Class.forName("org.hsqldb.jdbc.JDBCDriver");


			c = DriverManager.getConnection(URICONNECTION + NAME_DB, "sa",
					"");
			
			stmt = c.prepareStatement("SELECT * FROM " + tableName + ";" );
			result = stmt.executeQuery();
			
			if(result!=null && result.next()){
				retVal = true;
			}

		} catch (SQLException e) {
			if(!e.getMessage().contains("object not found")){
			    // log only in the case of unexpected error occurs, object not found is an expected error as the table may not exist
				logger.info("SQLException",e);
			}
		} catch (Exception e) {
			logger.info("ERROR: failed to load HSQLDB JDBC driver.",e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException e) {
				logger.info("SQL Exception", e);
			}
		}
		return retVal;
	}

	
	
	public void resetDB() {
		Connection c = null;
		PreparedStatement stmt = null, stmt2 = null;

		try {

			Class.forName("org.hsqldb.jdbc.JDBCDriver");

			c = DriverManager.getConnection(URICONNECTION + NAME_DB, username,
					password);
			stmt2 = c.prepareStatement("DROP TABLE IF EXISTS SUBSCRIPTION;");
			stmt2.execute();
			stmt = c.prepareStatement("DROP TABLE IF EXISTS QUERY;");
			stmt.execute();

			
		


		} catch (SQLException e) {
			logger.error(e.toString());
		} catch (ClassNotFoundException e) {
			logger.error(e.toString());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (stmt2 != null) {
					stmt2.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException e) {
				logger.info("SQL Exception", e);
			}
		}
	}
	
	
	public void createSchema(String schemaName) {
		Connection c = null;
		PreparedStatement stmt = null;

		try {

			Class.forName("org.hsqldb.jdbc.JDBCDriver");

			c = DriverManager.getConnection(URICONNECTION + NAME_DB, username,
					password);

			stmt = c.prepareStatement("CREATE SCHEMA " + schemaName + ";");

			stmt.execute();

		} catch (SQLException e) {
			logger.error(e.toString());
		} catch (ClassNotFoundException e) {
			logger.error(e.toString());
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (c != null) {
					c.close();
				}
			} catch (SQLException e) {
				logger.info("SQL Exception", e);
			}
		}

	}


	public boolean registerQuery(String query, String name) {
		// return value is -1 in the case of errors, 0 in the case of successful saving, an integer in terms of already existing query
		boolean returnValue = false; 
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			c = DriverManager.getConnection(URICONNECTION + NAME_DB, username,
					password);
			
			
			stmt = c.prepareStatement("INSERT INTO QUERY (query, name, create_date, is_deleted) VALUES ('"  + query + "','" + name + "', CURRENT_TIMESTAMP , false);");
			stmt.executeUpdate();
			returnValue = true;
		
		} catch (SQLException e) {
			logger.info("SQLException",e);
		} catch (Exception e) {
			logger.info("ERROR: failed to load HSQLDB JDBC driver.",e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (c != null) {
					c.close();
				} 
			} catch (SQLException e) {
				logger.info("SQL Exception", e);
			}			
		}
		return returnValue;
	}
	
	

//	public void delete(String inID, String outID) {
//		PreparedStatement stmt = null;
//		Connection c = null;
//		try {
//
//
//				Class.forName("org.hsqldb.jdbc.JDBCDriver");
//
//
//			c = DriverManager.getConnection(URICONNECTION + NAME_DB, username,
//					password);
//
//			stmt = c.prepareStatement("DELETE FROM PUBLIC.PAIRSAV WHERE INID = ? AND AVID  = ?");
//
//			stmt.setString(1, inID);
//			stmt.setString(2, outID);
//
//			stmt.executeUpdate();
//
//		} catch (SQLException e) {
//			logger.info("SQLException",e);
//		} catch (Exception e) {
//			logger.info("ERROR: failed to load HSQLDB JDBC driver.",e);
//
//		} finally {
//			try {
//				if (stmt != null) {
//					stmt.close();
//				}
//				if (c != null) {
//					c.close();
//				}
//			} catch (SQLException e) {
//				logger.info("SQL Exception", e);
//			}
//
//		}
//
//	}
//
//
//	
//	public List<String> getInIDs(String outID) {
//
//		PreparedStatement stmt = null;
//		Connection c = null;
//		ResultSet result = null;
//		List<String> listInID = new ArrayList<String>();
//		try {
//
//
//				Class.forName("org.hsqldb.jdbc.JDBCDriver");
//
//			c = DriverManager.getConnection(URICONNECTION + NAME_DB, username,
//					password);
//
//			stmt = c.prepareStatement("SELECT INID FROM PUBLIC.PAIRSAV WHERE AVID = ?");
//
//			stmt.setString(1, outID);
//
//			result = stmt.executeQuery();
//
//			while (result.next()) {
//
//				listInID.add(result.getString(1).replaceAll("\\s", ""));
//
//			}
//
//		} catch (SQLException e) {
//			logger.info("SQLException",e);
//		} catch (Exception e) {
//			logger.info("ERROR: failed to load HSQLDB JDBC driver.",e);
//
//		} finally {
//			try {
//				if (result != null) {
//					result.close();
//				}
//				if (stmt != null) {
//					stmt.close();
//				}
//				if (c != null) {
//					c.close();
//				}
//			} catch (SQLException e) {
//				logger.info("SQL Exception", e);
//			}
//
//		}
//
//		return listInID;
//	}


}
