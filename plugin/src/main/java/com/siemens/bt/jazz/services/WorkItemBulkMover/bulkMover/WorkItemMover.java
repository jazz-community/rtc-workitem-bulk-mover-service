package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.IAuditable;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.internal.IAdditionalSaveParameters;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.service.IAuditableServer;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.ibm.team.workitem.service.IWorkItemWrapper;
import com.ibm.team.workitem.service.internal.WorkItemWrapper;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers.AttributeHelpers;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers.WorkItemTypeHelpers;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.*;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.collections.AttributeDefinitions;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.operations.BulkMoveOperation;
import com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models.WorkItem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;

public class WorkItemMover {
	private TeamRawService service;
	private IWorkItemServer workItemServer;
	private IProgressMonitor monitor;
	
	public WorkItemMover(TeamRawService parentService) {
		service = parentService;
		workItemServer = service.getService(IWorkItemServer.class);
		monitor = null;
	}

    /**
     * Prepare all work items for being moved
     * @param sourceWorkItems the list of work items to be moved
     * @param targetArea the target project area for all those work items
     * @param mappingDefinitions the mapping definition provided by the user
     * @return work items and attribute definitions
     * @throws UnsupportedEncodingException
     * @throws TeamRepositoryException
     * @throws URISyntaxException
     */
	@SuppressWarnings("restriction")
	public MovePreparationResult PrepareMove(List<IWorkItem> sourceWorkItems, IProjectAreaHandle targetArea, Collection<AttributeDefinition> mappingDefinitions, Map<String, String> typeMap) throws UnsupportedEncodingException, TeamRepositoryException, URISyntaxException {
		// run the bulk movement operation
		BulkMoveOperation oper = new BulkMoveOperation(targetArea, service);
		oper.run(sourceWorkItems, workItemServer, monitor);

		// get the workitems involved into the move operation
		List<WorkItemMoveMapper> workItems = oper.getMappedWorkItems();
		if(workItems.size() == 0) {
			throw new TeamRepositoryException("No work Items found to move. This likely happens if Source and Targer project area are the same");
		}

		// update work item type according to client mapping
		for(WorkItemMoveMapper entry : workItems) {
			String sourceType = entry.getSourceWorkItem().getWorkItemType();
			String targetType = typeMap.get(sourceType);
			WorkItemTypeHelpers.setWorkItemType(entry.getSourceWorkItem(), entry.getTargetWorkItem(), sourceType, targetType, workItemServer, monitor);
		}

		if(mappingDefinitions != null && mappingDefinitions.size() > 0) {
			// if the user has sent mappings for specific attributes, apply them now
			applyMappingsToWorkItems(workItems, mappingDefinitions);
		}

		// compare source and target work items to track what has changed
		AttributeDefinitions anRes = analyzeWorkItems2(workItems);

		// everything is ready, return the new work items and all analyzation results
		return new MovePreparationResult(WorkItemMoveMapper.getAllTargetWorkItems(workItems), anRes);
	}

    /**
     * Apply the the specified attribute mappings to all affected work item
     * @param mappedWorkItems all work items involved into the move workflow
     * @param mappingDefinitions the mapping definition provided by the user
     * @throws TeamRepositoryException if anything fails
     */
	private void applyMappingsToWorkItems(List<WorkItemMoveMapper> mappedWorkItems,
			Collection<AttributeDefinition> mappingDefinitions) throws TeamRepositoryException {
        AttributeHelpers attributeHelpers = new AttributeHelpers(service, workItemServer, monitor);
		Map<Integer, HashMap<String, String>> attributeMap = getAttributesByWorkItem(mappingDefinitions);

		for(WorkItemMoveMapper mappedWorkItem : mappedWorkItems) {
			IWorkItem sourceWorkItem = mappedWorkItem.getSourceWorkItem();
			IWorkItem targetWorkItem = mappedWorkItem.getTargetWorkItem();
			HashMap<String, String> attributes = attributeMap.get(sourceWorkItem.getId());

			if(attributes != null) {
				for(Entry<String, String> attribute : attributes.entrySet()) {
                    attributeHelpers.setAttributeForWorkItem(targetWorkItem, attribute.getKey(), attribute.getValue());
				}
			}
		}
	}

    /**
     * The users passes attribute definitions and those have work item IDS associated
     * This method restructures them by work item
     * @param mappingDefinitions the mapping definition provided by the user
     * @return attribute definitions by work item ID
     */
	private Map<Integer, HashMap<String, String>> getAttributesByWorkItem(Collection<AttributeDefinition> mappingDefinitions) {
		Map<Integer, HashMap<String, String>> attributes = new HashMap<Integer, HashMap<String, String>>();

		for(AttributeDefinition mapping : mappingDefinitions) {
			String attributeId = mapping.getIdentifier();

			for(MappingDefinition entry : mapping.getMappingDefinitions()) {
				for(AffectedWorkItem aWi : entry.getAffectedWorkItems()) {

					int workItem = aWi.getWorkItem().getId();
					String valueId = aWi.getMappedIdentifier();
					if(valueId != null) {
						if (attributes.containsKey(workItem)) {
							HashMap<String, String> workItemAttributes = attributes.get(workItem);
							workItemAttributes.put(attributeId, valueId);
						} else {
							HashMap<String, String> map = new HashMap<String, String>();
							map.put(attributeId, valueId);
							attributes.put(workItem, map);
						}
					}
				}
			}
		}
		return attributes;
	}

	private AttributeDefinitions analyzeWorkItems2(List<WorkItemMoveMapper> mappedWorkItems) throws TeamRepositoryException {
		AttributeDefinitions attributeDefinitions = new AttributeDefinitions();

		IRepositoryItemService itemService = service.getService(IRepositoryItemService.class);

		for(WorkItemMoveMapper mappedWorkItem : mappedWorkItems) {
			addWorkItemToAttributeDefinitions(attributeDefinitions, mappedWorkItem, itemService);
		}

		return  attributeDefinitions;
	}

	private void addWorkItemToAttributeDefinitions(AttributeDefinitions attributeDefinitions, WorkItemMoveMapper mappedWorkItem, IRepositoryItemService itemService) throws TeamRepositoryException {
		IAuditableServer auditSrv = workItemServer.getAuditableServer();
		AttributeHelpers attributeHelpers = new AttributeHelpers(service, workItemServer, monitor);

		IWorkItem sourceWorkItem = mappedWorkItem.getSourceWorkItem();
		IWorkItem targetWorkItem = mappedWorkItem.getTargetWorkItem();
		IProjectArea sourceArea = (IProjectArea) itemService.fetchItem(sourceWorkItem.getProjectArea(), null);
		IProjectArea targetArea = (IProjectArea) itemService.fetchItem(targetWorkItem.getProjectArea(), null);

		if(!sourceArea.sameItemId(targetArea)) {
			List<IAttribute> sourceAttrs = workItemServer.findAttributes(sourceArea, monitor);
			List<IAttribute> targetAttrs = workItemServer.findAttributes(targetArea, monitor);
			// read required attributes from target process
			Collection<String> requiredAttributes = workItemServer.findRequiredAttributes(targetWorkItem, null, monitor);

			for(IAttribute sourceAttr : sourceAttrs) {
				if(sourceWorkItem.hasAttribute(sourceAttr)) {
					Object sourceValue = sourceAttr.getValue(auditSrv, sourceWorkItem, monitor);
					for(IAttribute targetAttr : targetAttrs) {
						if(targetWorkItem.hasAttribute(targetAttr)) {
							Object targetValue = targetAttr.getValue(auditSrv, targetWorkItem, monitor);
							if(targetAttr.getIdentifier().equals(sourceAttr.getIdentifier())
									&& !areValuesEqual(sourceValue, targetValue)
									&& !AttributeHelpers.IGNORED_ATTRIBUTES.contains(targetAttr.getIdentifier())) {
								if(!attributeDefinitions.contains(sourceAttr.getIdentifier())) {
									AttributeDefinition definition = new AttributeDefinition(targetAttr.getIdentifier(), targetAttr.getDisplayName());
									attributeDefinitions.add(definition);
								}
								AttributeDefinition definition = attributeDefinitions.get(sourceAttr.getIdentifier());
								AttributeValue oldAttributeValue = attributeHelpers.getCurrentValueRepresentation(sourceAttr, sourceWorkItem);
								MappingDefinition mapping = definition.getMapping(oldAttributeValue.getIdentifier());
								WorkItem wi = new WorkItem(sourceWorkItem.getId(), sourceWorkItem.getHTMLSummary().getPlainText(), null);
								boolean isRequired = requiredAttributes.contains(targetAttr.getIdentifier());
								if(mapping == null) {
									MappingDefinition def = new MappingDefinition(
											oldAttributeValue, new AffectedWorkItem(wi, isRequired));
									definition.addValueMapping(def);
									List<AttributeValue> val = attributeHelpers.getAvailableOptionsPresentations(targetAttr, targetWorkItem);
									def.addAllowedValues(val);
								} else {
									mapping.addAffectedWorkItem(wi, isRequired);
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean areValuesEqual(Object sourceValue, Object targetValue) {
		return sourceValue == targetValue || (sourceValue != null && sourceValue.equals(targetValue));
	}

	@SuppressWarnings("restriction")
	public IStatus MoveAll(List<IWorkItem> workItems) throws TeamRepositoryException {
		List<IWorkItemWrapper> wrappedWorkItems = new ArrayList<IWorkItemWrapper>();
		Set<String> saveParams = new HashSet<String>();
		saveParams.add(IAdditionalSaveParameters.UPDATE_EXTENDED_RICH_TEXT);
		for(IWorkItem w : workItems) {
			wrappedWorkItems.add(new WorkItemWrapper(w, null, null, saveParams, Collections.<IAuditable>emptySet()));
		}
		return workItemServer.saveWorkItems(wrappedWorkItems);
	}
}