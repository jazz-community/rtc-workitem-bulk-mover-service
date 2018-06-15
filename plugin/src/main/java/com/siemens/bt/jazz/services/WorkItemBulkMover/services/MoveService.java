package com.siemens.bt.jazz.services.WorkItemBulkMover.services;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.WorkItemMover;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeDefinition;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.MovePreparationResult;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.TypeMappingEntry;
import com.siemens.bt.jazz.services.WorkItemBulkMover.helpers.ProjectAreaHelpers;
import com.siemens.bt.jazz.services.WorkItemBulkMover.helpers.WorkItemHelpers;
import com.siemens.bt.jazz.services.base.rest.AbstractRestService;
import com.siemens.bt.jazz.services.base.rest.RestRequest;
import com.siemens.bt.jazz.services.base.utils.RequestReader;
import org.apache.commons.logging.Log;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class MoveService extends AbstractRestService {
    private IWorkItemServer workItemServer;
    private IProgressMonitor monitor;
    private Gson gson;
    private Type workItemIdCollectionType;
    private Type attributesCollectionType;
    private Type typeMappingCollectionType;
    private Type resultsType;

    public MoveService(Log log, HttpServletRequest request, HttpServletResponse response, RestRequest restRequest, TeamRawService parentService) {
        super(log, request, response, restRequest, parentService);
        this.workItemServer = parentService.getService(IWorkItemServer.class);
        this.monitor = new NullProgressMonitor();
        this.gson = new GsonBuilder().serializeNulls().create();
        this.workItemIdCollectionType = new TypeToken<Collection<Integer>>(){}.getType();
        this.attributesCollectionType = new TypeToken<Collection<AttributeDefinition>>(){}.getType();
        this.typeMappingCollectionType = new TypeToken<Collection<TypeMappingEntry>>(){}.getType();
        this.resultsType = new TypeToken<Collection<AttributeDefinition>>() {}.getType();
    }
	
	public void execute() throws IOException {
        JsonObject responseJson = new JsonObject();
        WorkItemMover mover = new WorkItemMover(parentService);
        boolean isMoved = false;
        boolean previewOnly = false;
        String error = null;
        Collection<AttributeDefinition> moveResults = null;

        // read request data
        JsonObject workItemData = RequestReader.readAsJson(request);
        JsonPrimitive previewPrimitive = workItemData.getAsJsonPrimitive("previewOnly");
        JsonPrimitive targetPA = workItemData.getAsJsonPrimitive("targetProjectArea");
        JsonArray workItemJson = workItemData.getAsJsonArray("workItems");
        JsonArray typeMappingJson = workItemData.getAsJsonArray("typeMapping");
        JsonArray attributesJson = workItemData.getAsJsonArray("mapping");

        if(previewPrimitive != null) {
            previewOnly = previewPrimitive.getAsBoolean();
        }
        // map cient data to model
        Collection<Integer> clientWorkItemList = gson.fromJson(workItemJson, workItemIdCollectionType);
        List<AttributeDefinition> clientMappingDefinitions = gson.fromJson(attributesJson, attributesCollectionType);
        Collection<TypeMappingEntry> typeMappingDefinitions = gson.fromJson(typeMappingJson, typeMappingCollectionType);
        Map<String, String> typeMap = new HashMap<String, String>();
        for (TypeMappingEntry def : typeMappingDefinitions) {
            typeMap.put(def.getSource(), def.getTarget());
        }

        try {
            // fetch full work item information
			List<IWorkItem> items = WorkItemHelpers.fetchWorkItems(clientWorkItemList, workItemServer, monitor);

            // resolve project area
            IProjectAreaHandle targetArea = ProjectAreaHelpers.getProjectArea(targetPA.getAsString(), parentService);

            // prepare movement and track fields to be changed
			MovePreparationResult preparationResult = mover.PrepareMove(items, targetArea, clientMappingDefinitions, typeMap);

			// store attribute based ovservations to be able to return this information to the end user
			moveResults = preparationResult.getAttributeDefinitions();

			if(!previewOnly) {
                // try to move the work items...
                IStatus status = mover.MoveAll(preparationResult.getWorkItems());
                isMoved = status.isOK();
                if(!isMoved) {
                    error = status.getMessage();
                }
            }
            if(!isMoved && moveResults.size() == 0) {
			    error = "The move operation failed, but there is no mapping data available.";
            }
		} catch (Exception e) {
            // Inform the user the the items could not be moved
            error = e.getMessage();
            if(error == null) {
                error = e.toString();
            }
		}

		if(error != null) {
            responseJson.addProperty("error", error);
        }

        // prepare data to be returend
        responseJson.addProperty("successful", isMoved);
        responseJson.add("mapping", gson.toJsonTree(moveResults, resultsType));
        response.getWriter().write(gson.toJson(responseJson));
    }
}
