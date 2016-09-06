package eu.neclab.iotplatform.knowledgebase.ctrl;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.neclab.iotplatform.knowledgebase.database.CacheDAO;
import eu.neclab.iotplatform.knowledgebase.database.QuerySubscriptionDAO;
import eu.neclab.iotplatform.knowledgebase.model.QueryModel;
import eu.neclab.iotplatform.knowledgebase.model.QueryRequest;
import eu.neclab.iotplatform.knowledgebase.model.SystemParameters;
import eu.neclab.iotplatform.knowledgebase.util.CommonMethods;

@RestController
public class RestCtrl {
	
	public SystemParameters sp;
	
	QuerySubscriptionDAO querySubscriptionDAO;
	SparqlCtrl sparqlCtrl;
    CacheDAO queryCacheDAO;

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
			if(CommonMethods.isNullOrEmpty(queryName)){
				// every query must have a request type
				response = new StringBuffer("Error: Please enter a query request name! (e.g., getSubTypes, getSuperTypes, getAttributes)");				
			}
			else{
				QueryModel qm  =querySubscriptionDAO.getQueryModel(queryName);
				if(qm == null){
					response = new StringBuffer("Error: Query you requested does not exist! Please register the query.");
				}
				else if(CommonMethods.isNullOrEmpty(entityType)){
					String query = qm.getQuery();
					if(query.contains("<???>")){
						response = new StringBuffer("Error: Query you requested is missing a parameter (e.g., entity type)!");
					}
					else{
						String cacheResponse = queryCacheDAO.queryCacheForSparqlResponse(queryName, "");
						if(cacheResponse == null){
							// a query with no entity id, just send the query directly to the Sparql server						
							response = sparqlCtrl.sendSparqlPost(sp.getSparql_URL() + ":" + sp.getSparql_port() + "/Subscription/query", "query=" + query);
							queryCacheDAO.putSparqlResponseToCache(queryName, "", response.toString());
						}
						else{ // the response is already in the cache, serve it quickly
							response = new StringBuffer(cacheResponse);
						}
					}
				}
				else{ // there exists an entity type, replace the <???> part of the query with the entity type
					String query = qm.getQuery();
					if(!query.contains("<???>")){
						// if there is an entity type, the corresponding query MUST contain <???> to replace with the entity type
						response = new StringBuffer("Error: Query you requested does not contain an Entity Type field!");				
					}
					else{ // there exists one (or multiple) <???> fields in the query, replace them with the provided entity type
						query = query.replace("<???>", entityType); 
						
						String cacheResponse = queryCacheDAO.queryCacheForSparqlResponse(queryName, entityType);
						if(cacheResponse == null){
							// a query with no entity id, just send the query directly to the Sparql server						
							response = sparqlCtrl.sendSparqlPost(sp.getSparql_URL() + ":" + sp.getSparql_port() + "/Subscription/query", "query=" + query);
							queryCacheDAO.putSparqlResponseToCache(queryName, entityType, response.toString());
						}
						else{ // the response is already in the cache, serve it quickly
							response = new StringBuffer(cacheResponse);
						}
					}
					
				}
			}		
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();		
	}
	
	
	
	
	@RequestMapping(value="/subscribe", method = RequestMethod.POST)
    public @ResponseBody String subscribeQuery(@RequestParam(value="request", defaultValue="") String queryName, 
    		@RequestParam(value="entityType", defaultValue="") String entityType,@RequestBody String subscriberURL) {
		StringBuffer response = null;
		boolean subscriptionSaved=false; 

		try {
			if(CommonMethods.isNullOrEmpty(queryName)){
				// every query must have a request type
				response = new StringBuffer("Error: Please enter a query name for subscription! (e.g., getSubTypes, getSuperTypes, getAttributes)");				
			}
			else if(CommonMethods.isNullOrEmpty(subscriberURL)){
				// every subscription request must have a URL 
				response = new StringBuffer("Error: Please enter a subscriberURL for subscription!");				
			}
			else{
				QueryModel qm  =querySubscriptionDAO.getQueryModel(queryName);
				if(qm == null){
					response = new StringBuffer("Error: Query you requested to subscribe does not exist! Please register the query.");
				}
				if(CommonMethods.isNullOrEmpty(entityType)){
					String query = qm.getQuery();
					if(query.contains("<???>")){
						response = new StringBuffer("Error: Subscription you requested is missing a parameter (e.g., entity type)!");
					}
					else{
						// a query with no entity id, you can subscribe to this query
						subscriptionSaved = querySubscriptionDAO.registerSubscription(queryName, entityType, subscriberURL);
						if(subscriptionSaved){
							response = new StringBuffer("Success: The subscription is succesfully registered. Query name: " +  queryName +
									" (Query with no entity type).");
						}
						else{
							response = new StringBuffer("Error: The subscription could not be registered. It may already exist or the query may not exist.");
						}
					}
				}
				else{ // there exists an entity type provided by user, replace the <???> part of the query with the entity type
					String query = qm.getQuery();
					if(!query.contains("<???>")){
						// if there is an entity type, the corresponding query MUST contain <???> to replace with the entity type
						response = new StringBuffer("Error: Subscription you requested does not contain an Entity Type field!");				
					}
					else{
						// a query with entity id, you can subscribe to this query
						subscriptionSaved = querySubscriptionDAO.registerSubscription(queryName, entityType, subscriberURL);	
						if(subscriptionSaved){
							response = new StringBuffer("Success: The subscription is succesfully registered. Query name: " +  queryName +
									" (Query with no entity type).");
						}
						else{
								response = new StringBuffer("Error: The subscription could not be registered. It may already exist or the query may not exist.");
						}
						
					}
					
				}
			}

		
			
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();		
	}

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





	public CacheDAO getQueryCacheDAO() {
		return queryCacheDAO;
	}


	public void setQueryCacheDAO(CacheDAO queryCacheDAO) {
		this.queryCacheDAO = queryCacheDAO;
	}


	public SparqlCtrl getSparqlCtrl() {
		return sparqlCtrl;
	}





	public void setSparqlCtrl(SparqlCtrl sparqlCtrl) {
		this.sparqlCtrl = sparqlCtrl;
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


}
