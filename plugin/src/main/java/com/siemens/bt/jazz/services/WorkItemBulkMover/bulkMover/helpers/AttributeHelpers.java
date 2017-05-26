package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IIteration;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.IItem;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.internal.model.WorkItemAttributes;
import com.ibm.team.workitem.common.internal.util.IterationsHelper;
import com.ibm.team.workitem.common.model.*;
import com.ibm.team.workitem.common.workflow.ICombinedWorkflowInfos;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;
import com.ibm.team.workitem.service.IAuditableServer;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.*;

public final class AttributeHelpers {
    TeamRawService teamRawService;
    IWorkItemServer workItemServer;
    IProgressMonitor monitor;

    public AttributeHelpers(TeamRawService teamRawService, IWorkItemServer workItemServer, IProgressMonitor monitor) {
        this.teamRawService = teamRawService;
        this.workItemServer = workItemServer;
        this.monitor = monitor;
    }

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
            // TODO describe why the properties below can be safely ignored
            IWorkItem.CUSTOM_ATTRIBUTES_PROPERTY, //TODO make dynamic
            IWorkItem.CONTEXT_ID_PROPERTY,
            IItem.ITEM_ID_PROPERTY,
            IWorkItem.STATE_TRANSITIONS_PROPERTY
    ));

    @SuppressWarnings("restriction")
    public void setAttributeForWorkItem(IWorkItem sourceWorkItem, IWorkItem targetWorkItem, String attributeId, String valueId) throws TeamRepositoryException {
        IProjectAreaHandle paHandle = targetWorkItem.getProjectArea();
        IAttribute attribute = workItemServer.findAttribute(paHandle, attributeId, monitor);
        Identifier<IAttribute> identifier = WorkItemAttributes.getPropertyIdentifier(attribute.getIdentifier());

        if(valueId != null) {
            if (WorkItemAttributes.TYPE.equals(identifier)) {
                WorkItemTypeHelpers.setWorkItemType(targetWorkItem, sourceWorkItem.getWorkItemType(), valueId, workItemServer, monitor);
            } else if (WorkItemAttributes.RESOLUTION.equals(identifier)) {
                ResolutionHelpers.setResolution(targetWorkItem, valueId, workItemServer, monitor);
            } else if (WorkItemAttributes.STATE.equals(identifier)) {
                StateHelpers.setState(targetWorkItem, valueId, workItemServer, monitor);
            } else if (WorkItemAttributes.CATEGORY.equals(identifier)) {
                CategoryHelpers.setCategory(targetWorkItem, valueId, workItemServer, monitor);
            } else if (WorkItemAttributes.TARGET.equals(identifier)) {
                TargetHelpers.setTarget(targetWorkItem, valueId, workItemServer, monitor);
            } else if (LiteralHelpers.isValidLiteral(attribute)) {
                LiteralHelpers.setLiteral(targetWorkItem, attributeId, valueId, workItemServer, monitor);
            }
        }
    }


    @SuppressWarnings("restriction")
    public AttributeValue getCurrentValueRepresentation(IAttribute attribute, IWorkItem workItem) throws TeamRepositoryException {
        IAuditableServer auditSrv = workItemServer.getAuditableServer();
        Object attributeValue = attribute.getValue(auditSrv, workItem, monitor);
        IProjectAreaHandle pa = workItem.getProjectArea();
        Identifier<IAttribute> identifier = WorkItemAttributes.getPropertyIdentifier(attribute.getIdentifier());
        AttributeValue value = new AttributeValue("", "");;

        if(attributeValue != null) {
            if (WorkItemAttributes.TYPE.equals(identifier)) {
                value = WorkItemTypeHelpers.getWorkItemType(attributeValue, pa, workItemServer, monitor);
            } else if (WorkItemAttributes.RESOLUTION.equals(identifier)) {
                value = ResolutionHelpers.getResolution(attributeValue, workItem, workItemServer, monitor);
            } else if (WorkItemAttributes.STATE.equals(identifier)) {
                value = StateHelpers.getState(attributeValue, workItem, workItemServer, monitor);
            } else if (WorkItemAttributes.CATEGORY.equals(identifier)) {
                value = CategoryHelpers.getCategory(attributeValue, workItemServer, teamRawService, monitor);
            } else if (WorkItemAttributes.TARGET.equals(identifier)) {
                value = TargetHelpers.getTarget(attributeValue, auditSrv, workItemServer, teamRawService, monitor);
            } else if (WorkItemAttributes.CUSTOM_ATTRIBUTES.equals(identifier)) {
                //handleCustomAttributes(attribute, workItem);
            } else {
                value = LiteralHelpers.getLiteral(attribute, attributeValue, workItemServer, monitor);
            }
        }
        return value;
    }

    @SuppressWarnings("restriction")
    public List<AttributeValue> getAvailableOptionsPresentations(IAttribute attribute, IWorkItem workItem) throws TeamRepositoryException {
        IProjectAreaHandle pa = workItem.getProjectArea();
        List<AttributeValue> values;
        Identifier<IAttribute> identifier = WorkItemAttributes.getPropertyIdentifier(attribute.getIdentifier());

        if (WorkItemAttributes.TYPE.equals(identifier)) {
            values = WorkItemTypeHelpers.addWorkItemTypesAsValues(pa, workItemServer, monitor);
        } else if (WorkItemAttributes.RESOLUTION.equals(identifier)) {
            values = ResolutionHelpers.addResolutionsAsValues(pa, workItemServer, monitor);
        } else if (WorkItemAttributes.STATE.equals(identifier)) {
            values = StateHelpers.addStatesAsValues(pa, workItem, workItemServer, monitor);
        } else if (WorkItemAttributes.CATEGORY.equals(identifier)) {
            values = CategoryHelpers.addCategoriesAsValues(pa, workItemServer, monitor);
        } else if (WorkItemAttributes.TARGET.equals(identifier)) {
            values = TargetHelpers.addTargetsAsValues(pa, workItemServer, monitor);
        } else if (WorkItemAttributes.CUSTOM_ATTRIBUTES.equals(identifier)) {
            values = handleCustomAttributes(attribute, workItem);
        } else {
            values = LiteralHelpers.addLiteralsAsValues(attribute, workItemServer, monitor);
        }
        return values;
    }

    /*
     * TODO fix this
     */
    public List<AttributeValue> handleCustomAttributes(IAttribute attribute, IWorkItem workItem) throws TeamRepositoryException {
        List<AttributeValue> vla = new ArrayList<AttributeValue>();
        IAuditableServer auditSrv = workItemServer.getAuditableServer();
        IRepositoryItemService itemService = teamRawService.getService(IRepositoryItemService.class);

        Object s_val = attribute.getValue(auditSrv, workItem, monitor);
        if(s_val instanceof List) {
            @SuppressWarnings("unchecked")
            List<IAttributeHandle> customAttrs = (List<IAttributeHandle>)s_val;
            for(IAttributeHandle attrHandle: customAttrs) {
                IAttribute attr = (IAttribute) itemService.fetchItem(attrHandle, null);
                AttributeValue attrVal = getCurrentValueRepresentation(attr, workItem);
                vla = getAvailableOptionsPresentations(attr, workItem);
                String id = attr.getIdentifier();
                Object val = attr.getValue(auditSrv, workItem, monitor);
                Class c = val.getClass();
                //ILiteral lit = (ILiteral)val;
                Identifier<? extends ILiteral> identifier = (Identifier<? extends ILiteral>)val;
                String sid = identifier.getStringIdentifier();
                IEnumeration<? extends ILiteral> enumeration = workItemServer.resolveEnumeration(attr, monitor);
                List<? extends ILiteral> list = enumeration.getEnumerationLiterals(false);
                for (ILiteral literal : list) {
                    if(sid.equals(literal.getIdentifier2().getStringIdentifier())) {
                        String chosenLit = literal.getName();
                        chosenLit.toLowerCase();
                    }
                }
//				if(val instanceof LiteralImpl) {
//					LiteralImpl limpl = (LiteralImpl)val;
//					String str = limpl.getString();
//					Object limplVal = limpl.getValue();
//				}
                boolean issame = id ==val;
            }
        } else {
            vla = vla;
        }
        return vla;
    }
}
