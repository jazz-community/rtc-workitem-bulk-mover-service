package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

public class AttributeValue {
	private String identifier;
	private String displayName;

	public AttributeValue(String id, String dn) {
		this.identifier = id;
		this.displayName = dn;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}
