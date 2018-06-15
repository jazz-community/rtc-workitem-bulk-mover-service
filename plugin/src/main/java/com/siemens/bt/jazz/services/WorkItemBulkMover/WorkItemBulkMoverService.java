package com.siemens.bt.jazz.services.WorkItemBulkMover;

import com.ibm.team.jfs.app.http.util.HttpConstants.HttpMethod;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.InfoService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.MoveService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.ProjectAreaService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.ProjectAreaTypeService;
import com.siemens.bt.jazz.services.base.BaseService;
import com.siemens.bt.jazz.services.base.router.factory.RestFactory;

public class WorkItemBulkMoverService extends BaseService implements IWorkItemBulkMoverService {
	public WorkItemBulkMoverService() {
		super();
		router.addService(HttpMethod.POST, "info", new RestFactory(InfoService.class));
		router.addService(HttpMethod.POST, "move", new RestFactory(MoveService.class));
		router.addService(HttpMethod.GET, "project-areas", new RestFactory(ProjectAreaService.class));
		router.addService(HttpMethod.GET, "types", new RestFactory(ProjectAreaTypeService.class));
	}
}
