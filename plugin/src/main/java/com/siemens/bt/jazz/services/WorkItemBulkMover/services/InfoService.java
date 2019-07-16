package com.siemens.bt.jazz.services.WorkItemBulkMover.services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ibm.team.repository.service.TeamRawService;
import com.siemens.bt.jazz.services.base.configuration.Configuration;
import com.siemens.bt.jazz.services.base.rest.parameters.PathParameters;
import com.siemens.bt.jazz.services.base.rest.service.AbstractRestService;
import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class InfoService extends AbstractRestService {

    public InfoService(String uri, Log log, HttpServletRequest request, HttpServletResponse response, Configuration configuration, TeamRawService parentService, PathParameters pathParameters) {
        super(uri, log, request, response, configuration, parentService, pathParameters);
    }

    public void execute() throws IOException {
        Version v = Platform.getBundle("com.siemens.bt.jazz.services.WorkItemBulkMover").getVersion();
        JsonObject statusObject = new JsonObject();
        statusObject.addProperty("version", v.toString());
        response.getWriter().write(new Gson().toJson(statusObject));
    }
}