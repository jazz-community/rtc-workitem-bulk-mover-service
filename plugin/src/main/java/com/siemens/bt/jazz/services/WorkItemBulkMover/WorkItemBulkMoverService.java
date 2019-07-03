package com.siemens.bt.jazz.services.WorkItemBulkMover;

import com.siemens.bt.jazz.services.WorkItemBulkMover.services.InfoService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.MoveService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.ProjectAreaService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.ProjectAreaTypeService;
import com.siemens.bt.jazz.services.base.BaseService;

public class WorkItemBulkMoverService extends BaseService implements IWorkItemBulkMoverService {
	public WorkItemBulkMoverService() {
		super();
		router.get("info", InfoService.class);
		router.post("move", MoveService.class);
		router.get("project-areas", ProjectAreaService.class);
		router.get("types", ProjectAreaTypeService.class);
	}
}
