package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.google.gson.JsonPrimitive;
import com.ibm.team.foundation.common.text.XMLString;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.AttributeType;
import com.ibm.team.workitem.common.model.AttributeTypes;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;

import java.sql.Timestamp;

public final class PrimitiveHelpers {

    public static AttributeValue getPrimitive(IWorkItem workItem, IAttribute attribute) {
        Object val = workItem.getValue(attribute);
        JsonPrimitive primitivo;
        if(val == null) {
            primitivo = null;
        } else if(val instanceof Boolean) {
            primitivo = new JsonPrimitive((Boolean) val);
        } else if(val instanceof Number) {
            primitivo = new JsonPrimitive((Number) val);
        } else if(val instanceof String) {
            if(AttributeTypes.HTML_TYPES.contains(attribute.getAttributeType())) {
                val = XMLString.createFromXMLText((String) val).getPlainText();
            }
            primitivo = new JsonPrimitive((String) val);
        } else {
            primitivo = new JsonPrimitive(val.toString());
        }
        return new AttributeValue(attribute.getIdentifier(), primitivo);
    }

    static void setPrimitive(IWorkItem workItem, IAttribute attribute, JsonPrimitive value) throws TeamRepositoryException {
        AttributeType type = AttributeTypes.getAttributeType(attribute.getAttributeType());
        Object oValue;
        if(type.getInstanceType() == Timestamp.class) {
            oValue = Timestamp.valueOf(value.getAsString());
        } else if(value.isBoolean()) {
            oValue = value.getAsBoolean();
        } else if(value.isNumber()) {
            Class instanceType = type.getInstanceType();
            if(instanceType == Integer.class) {
                oValue = value.getAsInt();
            } else if(instanceType == Long.class) {
                oValue = value.getAsLong();
            } else {
                oValue = value.getAsNumber();
            }
        } else if(value.isString()) {
            oValue = value.getAsString();
        } else {
            oValue = value.toString();
        }

        try {
            workItem.setValue(attribute, oValue);
        } catch (Exception eee) {
            throw new TeamRepositoryException("Unable to set value \"" + value + "\" for attribute '" + attribute.getDisplayName() + "'. Make sure that your input matches.");
        }
    }
}
