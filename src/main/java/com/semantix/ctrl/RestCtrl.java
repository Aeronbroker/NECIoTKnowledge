package com.semantix.ctrl;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.semantix.database.QuerySubscriptionDAO;
import com.semantix.model.QueryRequest;
import com.semantix.model.SystemParameters;
import com.semantix.util.CommonMethods;

@RestController
public class RestCtrl {
	
	public SystemParameters sp;
	
	QuerySubscriptionDAO querySubscriptionDAO;
	SparqlCtrl sparqlCtrl;
	
    //private static final String template = "Hello, %s!";
    //private final AtomicLong counter = new AtomicLong();
      
   
    

	@RequestMapping(value="/forward", method = RequestMethod.POST)
    public @ResponseBody String forward(@RequestParam(value="query", defaultValue="") String query) {
		StringBuffer response = null;
		try {
			response = sparqlCtrl.sendSparqlPost(sp.getSparql_URL() + ":" + sp.getSparql_port() + "/Subscription/query", "query="+ query);
			//System.out.println(response);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();
	}
	

	@RequestMapping(value="/registerquery", method = RequestMethod.POST)
    public @ResponseBody String saveQuery(@RequestBody QueryRequest reqBody) {
		boolean querySaved=false; 
		String returnMessage;
		try {
			querySaved = querySubscriptionDAO.registerQuery(reqBody.getQuery(),reqBody.getQueryName());
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(!querySaved)
				returnMessage= "Error: Problem encountered during registeration. The query name might already exist, please use a unique query name.";
			else 
				returnMessage= "Success: The query is succesfully registered. Query name: " + reqBody.getQueryName();
		}
		return returnMessage;
	}
	
	
	@RequestMapping(value="/query", method = RequestMethod.GET)
    public @ResponseBody String saveQuery(@RequestParam(value="request", defaultValue="") String queryName, 
    		@RequestParam(value="entityType", defaultValue="") String entityType) {
		StringBuffer response = null;
		try {
			if(queryName.equalsIgnoreCase("")){
				// every query must have a request type
				response = new StringBuffer("Error: Please enter a request name! (e.g., getSubTypes, getSuperTypes, getAttributes)");				
			}
			else{
				String query  =querySubscriptionDAO.getQuery(queryName);
				if(CommonMethods.isNullOrEmpty(entityType)){
					if(query.contains("<???>")){
						response = new StringBuffer("Error: Query you requested is missing a parameter (e.g., entity type)!");
					}
					else{
					// a query with no entity id, just send the query directly to the Sparql server
					response = sparqlCtrl.sendSparqlPost(sp.getSparql_URL() + ":" + sp.getSparql_port() + "/Subscription/query", "query=" + query);
					}
				}
				else{ // there exists an entity type, replace the <???> part of the query with the entity type
					if(!query.contains("<???>")){
						// if there is an entity type, the corresponding query MUST contain <???> to replace with the entity type
						response = new StringBuffer("Error: Query you requested does not contain an Entity Type field!");				
					}
					else{ // there exists one (or multiple) <???> fields in the query, replace them with the provided entity type
						query = query.replace("<???>", entityType); 
						response = sparqlCtrl.sendSparqlPost(sp.getSparql_URL() + ":" + sp.getSparql_port() + "/Subscription/query", "query=" + query);
					}
					
				}
			}

		
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();		
	}
	
	
	
//	@RequestMapping("/createRDF")
//    public ResponseMessage greeting(@RequestParam(value="resource", defaultValue="http://localhost") String name, 
//    						@RequestParam(value="property", defaultValue="vCard") String propertyName,
//    						@RequestParam(value="value", defaultValue="John Smith") int temp
//    		) {
//        return new ResponseMessage(counter.incrementAndGet(),
//                            String.format(template, name), propertyName, temp);
//    }
//	
//    
//    
//    @RequestMapping("/test")
//    public String test() {
//    	
//    	
//    	return "hi";
//    }





	public SystemParameters getSp() {
		return sp;
	}





	public void setSp(SystemParameters sp) {
		this.sp = sp;
	}





	public QuerySubscriptionDAO getQuerySubscriptionDAO() {
		return querySubscriptionDAO;
	}





	public void setQuerySubscriptionDAO(QuerySubscriptionDAO subscriptionDAO) {
		this.querySubscriptionDAO = subscriptionDAO;
	}





	public SparqlCtrl getSparqlCtrl() {
		return sparqlCtrl;
	}





	public void setSparqlCtrl(SparqlCtrl sparqlCtrl) {
		this.sparqlCtrl = sparqlCtrl;
	}
    

    

}
