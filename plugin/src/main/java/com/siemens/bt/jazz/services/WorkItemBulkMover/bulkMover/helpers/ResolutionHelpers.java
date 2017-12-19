package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IResolution;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.workflow.ICombinedWorkflowInfos;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.List;

final class ResolutionHelpers {

    static AttributeValue getResolution(Object resolutionObj, IWorkItem workItem,
                                        IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        String stateId = (String)resolutionObj;
        IWorkflowInfo iwfl = workItemServer.findWorkflowInfo(workItem, monitor);
        Identifier<IResolution>[] allWorkflowResolutions = iwfl.getAllResolutionIds();
        for(Identifier<IResolution> state : allWorkflowResolutions) {
            String id = state.getStringIdentifier();
            if(id.equals(stateId)) {
                String name = iwfl.getResolutionName(state);
                return new AttributeValue(id, name);
            }
        }
        return new AttributeValue("", "");
    }

    static void setResolution(IWorkItem workItem, String resolutionId,
                              IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IProjectAreaHandle pa = workItem.getProjectArea();
        ICombinedWorkflowInfos workFlowInfo = workItemServer.findCachedCombinedWorkflowInfos(pa);
        if (workFlowInfo == null) {
            workFlowInfo = workItemServer.findCombinedWorkflowInfos(pa, monitor);
        }
        Identifier<IResolution>[] identifiers = workFlowInfo.getAllResolutionIds();
        for(Identifier<IResolution> id : identifiers) {
            if(resolutionId != null && resolutionId.equals(id.getStringIdentifier())) {
                workItem.setResolution2(id);
                return;
            }
        }
    }

    static List<AttributeValue> addResolutionsAsValues(IProjectAreaHandle pa,
                                                       IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        ICombinedWorkflowInfos workFlowInfo = workItemServer.findCachedCombinedWorkflowInfos(pa);
        if (workFlowInfo == null) {
            workFlowInfo = workItemServer.findCombinedWorkflowInfos(pa, monitor);
        }
        Identifier<IResolution>[] arridentifier = workFlowInfo.getAllResolutionIds();
        int n = arridentifier.length;
        int n2 = 0;
        while (n2 < n) {
            Identifier<IResolution> resolutionId = arridentifier[n2];
            String name = workFlowInfo.getResolutionName(resolutionId);
            String id = resolutionId.getStringIdentifier();
            values.add(new AttributeValue(id, name));
            ++n2;
        }
        return values;
    }
}
