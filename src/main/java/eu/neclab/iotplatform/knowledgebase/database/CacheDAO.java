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
