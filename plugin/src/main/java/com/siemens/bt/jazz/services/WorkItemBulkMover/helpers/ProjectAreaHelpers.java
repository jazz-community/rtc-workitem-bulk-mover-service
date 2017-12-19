package com.siemens.bt.jazz.services.WorkItemBulkMover.helpers;

import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.process.service.IProcessServerService;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.TeamRawService;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

public final class ProjectAreaHelpers {

    public static IProjectAreaHandle getProjectArea(String projectAreaName, TeamRawService service) throws UnsupportedEncodingException, TeamRepositoryException, URISyntaxException {
        if(projectAreaName == null)
            return null;
        IProcessServerService processServerService = service.getService(IProcessServerService.class);
        String projectAreaURI = java.net.URLEncoder.encode(projectAreaName, "UTF-8");
        projectAreaURI = projectAreaURI.replace("+", "%20");
        IProcessArea processArea = processServerService.findProcessArea(new java.net.URI(projectAreaURI).toString(), null);
        return processArea != null ? processArea.getProjectArea() : null;
    }
}
