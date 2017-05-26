package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import java.util.List;

import com.ibm.team.workitem.common.model.IWorkItem;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.collections.AttributeDefinitions;

/**
 * wrapper class holding all neccessary data for move service to return back to the caller
 */
public class MovePreparationResult {
	private List<IWorkItem> workItems;
	private AttributeDefinitions attributeDefinitions;
	
	public MovePreparationResult(List<IWorkItem> workItems, AttributeDefinitions attributeDefinitions) {
		this.workItems = workItems;
		this.attributeDefinitions = attributeDefinitions;
	}
	
	public List<IWorkItem> getWorkItems() {
		return workItems;
	}
	
	public AttributeDefinitions getAttributeDefinitions() {
		return attributeDefinitions;
	}
}
