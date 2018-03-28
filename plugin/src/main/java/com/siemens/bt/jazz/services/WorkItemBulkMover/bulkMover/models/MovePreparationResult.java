package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import java.util.Collection;
import java.util.List;

import com.ibm.team.workitem.common.model.IWorkItem;

/**
 * wrapper class holding all neccessary data for move service to return back to the caller
 */
public class MovePreparationResult {
	private List<IWorkItem> workItems;
	private Collection<AttributeDefinition> attributeDefinitions;
	
	public MovePreparationResult(List<IWorkItem> workItems, Collection<AttributeDefinition> attributeDefinitions) {
		this.workItems = workItems;
		this.attributeDefinitions = attributeDefinitions;
	}
	
	public List<IWorkItem> getWorkItems() {
		return workItems;
	}
	
	public Collection<AttributeDefinition> getAttributeDefinitions() {
		return attributeDefinitions;
	}
}
