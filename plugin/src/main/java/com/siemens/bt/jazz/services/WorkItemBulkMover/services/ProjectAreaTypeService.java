package com.siemens.bt.jazz.services.WorkItemBulkMover.services;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers.WorkItemTypeHelpers;
import com.siemens.bt.jazz.services.WorkItemBulkMover.helpers.ProjectAreaHelpers;
import com.siemens.bt.jazz.services.base.rest.AbstractRestService;
import com.siemens.bt.jazz.services.base.rest.RestRequest;
import org.apache.commons.logging.Log;
import org.apache.http.auth.AuthenticationException;
import org.eclipse.core.runtime.NullProgressMonitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class ProjectAreaTypeService extends AbstractRestService {
    public ProjectAreaTypeService(Log log, HttpServletRequest request, HttpServletResponse response, RestRequest restRequest, TeamRawService parentService) {
        super(log, request, response, restRequest, parentService);
    }

    public void execute() throws IOException, URISyntaxException, AuthenticationException {
        String pa = restRequest.getParameterValue("project-area");
        if(pa == null) {
            response.setStatus(400);
            return;
        }
        JsonArray typeArray = new JsonArray();
        try {
            IProjectAreaHandle targetArea = ProjectAreaHelpers.getProjectArea(pa, parentService);
            IWorkItemServer serverService = parentService.getService(IWorkItemServer.class);
            List<IWorkItemType> types = WorkItemTypeHelpers.getWorkItemTypes(targetArea, serverService, new NullProgressMonitor());
            for(IWorkItemType type : types) {
                JsonObject typeObject = new JsonObject();
                typeObject.addProperty("id", type.getIdentifier());
                typeObject.addProperty("name", type.getDisplayName());
                typeArray.add(typeObject);
            }
        } catch (TeamRepositoryException e) {
            response.setStatus(500);
        }
        response.getWriter().write(new Gson().toJson(typeArray));
    }
}