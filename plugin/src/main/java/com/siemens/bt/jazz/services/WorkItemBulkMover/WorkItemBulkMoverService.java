package com.siemens.bt.jazz.services.WorkItemBulkMover;

import com.siemens.bt.jazz.services.WorkItemBulkMover.services.InfoService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.MoveService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.ProjectAreaService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.services.ProjectAreaTypeService;
import com.siemens.bt.jazz.services.base.BaseService;
import com.siemens.bt.jazz.services.base.configuration.Configuration;
import com.siemens.bt.jazz.services.base.configuration.preset.ContentConfigurator;
import com.siemens.bt.jazz.services.base.configuration.preset.EncodingConfigurator;
import org.apache.http.entity.ContentType;

import java.nio.charset.StandardCharsets;

public class WorkItemBulkMoverService extends BaseService implements IWorkItemBulkMoverService {
	public WorkItemBulkMoverService() {
		super();

		// create an encoding configurator that sets utf 8 encoding for all responses
		EncodingConfigurator utf = new EncodingConfigurator(StandardCharsets.UTF_8.name());
		// create a content configurator that sets the response type to application/xml
		ContentConfigurator xml = new ContentConfigurator(ContentType.APPLICATION_JSON.toString());
		// wrap the configurators in a configuration
		Configuration response = new Configuration(utf, xml);

		router.get("info", InfoService.class, response);
		router.post("move", MoveService.class, response);
		router.get("project-areas", ProjectAreaService.class, response);
		router.get("types", ProjectAreaTypeService.class, response);
	}
}
