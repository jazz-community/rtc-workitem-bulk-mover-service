package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IState;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.workflow.ICombinedWorkflowInfos;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.List;

public final class StateHelpers {

    public static final AttributeValue getState(Object stateObj, IWorkItem workItem,
                                    IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        String stateId = (String)stateObj;
        IWorkflowInfo iwfl = workItemServer.findWorkflowInfo(workItem, monitor);
        Identifier<IState>[] allWorkflowStates = iwfl.getAllStateIds();
        for(Identifier<IState> state : allWorkflowStates) {
            String id = state.getStringIdentifier();
            if(id.equals(stateId)) {
                String name = iwfl.getStateName(state);
                return new AttributeValue(id, name);
            }
        }
        return new AttributeValue("", "");
    }

    public static final void setState(IWorkItem workItem, String stateId,
                          IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IProjectAreaHandle pa = workItem.getProjectArea();
        IWorkflowInfo iwfl = workItemServer.findWorkflowInfo(workItem, monitor);
        Identifier<IState>[] allWorkflowStates = iwfl.getAllStateIds();
        for(Identifier<IState> state : allWorkflowStates) {
            String id = state.getStringIdentifier();
            if(id.equals(stateId)) {
                workItem.setState2(state);
            }
        }
        //TODO this impl is no longer valid! Problem is that setState2 is deprecated
    }

    public static final List<AttributeValue> addStatesAsValues(IProjectAreaHandle pa, IWorkItem wi,
                                                   IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        ICombinedWorkflowInfos workFlowInfo = workItemServer.findCachedCombinedWorkflowInfos(pa);
        if (workFlowInfo == null) {
            workFlowInfo = workItemServer.findCombinedWorkflowInfos(pa, monitor);
        }
        Identifier<IState>[] identifiers = workFlowInfo.getAllStateIds();
        int n = identifiers.length;
        int n2 = 0;
        while (n2 < n) {
            Identifier<IState> stateId = identifiers[n2];
            String id = stateId.getStringIdentifier();
            String name = workFlowInfo.getStateName(stateId);
            values.add(new AttributeValue(id, name + "(" + id + ")"));
            ++n2;
        }
        return values;
    }

}
