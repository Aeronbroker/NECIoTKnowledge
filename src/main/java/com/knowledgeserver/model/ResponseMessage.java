package com.knowledgeserver.model;

public class ResponseMessage {
    private final long id;
    private final String content;
  
    public ResponseMessage(long id, String content) {
        this.id = id;
        this.content = content;
     }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }


    
}
