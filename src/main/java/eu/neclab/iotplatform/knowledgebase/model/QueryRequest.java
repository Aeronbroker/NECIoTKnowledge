package eu.neclab.iotplatform.knowledgebase.model;

public class QueryRequest {

	String queryName;
	String query;

	
	
	public QueryRequest(String queryName, String query) {
		super();
		this.queryName = queryName;
		this.query = query;
	}
	public String getQueryName() {
		return queryName;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	
	
}
