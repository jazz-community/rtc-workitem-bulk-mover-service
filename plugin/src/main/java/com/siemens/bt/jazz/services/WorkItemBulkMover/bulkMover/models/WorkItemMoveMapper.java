package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import com.ibm.team.workitem.common.model.IWorkItem;

import java.util.ArrayList;
import java.util.List;

/**
 * While preparing the move, the Bulk Mover saves both the old and new work item into a collection
 * This class represents one work item, containing the initial (source) and the target state
 */
public class WorkItemMoveMapper {
	private int workItemId;
	private IWorkItem sourceWorkItem;
	private IWorkItem targetWorkItem;
	
	public WorkItemMoveMapper(IWorkItem sourceWorkItem, IWorkItem targetWorkItem) {
		this.sourceWorkItem = sourceWorkItem;
		this.targetWorkItem = targetWorkItem;
		this.workItemId = sourceWorkItem.getId();
	}
	
	public int getId() {
		return workItemId;
	}
	
	public IWorkItem getSourceWorkItem() {
		return sourceWorkItem;
	}
	
	public IWorkItem getTargetWorkItem() {
		return targetWorkItem;
	}


    public static List<IWorkItem> getAllTargetWorkItems(List<WorkItemMoveMapper> mappedWorkItems) {
        List<IWorkItem> workItems = new ArrayList<IWorkItem>();
        for(WorkItemMoveMapper item : mappedWorkItems) {
            workItems.add(item.getTargetWorkItem());
        }
        return workItems;
    }
}
