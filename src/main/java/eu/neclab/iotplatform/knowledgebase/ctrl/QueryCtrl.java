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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.neclab.iotplatform.knowledgebase.database.CacheDAO;
import eu.neclab.iotplatform.knowledgebase.database.QuerySubscriptionDAO;
import eu.neclab.iotplatform.knowledgebase.model.QueryModel;
import eu.neclab.iotplatform.knowledgebase.model.SystemParameters;
import eu.neclab.iotplatform.knowledgebase.util.CommonMethods;


@Component
public class QueryCtrl {



    private SystemParameters sp;
    private QuerySubscriptionDAO querySubscriptionDAO;
    private SparqlCtrl sparqlCtrl;
    private CacheDAO queryCacheDAO;

    
	
	@Scheduled(initialDelay=15000, fixedRate=10000) // update cache every 10 seconds
    public void updateCacheForQueries() {
		List<String> currentKeyList = queryCacheDAO.getKeyList();
		if(currentKeyList.size() > sp.getMax_cache_entries()){ // check if the max cache size is exceeded
			queryCacheDAO.clearCache();
			return;
		}
		for(int i=0;i<currentKeyList.size();i++){ // update each entry in the query cache
			String key = currentKeyList.get(i);
			int divisorIndex = key.indexOf("_");
			String queryName= key.substring(0, divisorIndex);
			String entityType = key.substring(divisorIndex+1);
			String cacheResponse = queryCacheDAO.queryCacheForSparqlResponse(queryName, entityType);
			QueryModel qm  =querySubscriptionDAO.getQueryModel(queryName);
			String query="";
			if(qm == null){
				// the query does not exist anymore in the database, remove the cache entry too
				queryCacheDAO.remove(key); 
				currentKeyList.remove(i);
				i--; // set the iterator to correct value
				continue; // check other cache entries
			}
			else if(CommonMethods.isNullOrEmpty(entityType)){
				query = qm.getQuery();
			}
			else{ // if the query has an entityType
				query = qm.getQuery();
				query = query.replace("<???>", entityType); 
			}
			
			try {
				StringBuffer sparqlResponse = sparqlCtrl.sendSparqlPost(sp.getSparql_URL() + ":" + sp.getSparql_port() + "/Subscription/query", "query=" + query);
				if(sparqlResponse== null || CommonMethods.isNullOrEmpty(sparqlResponse.toString())){
					// the query result does not exist anymore in the SPARQL database, remove the cache entry too
					queryCacheDAO.remove(key); 
					queryCacheDAO.removeKey(key);
					continue; // check other cache entries

				}
				else if(sparqlResponse.toString().equalsIgnoreCase(cacheResponse)){ // SPARQL result is the same as the cache, so don`t need to do anything
					continue;
				}
				else{ // SPARQL response has been changed. Changed the cache entry too
					queryCacheDAO.putSparqlResponseToCache(queryName, entityType, sparqlResponse.toString());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	
	

	public void setSp(SystemParameters sp) {
		this.sp = sp;
	}

	public void setQuerySubscriptionDAO(QuerySubscriptionDAO querySubscriptionDAO) {
		this.querySubscriptionDAO = querySubscriptionDAO;
	}

	public void setSparqlCtrl(SparqlCtrl sparqlCtrl) {
		this.sparqlCtrl = sparqlCtrl;
	}



	public void setQueryCacheDAO(CacheDAO queryCacheDAO) {
		this.queryCacheDAO = queryCacheDAO;
	}

	
	
	
	// HTTP Post request
		public StringBuffer postUpdateToSubscriber(String url, String urlParameters) throws Exception {

			byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
			int    postDataLength = postData.length;
			URL obj = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) obj.openConnection();

			conn.setDoOutput( true );
			conn.setInstanceFollowRedirects( false );
			conn.setRequestMethod( "POST" );
			conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded"); 
			conn.setRequestProperty( "charset", "utf-8");
			conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
			conn.setUseCaches( false );
			try( DataOutputStream wr = new DataOutputStream( conn.getOutputStream())) {
			    wr.write( postData );
				wr.flush();
				wr.close();
			}
			int responseCode = conn.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();		
			return response;
		}
	
	
}
