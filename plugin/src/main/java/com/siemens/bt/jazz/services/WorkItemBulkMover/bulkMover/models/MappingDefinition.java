package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models.WorkItem;

import java.util.ArrayList;
import java.util.List;

public class MappingDefinition {
	private AttributeValue oldValue;
	private List<AffectedWorkItem> affectedWorkItems;
    private String chosen;
	private boolean showDetails = false;

	public MappingDefinition(AttributeValue oldValue, AffectedWorkItem affectedWorkItem) {
	    this.oldValue = oldValue;
	    this.affectedWorkItems = new ArrayList<AffectedWorkItem>();
	    this.affectedWorkItems.add(affectedWorkItem);
        this.chosen = "nothing";
    }

    public void addAffectedWorkItem(WorkItem wi, boolean isRequired) {

	    this.affectedWorkItems.add(new AffectedWorkItem(wi, isRequired));
    }

    public List<AffectedWorkItem> getAffectedWorkItems() {
	    return this.affectedWorkItems;
    }

    public AttributeValue getOldValue() {
        return oldValue;
    }
}
