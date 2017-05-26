package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.List;

public final class WorkItemTypeHelpers {

    public static final AttributeValue getWorkItemType(Object wiType, IProjectAreaHandle pa,
                                          IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        String typeId = (String)wiType;
        IWorkItemType type = workItemServer.findWorkItemType(pa, typeId, monitor);
        String id = type.getIdentifier();
        String displayName = type.getDisplayName();
        return new AttributeValue(id, displayName);
    }

    public static final void setWorkItemType(IWorkItem workItem, String odlWorkItemTypeId, String newWorkItemTypeId,
                                 IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IProjectAreaHandle pa = workItem.getProjectArea();
        IWorkItemType oldType = workItemServer.findWorkItemType(pa, odlWorkItemTypeId, monitor);
        IWorkItemType newType = workItemServer.findWorkItemType(pa, newWorkItemTypeId, monitor);
        if(newType != null) {
            workItemServer.updateWorkItemType(workItem, newType, oldType, monitor);
        }
    }

    public static final List<AttributeValue> addWorkItemTypesAsValues(IProjectAreaHandle pa,
                                                         IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        List<IWorkItemType> workItemTypes = workItemServer.findCachedWorkItemTypes(pa);
        if (workItemTypes == null) {
            workItemTypes = workItemServer.findWorkItemTypes(pa, monitor);
        }
        for (IWorkItemType type : workItemTypes) {
            String name = type.getDisplayName();
            String id = type.getIdentifier();
            values.add(new AttributeValue(id, name));
        }
        return values;
    }

}
