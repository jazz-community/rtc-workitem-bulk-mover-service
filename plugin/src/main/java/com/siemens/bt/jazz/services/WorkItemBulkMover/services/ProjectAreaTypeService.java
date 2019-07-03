package com.siemens.bt.jazz.services.WorkItemBulkMover.services;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers.WorkItemTypeHelpers;
import com.siemens.bt.jazz.services.WorkItemBulkMover.helpers.ProjectAreaHelpers;
import com.siemens.bt.jazz.services.base.rest.parameters.PathParameters;
import com.siemens.bt.jazz.services.base.rest.parameters.RestRequest;
import com.siemens.bt.jazz.services.base.rest.service.AbstractRestService;
import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.NullProgressMonitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProjectAreaTypeService extends AbstractRestService {
    public ProjectAreaTypeService(Log log, HttpServletRequest request, HttpServletResponse response,
                                  RestRequest restRequest, TeamRawService parentService, PathParameters pathParameters) {
        super(log, request, response, restRequest, parentService, pathParameters);
    }

    public void execute() throws IOException, URISyntaxException {
        String pa = restRequest.getParameterValue("project-area");
        Map<String, JsonElement> typeMap = new TreeMap<String, JsonElement>();
        try {
            IProjectAreaHandle targetArea = ProjectAreaHelpers.getProjectArea(pa, parentService);
            if(targetArea == null) {
                response.setStatus(400);
                return;
            }
            IWorkItemServer serverService = parentService.getService(IWorkItemServer.class);
            List<IWorkItemType> types = WorkItemTypeHelpers.getWorkItemTypes(targetArea, serverService, new NullProgressMonitor());
            for(IWorkItemType type : types) {
                JsonObject typeObject = new JsonObject();
                typeObject.addProperty("id", type.getIdentifier());
                typeObject.addProperty("name", type.getDisplayName());
                typeMap.put(type.getDisplayName(), typeObject);
            }
        } catch (TeamRepositoryException e) {
            response.setStatus(500);
        }
        response.getWriter().write(new Gson().toJson(typeMap.values()));
    }
}