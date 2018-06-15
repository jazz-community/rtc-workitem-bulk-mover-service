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
import com.siemens.bt.jazz.services.base.rest.AbstractRestService;
import com.siemens.bt.jazz.services.base.rest.RestRequest;
import org.apache.commons.logging.Log;
import org.apache.http.auth.AuthenticationException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class InfoService extends AbstractRestService {
    public InfoService(Log log, HttpServletRequest request, HttpServletResponse response, RestRequest restRequest, TeamRawService parentService) {
        super(log, request, response, restRequest, parentService);
    }

    public void execute() throws IOException, URISyntaxException, AuthenticationException {
        Version v = Platform.getBundle("com.siemens.bt.jazz.services.WorkItemBulkMover").getVersion();
        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("version", v.toString());
        response.getWriter().write(new Gson().toJson(statusObject));
    }
}