package eu.neclab.iotplatform.knowledgebase.util;

import eu.neclab.iotplatform.knowledgebase.model.QueryModel;

public class CommonMethods {
	public static boolean isNullOrEmpty(String s){
		if(s == null || s.equalsIgnoreCase(""))
			return true;
		else return false;
	}
	
	
	public static String prepareQuery(QueryModel qm, String entityType){
		String query = qm.getQuery();
		if(query.contains("<???>")){
			return query.replace("<???>", entityType); 
		}
		else{
			return query;
		}
	}
	
}
