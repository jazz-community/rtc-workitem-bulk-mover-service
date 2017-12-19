package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.*;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.List;

public final class EnumerationHelpers {

    public static final boolean isValidEnumerationLiteral(IAttribute attribute) {
        return AttributeTypes.isEnumerationAttributeType(attribute.getAttributeType())
        || AttributeTypes.isEnumerationListAttributeType(attribute.getAttributeType());
    }

    static AttributeValue getEnumerationLiteral(IAttribute attribute, Object t_val,
                                                IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        Identifier<? extends ILiteral> lit = (Identifier<? extends ILiteral>)t_val;
        IEnumeration<? extends ILiteral> enumeration = workItemServer.resolveEnumeration(attribute, monitor);
        List<? extends ILiteral> list = enumeration.getEnumerationLiterals(false);
        for (ILiteral literal : list) {
            if(lit.getStringIdentifier().equals(literal.getIdentifier2().getStringIdentifier())) {
                String name = literal.getName();
                String id = literal.getIdentifier2().getStringIdentifier();
                return new AttributeValue(id, name);
            }
        }
        return null;
    }


    static void setEnumerationLiteral(IWorkItem workItem, String attributeId, String literalId,
                                      IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IProjectAreaHandle projectArea = workItem.getProjectArea();
        IAttribute attribute = workItemServer.findAttribute(projectArea, attributeId, monitor);
        IEnumeration<? extends ILiteral> enumeration = workItemServer.resolveEnumeration(attribute, monitor);
        List<? extends ILiteral> list = enumeration.getEnumerationLiterals(false);
        for (ILiteral literal : list) {
            Identifier<? extends ILiteral> identifier = literal.getIdentifier2();
            if(literalId.equals(identifier.getStringIdentifier())) {
                workItem.setValue(attribute, identifier);
                return;
            }
        }
    }

    static List<AttributeValue> addEnumerationLiteralsAsValues(IAttribute attribute,
                                                               IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        IEnumeration<? extends ILiteral> enumeration = workItemServer.resolveEnumeration(attribute, monitor);
        List<? extends ILiteral> list = enumeration.getEnumerationLiterals(false);
        for (ILiteral literal : list) {
            String name = literal.getName();
            String id = literal.getIdentifier2().getStringIdentifier();
            values.add(new AttributeValue(id, name));
        }
        return values;
    }
}
