package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

public class AttributeValue {
	private String identifier;
	private JsonElement displayName;

	public AttributeValue(String id, JsonPrimitive dn) {
		this.identifier = id;
		this.displayName = dn;
	}

	public AttributeValue(String id, String dn) {
		this.identifier = id;
		this.displayName = new JsonPrimitive(dn);
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public JsonPrimitive getDisplayName() {
		return displayName == null || displayName instanceof JsonNull ? null : (JsonPrimitive) displayName;
	}
}
