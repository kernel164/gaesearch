package org.gaesearch.model.jdo;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

@PersistenceCapable
// http://timyang.net/programming/thrift-protocol-buffers-performance-2/
public class SearchIndex {
	@PrimaryKey
	@Persistent
	String token;

	@Persistent(serialized = "true")
	HashMultimap<String, Long> content;

	@Override
	public String toString() {
		return token + " : content - " + content;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Multimap<String, Long> getContent() {
		return content;
	}

	public void addContent(String type, Long refId) {
		if (this.content == null) {
			this.content = HashMultimap.create();
		}
		this.content.put(type, refId);

		// pb;
		//try {
		//	contents = Contents.parseFrom(content);
		//} catch (InvalidProtocolBufferException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}

		//List<Content> contentList = contents.getContentsList();
		//List<Long> refIds = contentList.get(0).getReferenceIdList();

	}

	public void removeContent(String type, Long refId) {
		this.content.remove(type, refId);
	}

	public boolean isEmpty() {
		return this.content != null && this.content.size() == 0;
	}

	public boolean typeExists(String type) {
		return this.content != null && this.content.containsKey(type);
	}

	public boolean referenceExists(Long refId) {
		return this.content != null && this.content.containsValue(refId);
	}
}
