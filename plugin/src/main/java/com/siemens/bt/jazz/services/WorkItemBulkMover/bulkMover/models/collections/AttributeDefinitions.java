package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.collections;

import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeDefinition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a collection of AttributeDefinitions
 */
public class AttributeDefinitions {
	private Map<String, AttributeDefinition> attributeDefinitions;
	
	public AttributeDefinitions() {
		 this.attributeDefinitions = new HashMap<String, AttributeDefinition>();
	}
	
	public Collection<AttributeDefinition> getAttributeDefinitionCollection() {

		return attributeDefinitions.values();
	}

	public boolean contains(String identifier) {
	    return attributeDefinitions.containsKey(identifier);
    }
    public AttributeDefinition get(String identifier) {
        return attributeDefinitions.get(identifier);
    }

	public void add(AttributeDefinition attributeDefinition) {
		attributeDefinitions.put(attributeDefinition.getIdentifier(), attributeDefinition);
	}
}
