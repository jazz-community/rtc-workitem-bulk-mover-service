package com.siemens.bt.jazz.services.WorkItemBulkMover.helpers;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.service.IWorkItemServer;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * helper functions when working with work items
 */
public final class WorkItemHelpers {
    /**
     * Fetch the complete work item objects
     * @param workItemList all work items IDs for which we want to fetch their entire information
     * @param workItemServer work item server APi
     * @return list of fully fetched work items
     * @throws TeamRepositoryException just in case of any unexpected RTC behaviour
     */
    public static List<IWorkItem> fetchWorkItems(Collection<Integer> workItemList, IWorkItemServer workItemServer,
                                                 IProgressMonitor monitor) throws TeamRepositoryException {
        List<IWorkItem> sourceWorkItems = new ArrayList<IWorkItem>();
        for(int workItemId : workItemList) {
            IWorkItem sourceWorkItem = workItemServer.findWorkItemById(workItemId, IWorkItem.FULL_PROFILE, monitor);
            IWorkItem wi = (IWorkItem) sourceWorkItem.getWorkingCopy();
            sourceWorkItems.add(wi);
        }
        return sourceWorkItems;
    }
}
