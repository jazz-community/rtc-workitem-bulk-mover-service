package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models;

import com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models.WorkItem;

import java.util.ArrayList;
import java.util.List;

public class MappingDefinition {
	private AttributeValue oldValue;
	private List<AffectedWorkItem> affectedWorkItems;
    private List<AttributeValue> allowedValues;
    private String chosen;
    private String error;
	private boolean showDetails = false;

	public MappingDefinition(AttributeValue oldValue, AffectedWorkItem affectedWorkItem) {
	    this.oldValue = oldValue;
        allowedValues = new ArrayList<AttributeValue>();
        this.affectedWorkItems = new ArrayList<AffectedWorkItem>();
	    this.affectedWorkItems.add(affectedWorkItem);
	    this.error = null;
        this.chosen = "nothing";
    }

    public void addAffectedWorkItem(WorkItem wi, boolean isRequired) {
	    this.affectedWorkItems.add(new AffectedWorkItem(wi, isRequired));
    }

    public List<AttributeValue> getAttributeValues() {
        return allowedValues;
    }

    public void addAllowedValues(List<AttributeValue> attrVals) {
        allowedValues.addAll(attrVals);
    }

    public List<AffectedWorkItem> getAffectedWorkItems() {
	    return this.affectedWorkItems;
    }

    public AttributeValue getOldValue() {
        return oldValue;
    }

    public void setError(String errorMsg) {
	    this.error = errorMsg;
    }
}
