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

import org.apache.log4j.Logger;
import org.infinispan.manager.DefaultCacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eu.neclab.iotplatform.knowledgebase.database.CacheDAO;
import eu.neclab.iotplatform.knowledgebase.database.QuerySubscriptionDAO;
import eu.neclab.iotplatform.knowledgebase.model.SubscribeModel;
import eu.neclab.iotplatform.knowledgebase.model.SystemParameters;
import eu.neclab.iotplatform.knowledgebase.util.CommonMethods;


@Component
public class SubscriptionCtrl {

	private static Logger logger = Logger
			.getLogger(SubscriptionCtrl.class);

	    private SystemParameters sp;
    private QuerySubscriptionDAO querySubscriptionDAO;
    private SparqlCtrl sparqlCtrl;
    private CacheDAO subscriptionCache;

    
	
	@Scheduled(initialDelay=20000, fixedRate=60000)
    public void serveSubscriptions() {
		List<SubscribeModel> subsList = querySubscriptionDAO.getAllSubscriptions();
		for(int i=0;i<subsList.size();i++){		
			// check if the result exists in the cache
			SubscribeModel sm = subsList.get(i);
			if(sm.getSubscribedQuery() == null){
				logger.info("ERROR: The subscription does not correspond to any query and it will be removed. Subscription ID:" + sm.getId());
				boolean isDeleted= querySubscriptionDAO.deleteSubscription(sm.getId());
				if(isDeleted) 
					logger.info("INFO: The subscription is deleted. Subscription ID: "  + sm.getId());
				else
					logger.info("ERROR: Could not delete the subscription. Subscription ID: "  + sm.getId());
				continue; // skip this subscription as it is deleted
			}
				String sparqlQuery = CommonMethods.prepareQuery(sm.getSubscribedQuery(), sm.getEntityType());
				String prevSparqlResponse = subscriptionCache.queryCacheForSparqlResponse(sm.getSubscribedQuery().getName(), sm.getEntityType());
				
				if(CommonMethods.isNullOrEmpty(prevSparqlResponse)){ 
					// this subscription has not been served before, go to SPARQL server for the response
					// if not, send request to SPARQL, send an HTTP POST result to subscriber and put it to cache 
					// this is the first time that this particular subscription is being served
					
					// get SPARQL response
					StringBuffer response= null;
					try{
						response = sparqlCtrl.sendSparqlPost(sp.getSparql_URL() + ":" + sp.getSparql_port() + "/Subscription/query", "query=" + sparqlQuery);
					
						// send HTTP request for updating subscriber (on change update) 
					
						postUpdateToSubscriber(sm.getSubscribedServerURL(), "update="+ response.toString());
					}
					catch(Exception e) {
						// TODO Auto-generated catch block
						if(response == null) continue;
						logger.info("WARNING: Subscriber server is not responding. Subscriber URL:" + sm.getSubscribedServerURL());
						// we do not remove the subscription so that if the subscriber becomes alive again, we can continue sending the subscriptions
					}
				
					// put the value in the cache for future comparisons
					if(response != null){
						subscriptionCache.putSparqlResponseToCache(sm.getSubscribedQuery().getName(), sm.getEntityType(), response.toString());
					}
				}

				else{ // we have the previous SPARQL response
					// this subscription has been served before, check if the previous response is same with the current SPARQL response
					StringBuffer newSparqlResponseBuffer=null;
					try {
						newSparqlResponseBuffer = sparqlCtrl.sendSparqlPost(sp.getSparql_URL() + ":" + sp.getSparql_port() + "/Subscription/query", "query="+ sparqlQuery);
					} 
					catch (Exception e1) {
						logger.info("ERROR: SPARQL server is not responding.");
						continue;
					}
					String newSparqlResponse = newSparqlResponseBuffer.toString();
					if(!newSparqlResponse.equalsIgnoreCase(prevSparqlResponse)){ 
						// there is a change in the SPARQL response, we have to notify the subscriber about the update
						// send HTTP request for updating subscriber (on change update) 
						try{
							postUpdateToSubscriber(sm.getSubscribedServerURL(), "update="+ newSparqlResponse);
						}
						catch(Exception e) {
							// TODO Auto-generated catch block
							logger.info("WARNING: Subscriber server is not responding. Subscriber URL:" + sm.getSubscribedServerURL());
						}
						// update the cache with the new SPARQL response				
						subscriptionCache.updateSparqlResponse(sm.getSubscribedQuery().getName(),sm.getEntityType(),newSparqlResponse);
					} // else, no change happened. no need to take any action
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



	public void setInitialSubscriptionCache(DefaultCacheManager dcm) {
		this.subscriptionCache = new CacheDAO(sp,dcm);
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
