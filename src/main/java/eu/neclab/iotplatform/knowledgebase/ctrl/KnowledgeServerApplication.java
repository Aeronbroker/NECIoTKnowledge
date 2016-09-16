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

package eu.neclab.iotplatform.knowledgebase.ctrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import eu.neclab.iotplatform.knowledgebase.database.CacheDAO;
import eu.neclab.iotplatform.knowledgebase.database.HyperSqlDbServer;
import eu.neclab.iotplatform.knowledgebase.database.QuerySubscriptionDAO;
import eu.neclab.iotplatform.knowledgebase.model.SystemParameters;

@SpringBootApplication
@EnableScheduling
public class KnowledgeServerApplication {
	
	public static SystemParameters sp;
	
	public static void main(String[] args) throws FileNotFoundException {

		setSystemParameters("SystemParameters.txt");
		
		ApplicationContext ctx =  SpringApplication.run(KnowledgeServerApplication.class, args); 

		
		// initialize cache
		GlobalConfiguration globalConfig = new GlobalConfigurationBuilder()
				  .globalJmxStatistics()
				  .allowDuplicateDomains(true)
				  .build();
		DefaultCacheManager dcm = new DefaultCacheManager(globalConfig);
		
		// start DB server (HSQLDB)
		new HyperSqlDbServer(sp);
		
		// Create connections to databases and cache
		QuerySubscriptionDAO querySubscriptionDAO = new QuerySubscriptionDAO(sp);
		SparqlCtrl sparqlCtrl = new SparqlCtrl(sp);
		CacheDAO cacheDAO = new CacheDAO(sp, dcm);
		
		// rest interface for HTTP requests and responses
        RestCtrl restCtrl = ctx.getBean(RestCtrl.class);
        restCtrl.setSp(sp);
        restCtrl.setQuerySubscriptionDAO(querySubscriptionDAO);
        restCtrl.setSparqlCtrl(sparqlCtrl);  
        restCtrl.setQueryCacheDAO(cacheDAO);
        
    	// scheduled subscription tasks !!
		QueryCtrl queryCtrl = ctx.getBean(QueryCtrl.class);
		queryCtrl.setSp(sp);
		queryCtrl.setQuerySubscriptionDAO(querySubscriptionDAO);
		queryCtrl.setSparqlCtrl(sparqlCtrl);
		queryCtrl.setQueryCacheDAO(cacheDAO);
		
        

		// scheduled subscription tasks !!
		SubscriptionCtrl subsCtrl = ctx.getBean(SubscriptionCtrl.class);
		subsCtrl.setSp(sp);
		subsCtrl.setQuerySubscriptionDAO(querySubscriptionDAO);
		subsCtrl.setSparqlCtrl(sparqlCtrl);
		subsCtrl.setInitialSubscriptionCache(dcm);
		
        
	}

	public static void setSystemParameters(String s) throws FileNotFoundException{
        // set default values
		String uname="sa", password="", hsqldb_port="9001", sparql_port="3030", sparql_URL="http://localhost",dbname="mainDB", 
				dir="file:\\C:\\SemantixEngine\\SQL_database\\mainDB";
        int maxCacheEntries=1000;
		double subscribeTimeInterval=10;
        
        Scanner sc = new Scanner(new File(s));
    	while (sc.hasNext()) {
    		String tmp = sc.next();
    		if(tmp.equalsIgnoreCase("hsqldb_port")){
    			hsqldb_port = sc.next();
    		}
    		else if(tmp.equalsIgnoreCase("sparql_port")){
    			sparql_port = sc.next();
    		}		
    		else if(tmp.equalsIgnoreCase("sparql_URL")){
    			sparql_URL = sc.next();
    		}
    		else if(tmp.equalsIgnoreCase("hsqldb_username")){
    			uname = sc.next();
    		}
    		else if(tmp.equalsIgnoreCase("hsqldb_password")){
    			password = ""; 
    		}
    		else if(tmp.equalsIgnoreCase("hsqldb_dbname")){
    			dbname = sc.next();
    		}
    		else if(tmp.equalsIgnoreCase("hsqldb_directory")){
    			dir = sc.next();
    		}
    		else if(tmp.equalsIgnoreCase("subscribe_time_interval_seconds")){
    			subscribeTimeInterval = Double.parseDouble(sc.next());
    		}
    		else if(tmp.equalsIgnoreCase("max_cache_entries")){
    			maxCacheEntries = Integer.parseInt(sc.next());
    		}

    	}
    
    	sp = new SystemParameters(hsqldb_port, sparql_port,sparql_URL, uname, password,dbname,dir,subscribeTimeInterval, maxCacheEntries);
    	sc.close();
    }
}

