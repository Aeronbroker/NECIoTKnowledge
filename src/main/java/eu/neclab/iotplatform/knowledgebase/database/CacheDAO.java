package eu.neclab.iotplatform.knowledgebase.database;

import java.util.ArrayList;
import java.util.List;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;

import eu.neclab.iotplatform.knowledgebase.model.SystemParameters;

public class CacheDAO {

	// KEY: queryName+entityType  VALUE: SPARQL server JSON response
	Cache<String, String> cache;
	List<String> keyList;
	SystemParameters sp;
	
	
	public CacheDAO(SystemParameters sp, DefaultCacheManager dcm) {
		super();
		// initialize cache
		this.cache = dcm.getCache();
		this.keyList = new ArrayList<String>();
		this.sp=sp;
	}


	public String queryCacheForSparqlResponse(String queryName, String entityType) {
		// key consists of query name + entity type + (optionally) other parameters in order
		return cache.get(queryName + "_" + entityType);	
	}
	
	public String putSparqlResponseToCache(String queryName,String entityType, String initialSparqlResponse) {
		if(cache.containsKey(queryName + "_" + entityType)){ // just to make sure it doesn`t exist, if someone else put at this time, replace
			return cache.replace(queryName +"_" + entityType, initialSparqlResponse);
		}
		// key consists of query name + entity type + (optionally) other parameters in order
		addKey(queryName + "_"+ entityType);
		return cache.putIfAbsent(queryName + "_"+ entityType, initialSparqlResponse);	// cache must have unique keys
	
	}
	
	public String updateSparqlResponse(String queryName, String entityType, String newSparqlResponse) {
		// key consists of query name + entity type + (optionally) other parameters in order
		return cache.replace(queryName +"_" + entityType, newSparqlResponse);	
	}		
	
	public void clearCache(){
		cache.clear();
		keyList.clear();
	}


	public void remove(String key){
		cache.remove(key);
	}

	
	public List<String> getKeyList() {
		return keyList;
	}


	public void addKey(String newKey) {
		keyList.add(newKey);
	}	

	public void removeKey(String key) {
		keyList.remove(key);
	}	
}
