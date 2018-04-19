package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.internal.model.WorkItemAttributes;
import com.ibm.team.workitem.common.model.*;
import com.ibm.team.workitem.service.IAuditableServer;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.*;

public final class AttributeHelpers {
    private TeamRawService teamRawService;
    private IWorkItemServer workItemServer;
    private IProgressMonitor monitor;

    public AttributeHelpers(TeamRawService teamRawService, IWorkItemServer workItemServer, IProgressMonitor monitor) {
        this.teamRawService = teamRawService;
        this.workItemServer = workItemServer;
        this.monitor = monitor;
    }

    private static final Set<String> BUILT_IN_ATTRIBUTES_TO_IGNORE = new HashSet<String>(Arrays.asList(
            IWorkItem.STATE_PROPERTY,
            IWorkItem.RESOLUTION_PROPERTY
    ));

    /**
     * The attributes which can be safely ingored for movement scenarios
     */
    public static final Set<String> IGNORED_ATTRIBUTES = new HashSet<String>(Arrays.asList(
            // id will be the same, as we do a move and not a copy
            IWorkItem.ID_PROPERTY,
            // creation and modification information is set automatically on save
            IWorkItem.MODIFIED_BY_PROPERTY,
            IWorkItem.MODIFIED_PROPERTY,
            IWorkItem.CREATION_DATE_PROPERTY,
            // target project area is provided by the user
            IWorkItem.PROJECT_AREA_PROPERTY,
            // approvals are type and project area independent, no changes needed
            IWorkItem.APPROVAL_DESCRIPTORS_PROPERTY,
            IWorkItem.APPROVALS_PROPERTY,
            // comments are type and project are independent, no changes needed
            IWorkItem.COMMENTS_PROPERTY,
            // custom attributes are not moveable if they do not exist in the target area
            IWorkItem.CUSTOM_ATTRIBUTES_PROPERTY,
            // type is already processed before
            IWorkItem.TYPE_PROPERTY,
            // the following attributes are automatically tracked by the server itself
            IWorkItem.CONTEXT_ID_PROPERTY,
            IItem.ITEM_ID_PROPERTY,
            IWorkItem.STATE_TRANSITIONS_PROPERTY
    ));

    @SuppressWarnings("restriction")
    public void setAttributeForWorkItem(IWorkItem targetWorkItem, String attributeId, String valueId) throws TeamRepositoryException {
        IProjectAreaHandle paHandle = targetWorkItem.getProjectArea();
        IAttribute attribute = workItemServer.findAttribute(paHandle, attributeId, monitor);
        Identifier<IAttribute> identifier = WorkItemAttributes.getPropertyIdentifier(attribute.getIdentifier());
        String type = attribute.getAttributeType();

        if(valueId != null) {
            if (WorkItemAttributes.RESOLUTION.equals(identifier)) {
                ResolutionHelpers.setResolution(targetWorkItem, valueId, workItemServer, monitor);
            } else if (WorkItemAttributes.STATE.equals(identifier)) {
                StateHelpers.setState(targetWorkItem, valueId, workItemServer, monitor);
            } else if (AttributeTypes.CATEGORY.equals(type)) {
                CategoryHelpers.setCategory(targetWorkItem, attribute, valueId, workItemServer, monitor);
            } else if (AttributeTypes.ITERATION.equals(type)) {
                TargetHelpers.setTarget(targetWorkItem, attribute, valueId, workItemServer);
            } else if (AttributeTypes.DELIVERABLE.equals(type)) {
                FoundInHelpers.setFoundIn(targetWorkItem, attribute, valueId, workItemServer, monitor);
            } else if(EnumerationHelpers.isValidEnumerationLiteral(attribute)) {
                EnumerationHelpers.setEnumerationLiteral(targetWorkItem, attributeId, valueId, workItemServer, monitor);
            } else if (isPrimitiveCustomAttributeType(attribute)) {
                PrimitiveHelpers.setPrimitive(targetWorkItem, attribute, valueId);
            }
        }
    }

    @SuppressWarnings("restriction")
    public AttributeValue getCurrentValueRepresentation(IAttribute attribute, IWorkItem workItem) throws TeamRepositoryException {
        IAuditableServer auditSrv = workItemServer.getAuditableServer();
        AttributeValue value = new AttributeValue("", "");

        if(attribute != null) {
            String type = attribute.getAttributeType();
            Object attributeValue = attribute.getValue(auditSrv, workItem, monitor);
            Identifier<IAttribute> identifier = WorkItemAttributes.getPropertyIdentifier(attribute.getIdentifier());

            if (attributeValue != null) {
                if (WorkItemAttributes.RESOLUTION.equals(identifier)) {
                    value = ResolutionHelpers.getResolution(attributeValue, workItem, workItemServer, monitor);
                } else if (WorkItemAttributes.STATE.equals(identifier)) {
                    value = StateHelpers.getState(attributeValue, workItem, workItemServer, monitor);
                } else if (AttributeTypes.CATEGORY.equals(type)) {
                    value = CategoryHelpers.getCategory(attributeValue, workItemServer, teamRawService, monitor);
                } else if (AttributeTypes.ITERATION.equals(type)) {
                    value = TargetHelpers.getTarget(attributeValue, auditSrv, teamRawService, monitor);
                } else if (AttributeTypes.DELIVERABLE.equals(type)) {
                    value = FoundInHelpers.getFoundIn(attributeValue, teamRawService);
                } else if (EnumerationHelpers.isValidEnumerationLiteral(attribute)) {
                    value = EnumerationHelpers.getEnumerationLiteral(attribute, attributeValue, workItemServer, monitor);
                } else if (isPrimitiveCustomAttributeType(attribute)) {
                    value = PrimitiveHelpers.getPrimitive(workItem, attribute);
                }
            }
        }
        return value;
    }

    @SuppressWarnings("restriction")
    public List<AttributeValue> getAvailableOptionsPresentations(IAttribute attribute, IWorkItem workItem) throws TeamRepositoryException {
        IProjectAreaHandle pa = workItem.getProjectArea();
        List<AttributeValue> values;
        Identifier<IAttribute> identifier = WorkItemAttributes.getPropertyIdentifier(attribute.getIdentifier());
        String type = attribute.getAttributeType();

        if (WorkItemAttributes.RESOLUTION.equals(identifier)) {
            values = ResolutionHelpers.addResolutionsAsValues(pa, workItemServer, monitor);
        } else if (WorkItemAttributes.STATE.equals(identifier)) {
            values = StateHelpers.addStatesAsValues(pa, workItem, workItemServer, monitor);
        } else if (AttributeTypes.CATEGORY.equals(type)) {
            values = CategoryHelpers.addCategoriesAsValues(pa, workItemServer, monitor);
        } else if (AttributeTypes.ITERATION.equals(type)) {
            values = TargetHelpers.addTargetsAsValues(pa, workItemServer, monitor);
        } else if (AttributeTypes.DELIVERABLE.equals(type)) {
            values = FoundInHelpers.addFoundInAsValues(pa, workItemServer, monitor);
        } else if(EnumerationHelpers.isValidEnumerationLiteral(attribute)) {
            values = EnumerationHelpers.addEnumerationLiteralsAsValues(attribute, workItemServer, monitor);
        } else if (isPrimitiveCustomAttributeType(attribute)) {
            values = null; // not needed for primitives
        } else {
            values = null;
        }
        return values;
    }

    public static boolean isPrimitiveCustomAttributeType(IAttribute attribute) {
        String type = attribute.getAttributeType();
        return (AttributeTypes.isPrimitiveAttributeType(type)
                && !AttributeTypes.CATEGORY.equals(type)
                && !AttributeTypes.ITERATION.equals(type)
                && !AttributeTypes.DELIVERABLE.equals(type)
                && !EnumerationHelpers.isValidEnumerationLiteral(attribute)
                && !BUILT_IN_ATTRIBUTES_TO_IGNORE.contains(attribute.getIdentifier()));
    }
}
