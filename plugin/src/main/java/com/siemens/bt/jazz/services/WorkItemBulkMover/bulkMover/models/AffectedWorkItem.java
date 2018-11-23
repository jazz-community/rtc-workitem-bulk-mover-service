package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models.WorkItem;

public class AffectedWorkItem {
    private WorkItem workItem;
    private JsonElement chosen;
    private boolean isRequired;

    public AffectedWorkItem(WorkItem workItem) {
        this(workItem, false);
    }

    public AffectedWorkItem(WorkItem workItem, boolean isRequired, JsonPrimitive chosen) {
        this.workItem = workItem;
        this.isRequired = isRequired;
        this.chosen = chosen;
    }

    public AffectedWorkItem(WorkItem workItem, boolean isRequired) {
        this.workItem = workItem;
        this.isRequired = isRequired;
        this.chosen = null;
    }

    public WorkItem getWorkItem() {
        return workItem;
    }


    public JsonPrimitive getMappedIdentifier() {
        return chosen == null || chosen instanceof JsonNull ? null : (JsonPrimitive) chosen;
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof AffectedWorkItem) {
            AffectedWorkItem el = (AffectedWorkItem) object;
            return this.workItem.getId() == el.getWorkItem().getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(workItem.getId());
    }
}
