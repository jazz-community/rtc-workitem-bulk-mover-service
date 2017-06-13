package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.model.*;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by z002t6hs on 12.06.2017.
 */
public final class FoundInHelpers {
    public static final AttributeValue getFoundIn(Object targetValue, IWorkItemServer workItemServer, TeamRawService service, IProgressMonitor monitor) throws TeamRepositoryException {
        IRepositoryItemService itemService = service.getService(IRepositoryItemService.class);
        IDeliverableHandle deliverableHandle = (IDeliverableHandle)targetValue;
        IDeliverable deliverable = (IDeliverable) itemService.fetchItem(deliverableHandle, null);

        if(targetValue != null && deliverable != null) {
            return new AttributeValue(deliverable.getName(), deliverable.getName());
        } else {
            return new AttributeValue("", "");
        }
    }

    public static final void setFoundIn(IWorkItem workItem, String targetValue,
                                        IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IAttribute foundInAttr = workItemServer.findAttribute(workItem.getProjectArea(), IWorkItem.FOUND_IN_PROPERTY, monitor);
        IDeliverable deliverable = workItemServer.findDeliverableByName(workItem.getProjectArea(), targetValue, IDeliverable.DEFAULT_PROFILE, monitor);
        if(deliverable != null) {
            workItem.setValue(foundInAttr, deliverable.getItemHandle());
        }
    }

    public static final List<AttributeValue> addFoundInAsValues(IProjectAreaHandle pa, TeamRawService service,
                                                                IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IRepositoryItemService itemService = service.getService(IRepositoryItemService.class);
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        List<IDeliverable> deliverables = workItemServer.findDeliverablesByProjectArea(pa, false, IDeliverable.SMALL_PROFILE, monitor);
        if(!deliverables.isEmpty()) {
            for(IDeliverable deliverable : deliverables) {
                values.add(new AttributeValue(deliverable.getName(), deliverable.getName()));
            }
        }
        return  values;
    }
}
