package com.knowledgeserver.model;

import java.util.Date;

public class SubscribeModel {

	int id;
	QueryModel subscribedQuery;
	String subscribedServerURL;
	String entityType;
	Date createDate;
	
	public SubscribeModel(int id, String subscribedServerURL, String entityType,
			QueryModel subscribedQuery) {
		super();
		this.id = id;
		this.subscribedServerURL = subscribedServerURL;
		this.entityType = entityType;
		this.subscribedQuery = subscribedQuery;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSubscribedServerURL() {
		return subscribedServerURL;
	}
	public void setSubscribedServerURL(String subscribedServerURL) {
		this.subscribedServerURL = subscribedServerURL;
	}
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public QueryModel getSubscribedQuery() {
		return subscribedQuery;
	}

	public void setSubscribedQuery(QueryModel subscribedQuery) {
		this.subscribedQuery = subscribedQuery;
	}

}
