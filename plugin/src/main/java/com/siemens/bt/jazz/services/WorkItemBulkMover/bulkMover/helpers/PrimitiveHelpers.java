package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.internal.model.NumericAttributeType;
import com.ibm.team.workitem.common.internal.model.TimestampAttributeType;
import com.ibm.team.workitem.common.model.AttributeType;
import com.ibm.team.workitem.common.model.AttributeTypes;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;

import java.math.BigDecimal;

final class PrimitiveHelpers {

    static AttributeValue getPrimitive(IWorkItem workItem, IAttribute attribute) {
        Object val = workItem.getValue(attribute);
        return new AttributeValue(attribute.getIdentifier(), val.toString());
    }

    static void setPrimitive(IWorkItem workItem, IAttribute attribute, String value) throws TeamRepositoryException {
        AttributeType type = AttributeTypes.getAttributeType(attribute.getAttributeType());

        Object oValue;
        if(type instanceof NumericAttributeType || AttributeTypes.NUMBER_TYPES.contains(attribute.getAttributeType())) {
            try {
                oValue = toObject(type.getInstanceType(), value);
            } catch (NumberFormatException ex) {
                throw new TeamRepositoryException("Unable to set value \"" + value + "\" for attribute '" + attribute.getDisplayName() + "'. Make sure that your input matches.");
            }
        } else if (type instanceof TimestampAttributeType) {
            throw new TeamRepositoryException("Datetime conversion not implemented");
        } else {
            // TODO: support other types here in 'else if' clauses
            oValue = value;
        }

        try {
            workItem.setValue(attribute, oValue);
        } catch (Exception eee) {
            throw new TeamRepositoryException(eee.getMessage());
        }
    }

    private static Object toObject(Class clazz, String value) {
        if(Boolean.class == clazz || Boolean.TYPE == clazz)
            return Boolean.parseBoolean(value);
        if(Byte.class == clazz)
            return Byte.parseByte(value);
        if(Short.class == clazz)
            return Short.parseShort(value);
        if(Integer.class == clazz)
            return Integer.parseInt(value);
        if(Long.class == clazz)
            return Long.parseLong(value);
        if(Float.class == clazz)
            return Float.parseFloat(value);
        if(Double.class == clazz)
            return Double.parseDouble(value);
        if(BigDecimal.class == clazz)
            return new BigDecimal(value);
        return value;
    }
}
