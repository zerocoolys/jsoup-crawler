package com.crawler.worker;

import org.jsoup.nodes.Document;

public class IdxDocument {

	private long id;

	private Document doc;

	public IdxDocument(long id, Document doc) {
		this.id = id;
		this.doc = doc;
	}

	public long getId() {
		return id;
	}

	public Document getDoc() {
		return doc;
	}

	
	
}
