package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.IAuditable;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.internal.IAdditionalSaveParameters;
import com.ibm.team.workitem.common.internal.model.WorkItemAttributes;
import com.ibm.team.workitem.common.internal.util.EMFHelper;
import com.ibm.team.workitem.common.model.AttributeTypes;
import com.ibm.team.workitem.common.model.IAttribute;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.service.IAuditableServer;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.ibm.team.workitem.service.IWorkItemWrapper;
import com.ibm.team.workitem.service.internal.WorkItemWrapper;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers.AttributeHelpers;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers.CategoryHelpers;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers.WorkItemTypeHelpers;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.*;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.operations.BulkMoveOperation;
import com.siemens.bt.jazz.services.WorkItemBulkMover.rtc.models.WorkItem;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

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
     * @throws TeamRepositoryException if anything with RTC goes wrong
     */
	@SuppressWarnings("restriction")
	public MovePreparationResult PrepareMove(List<IWorkItem> sourceWorkItems, IProjectAreaHandle targetArea, List<AttributeDefinition> mappingDefinitions, Map<String, String> typeMap) throws TeamRepositoryException {
		// run the bulk movement operation
		BulkMoveOperation oper = new BulkMoveOperation(targetArea, service);
		oper.run(sourceWorkItems, workItemServer, monitor);

		// get the workitems involved into the move operation
		List<WorkItemMoveMapper> workItems = oper.getMappedWorkItems();
		if(workItems.size() < sourceWorkItems.size()) {
			List<Integer> mappedIds = new ArrayList<Integer>();
			for(WorkItemMoveMapper entry : workItems) {
				mappedIds.add(new Integer(entry.getId()));
			}

			for(IWorkItem sourceWorkItem : sourceWorkItems) {
				if(!mappedIds.contains(sourceWorkItem.getId())) {
					IWorkItem sourceItem = (IWorkItem) EMFHelper.copy(sourceWorkItem);
					workItems.add(new WorkItemMoveMapper(sourceItem, sourceWorkItem));
				}
			}
		}

		// update work item type according to client mapping
		for(WorkItemMoveMapper entry : workItems) {
			String sourceType = entry.getSourceWorkItem().getWorkItemType();
			String targetType = typeMap.get(sourceType);

			if(entry.getSourceWorkItem().getProjectArea().sameItemId(targetArea)
			        && sourceType.equals(targetType)) {
			    throw new TeamRepositoryException("No move possible if source and target project area AND source and target work item type (" + targetType + ") are the same");
            }

			WorkItemTypeHelpers.setWorkItemType(entry.getSourceWorkItem(), entry.getTargetWorkItem(), sourceType, targetType, workItemServer, monitor);
		}

		if(mappingDefinitions != null && mappingDefinitions.size() > 0) {
			// if the user has sent mappings for specific attributes, apply them now
			applyMappingsToWorkItems(workItems, mappingDefinitions);
		}

		// compare source and target work items to track what has changed
		Collection<AttributeDefinition> anRes = analyzeWorkItems2(workItems, mappingDefinitions);

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

	private Collection<AttributeDefinition> analyzeWorkItems2(List<WorkItemMoveMapper> mappedWorkItems, List<AttributeDefinition> attributeDefinitions) throws TeamRepositoryException {
		IRepositoryItemService itemService = service.getService(IRepositoryItemService.class);

		for(WorkItemMoveMapper mappedWorkItem : mappedWorkItems) {
			addWorkItemToAttributeDefinitions(attributeDefinitions, mappedWorkItem, itemService);
		}

		return attributeDefinitions;
	}

	private void addWorkItemToAttributeDefinitions(List<AttributeDefinition> attributeDefinitions, WorkItemMoveMapper mappedWorkItem, IRepositoryItemService itemService) throws TeamRepositoryException {
		IAuditableServer auditSrv = workItemServer.getAuditableServer();
		AttributeHelpers attributeHelpers = new AttributeHelpers(service, workItemServer, monitor);

		IWorkItem sourceWorkItem = mappedWorkItem.getSourceWorkItem();
		IWorkItem targetWorkItem = mappedWorkItem.getTargetWorkItem();
		IProjectArea sourceArea = (IProjectArea) itemService.fetchItem(sourceWorkItem.getProjectArea(), null);
		IProjectArea targetArea = (IProjectArea) itemService.fetchItem(targetWorkItem.getProjectArea(), null);

		List<String> mappedAttrIds = new ArrayList<String>();
		List<IAttribute> sourceAttrs = workItemServer.findAttributes(sourceArea, monitor);
		List<IAttribute> targetAttrs = workItemServer.findAttributes(targetArea, monitor);
		Collection<String> requiredAttributes = workItemServer.findRequiredAttributes(targetWorkItem, null, monitor);
        List<String> alwaysMap = new ArrayList<String>();
		List<IAttribute> requiredAttrs = new ArrayList<IAttribute>();

		if(!sourceWorkItem.getWorkItemType().equals(targetWorkItem.getWorkItemType())) {
			IAttribute stateAttr = workItemServer.findAttribute(targetWorkItem.getProjectArea(), IWorkItem.STATE_PROPERTY, null);
			requiredAttrs.add(stateAttr);
            alwaysMap.add(IWorkItem.STATE_PROPERTY);
            alwaysMap.add(IWorkItem.RESOLUTION_PROPERTY);
		}

		for(IAttribute sourceAttr : sourceAttrs) {
			if(sourceWorkItem.hasAttribute(sourceAttr)) {
				mappedAttrIds.add(sourceAttr.getIdentifier());
				Object sourceValue = sourceAttr.getValue(auditSrv, sourceWorkItem, monitor);
				for(IAttribute targetAttr : targetAttrs) {
					if(targetWorkItem.hasAttribute(targetAttr)) {
						boolean eq1 = targetAttr.getIdentifier().equals(sourceAttr.getIdentifier());
						if(!eq1)
							continue; // early exit
						Object targetValue = targetAttr.getValue(auditSrv, targetWorkItem, monitor);
						boolean isRequired = requiredAttributes.contains(targetAttr.getIdentifier());
						boolean eq2a = areBothNullButRequired(isRequired, sourceValue, targetValue);
						boolean eq2b = isTargetRequiredButValueUnassigned(isRequired, targetAttr, targetValue);
						boolean eq2c = !areValuesEqual(sourceValue, targetValue);
						boolean eq3 = AttributeHelpers.IGNORED_ATTRIBUTES.contains(targetAttr.getIdentifier());
						if(eq1 && (eq2a || eq2b || eq2c) && !eq3) {
							if(isRequired) {
								requiredAttributes.remove(targetAttr.getIdentifier());
								requiredAttrs.add(targetAttr);
							}
							CreateAttributeDefinition(attributeDefinitions, attributeHelpers, sourceWorkItem, targetWorkItem, sourceAttr, targetAttr, isRequired);
						}
					}
				}
			}
		}

		for(IAttribute reqAttr : requiredAttrs) {
			if(!mappedAttrIds.contains(reqAttr.getIdentifier())) {
				CreateAttributeDefinition(attributeDefinitions, attributeHelpers, sourceWorkItem, targetWorkItem, null, reqAttr, true);
			} else if(alwaysMap.contains(reqAttr.getIdentifier())) {
			    IAttribute sourceAttr = workItemServer.findAttribute(sourceWorkItem.getProjectArea(), reqAttr.getIdentifier(), null);
                CreateAttributeDefinition(attributeDefinitions, attributeHelpers, sourceWorkItem, targetWorkItem, sourceAttr, reqAttr, true);
            }
		}
	}

	private void CreateAttributeDefinition(List<AttributeDefinition> attributeDefinitions, AttributeHelpers attributeHelpers, IWorkItem sourceWorkItem, IWorkItem targetWorkItem, IAttribute sourceAttr, IAttribute targetAttr, boolean isRequired) throws TeamRepositoryException {
		boolean isPrimitive = AttributeHelpers.isPrimitiveCustomAttributeType(targetAttr);
		int idx = attributeDefinitions.indexOf(new AttributeDefinition(targetAttr.getIdentifier(), isPrimitive));
		if(idx < 0) {
            AttributeDefinition definition = new AttributeDefinition(targetAttr.getIdentifier(), targetAttr.getDisplayName(), isPrimitive);
            attributeDefinitions.add(definition);
            idx = attributeDefinitions.indexOf(definition);
        }
		AttributeDefinition definition = attributeDefinitions.get(idx);
		AttributeValue oldAttributeValue = attributeHelpers.getCurrentValueRepresentation(sourceAttr, sourceWorkItem);
		MappingDefinition mapping = definition.getMapping(oldAttributeValue.getIdentifier());
		WorkItem wi = new WorkItem(sourceWorkItem.getId(), sourceWorkItem.getHTMLSummary().getPlainText(), null);
		if(mapping == null) {
            List<AttributeValue> val = attributeHelpers.getAvailableOptionsPresentations(targetAttr, targetWorkItem);
			MappingDefinition def = new MappingDefinition(
					oldAttributeValue, new AffectedWorkItem(wi, isRequired), isPrimitive);
			definition.addValueMapping(def);
            if(val != null && val.size() > 0) {
                def.addAllowedValues(val);
            } else {
            	def.setError("No items available!");
			}
        } else {
            List<AffectedWorkItem> mappings = mapping.getAffectedWorkItems();
            int wiIdx = mappings.indexOf(new AffectedWorkItem(wi, isRequired));
            if(wiIdx < 0) {
                mapping.addAffectedWorkItem(wi, isRequired);
            }
        }
	}

	private boolean isTargetRequiredButValueUnassigned(boolean isRequired, IAttribute attribute, Object targetValue) throws TeamRepositoryException {
		return isRequired && AttributeTypes.CATEGORY.equals(attribute.getAttributeType()) && CategoryHelpers.isArchivedOrUnassigned(targetValue, service);
	}

	private boolean areBothNullButRequired(boolean isReuqired, Object sourceValue, Object targetValue) {
		return sourceValue == null && targetValue == null && isReuqired;
	}

	private boolean areValuesEqual(Object sourceValue, Object targetValue) {
		return sourceValue == targetValue || (sourceValue != null && sourceValue.equals(targetValue));
	}

	@SuppressWarnings("restriction")
	public IStatus MoveAll(List<IWorkItem> workItems, Set<String> saveParams) throws TeamRepositoryException {
		List<IWorkItemWrapper> wrappedWorkItems = new ArrayList<IWorkItemWrapper>();
		for(IWorkItem w : workItems) {
			wrappedWorkItems.add(new WorkItemWrapper(w, null, null, saveParams, Collections.<IAuditable>emptySet()));
		}
		return workItemServer.saveWorkItems(wrappedWorkItems);
	}
}