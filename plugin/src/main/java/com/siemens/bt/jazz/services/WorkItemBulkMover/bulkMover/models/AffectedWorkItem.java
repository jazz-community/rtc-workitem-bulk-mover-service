package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models.WorkItem;

public class AffectedWorkItem {
    private WorkItem workItem;
    private String chosen;
    private boolean isRequired;

    public AffectedWorkItem(WorkItem workItem, boolean isRequired) {
        this.workItem = workItem;
        this.isRequired = isRequired;
        this.chosen = "nothing";
    }

    public WorkItem getWorkItem() {
        return workItem;
    }


    public String getMappedIdentifier() {
        return chosen;
    }
}
