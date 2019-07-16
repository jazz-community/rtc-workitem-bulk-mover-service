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
import com.siemens.bt.jazz.services.base.configuration.Configuration;
import com.siemens.bt.jazz.services.base.rest.parameters.PathParameters;
import com.siemens.bt.jazz.services.base.rest.service.AbstractRestService;
import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

public class ProjectAreaService extends AbstractRestService {

    public ProjectAreaService(String uri, Log log, HttpServletRequest request, HttpServletResponse response, Configuration configuration, TeamRawService parentService, PathParameters pathParameters) {
        super(uri, log, request, response, configuration, parentService, pathParameters);
    }

    public void execute() {
        Gson googleJson = new Gson();
        String ignoredProjectAreas = request.getParameter("ignore");
        List<String> ignorePrjAreaList;
        if(ignoredProjectAreas != null && ignoredProjectAreas.length() > 0) {
            ignorePrjAreaList = Arrays.asList(ignoredProjectAreas.split(","));
        } else {
            ignorePrjAreaList = new ArrayList<String>();
        }
        try {
            IProcessServerService processServerService = parentService.getService(IProcessServerService.class);
            IContributorHandle contribHandle = processServerService.getAuthenticatedContributor();
            IRepositoryItemService itemService = parentService.getService(IRepositoryItemService.class);
            IContributor contributor = (IContributor) itemService.fetchItem(contribHandle, null);
            Map<String, ProjectArea> projectAreas = new TreeMap<String, ProjectArea>();
            IProcessArea[] areas = processServerService.findProcessAreas(contributor, null, null);
            for(IProcessArea a : areas) {
                IProjectArea pa = (IProjectArea) itemService.fetchItem(a.getProjectArea(), null);
                String paId = pa.getItemId().getUuidValue();
                if(!ignorePrjAreaList.contains(paId)) {
                    projectAreas.put(pa.getName(), new ProjectArea(paId, pa.getName()));
                }
            }
            String projectAreasJson = googleJson.toJson(projectAreas);
            response.getWriter().write(projectAreasJson);
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}
