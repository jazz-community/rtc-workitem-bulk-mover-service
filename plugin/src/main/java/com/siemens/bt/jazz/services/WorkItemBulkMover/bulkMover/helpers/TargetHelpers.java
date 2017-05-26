package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IDevelopmentLine;
import com.ibm.team.process.common.IIteration;
import com.ibm.team.process.common.IIterationHandle;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.IAuditableHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.internal.util.IterationsHelper;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.ItemProfile;
import com.ibm.team.workitem.service.IAuditableServer;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TargetHelpers {

    @SuppressWarnings("restriction")
    public static final AttributeValue getTarget(Object t_val, IAuditableServer auditSrv,
                                    IWorkItemServer workItemServer, TeamRawService service, IProgressMonitor monitor) throws TeamRepositoryException {
        if(t_val == null)
            return new AttributeValue("", "");
        IRepositoryItemService itemService = service.getService(IRepositoryItemService.class);
        IIterationHandle iterHandle = (IIterationHandle)t_val;
        IIteration iteration = (IIteration) itemService.fetchItem(iterHandle, null);
        String name = iteration.getName();
        String fullId = IterationsHelper.createIterationPath((IIterationHandle)iterHandle, auditSrv, monitor);
        return new AttributeValue(fullId, name);
    }

    @SuppressWarnings("restriction")
    public static final void setTarget(IWorkItem workItem, String targetValue,
                           IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IAuditableServer auditSrv = workItemServer.getAuditableServer();
        IProjectAreaHandle ipa = workItem.getProjectArea();
        IIteration iter = IterationsHelper.resolveIterationFromPath(ipa, targetValue, auditSrv, null);
        workItem.setTarget(iter);
    }

    @SuppressWarnings("restriction")
    public static final List<AttributeValue> addTargetsAsValues(IProjectAreaHandle pa,
                                                    IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IAuditableServer auditSrv = workItemServer.getAuditableServer();
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        List<IDevelopmentLine> developmentLines = auditSrv.resolveAuditablesPermissionAware(Arrays.asList((
                auditSrv.resolveAuditable((IAuditableHandle)pa, ItemProfile.PROJECT_AREA_DEFAULT, monitor)).getDevelopmentLines()), ItemProfile.DEVELOPMENT_LINE_DEFAULT, monitor);
        if (!developmentLines.isEmpty()) {
            for (IDevelopmentLine curDevLine : developmentLines) {
                List<IIteration> iterations = IterationsHelper.findAllIterations(auditSrv, curDevLine.getIterations(), ItemProfile.ITERATION_DEFAULT, false, monitor);
                for (IIteration curIter : iterations) {
                    String name = curIter.getName();
                    String path = IterationsHelper.createIterationPath((IIterationHandle)curIter, auditSrv, monitor);
                    values.add(new AttributeValue(path, name));
                }
            }
        }
        return values;
    }
}
