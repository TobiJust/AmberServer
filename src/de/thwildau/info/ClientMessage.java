package de.thwildau.info;

import java.io.Serializable;

public class ClientMessage implements Serializable{
	
	private static final long serialVersionUID = 5241215198936208524L;

	public static enum Ident {
		EVENT_REQUEST, INFO_ALL, TEXT_MESSAGE, LOGIN, ERROR, REGISTER, QUERY, EVENT
	}
	
	private Ident id;
	private Object content;

	public ClientMessage(Ident id, Object content){
		this.setId(id);
		this.setContent(content);
	}

	public Ident getId() {
		return id;
	}

	public void setId(Ident id) {
		this.id = id;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}
	
}