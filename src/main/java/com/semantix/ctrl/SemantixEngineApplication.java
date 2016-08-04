package com.semantix.ctrl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.semantix.database.HyperSqlDbServer;
import com.semantix.database.QuerySubscriptionDAO;
import com.semantix.model.SystemParameters;

@SpringBootApplication
public class SemantixEngineApplication {
	
	public static SystemParameters sp;
	
	public static void main(String[] args) throws FileNotFoundException {
		ApplicationContext ctx =  SpringApplication.run(SemantixEngineApplication.class, args); 
		setSystemParameters("SystemParameters.txt");
		
		// start DB server
		new HyperSqlDbServer(sp);
		
		
		QuerySubscriptionDAO querySubscriptionDAO = new QuerySubscriptionDAO(sp);
		SparqlCtrl sparqlCtrl = new SparqlCtrl(sp);
        RestCtrl restCtrl = ctx.getBean(RestCtrl.class);
        restCtrl.setQuerySubscriptionDAO(querySubscriptionDAO);
        restCtrl.setSp(sp);
        restCtrl.setSparqlCtrl(sparqlCtrl);

        
	}
	
	

    



	public static void setSystemParameters(String s) throws FileNotFoundException{
        // set default values
		String uname="sa", password="", hsqldb_port="9001", sparql_port="3030",  sparql_URL="http://localhost",dbname="mainDB", 
				dir="file:\\C:\\SemantixEngine\\SQL_database\\mainDB";
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
    	}
    
    	sp = new SystemParameters(hsqldb_port, sparql_port,sparql_URL, uname, password,dbname,dir,subscribeTimeInterval);
    	sc.close();
    }
}
