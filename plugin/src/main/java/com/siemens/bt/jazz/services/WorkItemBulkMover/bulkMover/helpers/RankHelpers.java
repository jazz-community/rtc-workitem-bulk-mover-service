package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.service.IWorkItemServer;

public final class RankHelpers {
    private static final String RANK_ATTR_ID = "com.ibm.team.apt.attribute.planitem.newRanking._pm7NmRYUEd6L1tNIGdz5qQ";

    public static void unsetRank(IWorkItem wi, IWorkItemServer wiSrv) throws TeamRepositoryException {
        IAttribute attr = wiSrv.findAttribute(wi.getProjectArea(), RANK_ATTR_ID, null);
        if(wi.hasAttribute(attr)) {
            wi.removeCustomAttribute(attr);
        }
    }
}
