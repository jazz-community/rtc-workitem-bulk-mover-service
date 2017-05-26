package com.siemens.bt.jazz.services.WorkItemBulkMover.services;

import com.google.gson.Gson;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.service.IProcessServerService;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.IContributorHandle;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.TeamRawService;
import com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models.ProjectArea;
import com.siemens.bt.jazz.services.base.rest.AbstractRestService;
import com.siemens.bt.jazz.services.base.rest.RestRequest;
import org.apache.commons.logging.Log;
import org.apache.http.auth.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ProjectAreaService extends AbstractRestService {

    public ProjectAreaService(Log log, HttpServletRequest request, HttpServletResponse response, RestRequest restRequest, TeamRawService parentService) {
        super(log, request, response, restRequest, parentService);
    }

    public void execute() throws IOException, URISyntaxException, AuthenticationException {
        Gson googleJson = new Gson();
        try {
            IProcessServerService processServerService = parentService.getService(IProcessServerService.class);
            IContributorHandle contribHandle = processServerService.getAuthenticatedContributor();
            IRepositoryItemService itemService = parentService.getService(IRepositoryItemService.class);
            IContributor contributor = (IContributor) itemService.fetchItem(contribHandle, null);
            List<ProjectArea> projectAreas = new ArrayList<ProjectArea>();
            IProcessArea[] areas = processServerService.findProcessAreas(contributor, null, null);
            for(IProcessArea a : areas) {
                IProjectArea pa = (IProjectArea) itemService.fetchItem(a.getProjectArea(), null);
                projectAreas.add(new ProjectArea(pa.getItemId().toString(), pa.getName()));
            }
            String projectAreasJson = googleJson.toJson(projectAreas);
            response.getWriter().write(projectAreasJson);
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}
