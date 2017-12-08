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

    public static final void setWorkItemType(IWorkItem sourceWorkItem, IWorkItem targetWorkItem, String odlWorkItemTypeId, String newWorkItemTypeId,
                                 IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IProjectAreaHandle sourcePa = sourceWorkItem.getProjectArea();
        IProjectAreaHandle targetPa = targetWorkItem.getProjectArea();
        IWorkItemType oldType = workItemServer.findWorkItemType(sourcePa, odlWorkItemTypeId, monitor);
        IWorkItemType newType = workItemServer.findWorkItemType(targetPa, newWorkItemTypeId, monitor);
        if(newType != null) {
            workItemServer.updateWorkItemType(targetWorkItem, newType, oldType, monitor);
        }
    }

    public static final List<IWorkItemType> getWorkItemTypes(IProjectAreaHandle pa, IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        List<IWorkItemType> workItemTypes = workItemServer.findCachedWorkItemTypes(pa);
        if (workItemTypes == null) {
            workItemTypes = workItemServer.findWorkItemTypes(pa, monitor);
        }
        return workItemTypes;
    }
}
