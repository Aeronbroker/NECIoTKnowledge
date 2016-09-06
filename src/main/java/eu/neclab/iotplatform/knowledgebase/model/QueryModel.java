package eu.neclab.iotplatform.knowledgebase.model;

import java.util.Date;

public class QueryModel {
	
	int id; 
	String name;
	String query;
	Date createDate;
	public QueryModel(int id, String name, String query) {
		super();
		this.id = id;
		this.name = name;
		this.query = query;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	
	
	
	
	
}
