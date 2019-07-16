package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.operations;

import com.ibm.team.links.common.IItemReference;
import com.ibm.team.links.common.ILink;
import com.ibm.team.links.common.ILinkQueryPage;
import com.ibm.team.links.common.IReference;
import com.ibm.team.links.common.service.ILinkService;
import com.ibm.team.links.service.ILinkServiceLibrary;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.IWorkItemCommon;
import com.ibm.team.workitem.common.internal.CopyToProjectOperation;
import com.ibm.team.workitem.common.internal.model.Attachment;
import com.ibm.team.workitem.common.internal.util.EMFHelper;
import com.ibm.team.workitem.common.internal.util.PermissionContext;
import com.ibm.team.workitem.common.model.*;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.WorkItemMoveMapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;

import java.util.ArrayList;
import java.util.List;

/**
 * The Core Operation responsible for moving work items from one project area to another
 * extends the 'CopyToProjectOperation' offered by IBM out of the box
 */
@SuppressWarnings("restriction")
public class BulkMoveOperation extends CopyToProjectOperation {
    /* the display name of the operation */
	private static final String name = "BulkMoveOperation";
	/* automatically commit changes */
	private static final boolean commitChanges = true;
	/* declare that we will be moving, not copying */
	private static final boolean move = true;
	/* ... */
	private static final boolean resolveSource = false;
	/* include attachements in move procedure */
	private static final boolean moveAttachments = true;
	/* we will move work items only, so there is no need to copy them */
	private static final boolean copyAttachments = false;
    /* base jazz service for various kinds of operations */
	private TeamRawService service;
	/* the work items to be moved, we will use this to track the mapping process */
	private List<WorkItemMoveMapper> workItems;
	/* allows to monitor the progress of this process, we don't have any monitoring though */
	private IProgressMonitor monitor;

    /**
     * Constructs a Bulk Mover for operations
     * @param targetArea the project area to which the work items should be moved
     * @param service the TeamRawServices is the base jazz service required for various jazz operations
     */
	public BulkMoveOperation(IProjectAreaHandle targetArea, TeamRawService service) {
		super(name, targetArea, commitChanges, move, resolveSource, moveAttachments, copyAttachments);
		this.service = service;
		this.workItems = new ArrayList<WorkItemMoveMapper>();
		this.monitor = new NullProgressMonitor();
	}

    /**
     * Commit a work item. Will create a deep copy of the work item for being stored in the tracking list
     * The target work item data will be merged into the source work item. This ensures that work item ID remains the same
     * @param sourceWorkItem the original work item
     * @param targetWorkItem a new work item which contains the target work  item information
     * @param workItemCommon common service
     * @param monitor progress monitor
     * @throws TeamRepositoryException throws a team repo exception if commit fails for any reason
     */
	@SuppressWarnings("rawtypes")
	@Override
	protected void commit(IWorkItem sourceWorkItem, IWorkItem targetWorkItem, IWorkItemCommon workItemCommon,
			IProgressMonitor monitor) throws TeamRepositoryException {
        IWorkItem sourceItem = (IWorkItem) EMFHelper.copy(sourceWorkItem);
        EMFHelper.merge((EObject)sourceWorkItem, (EObject) targetWorkItem, EXCLUDE_ON_MOVE);
        workItems.add(new WorkItemMoveMapper(sourceItem, sourceWorkItem));
	}

    /**
     * createa a fresh work item. Not needed for move, but implementation required by base class
     * @param type the type (e.g. defect) of the work item
     * @param workItemCommon common service
     * @param monitor progress monitor
     * @return the newly created work item
     * @throws TeamRepositoryException in case of an error
     */
	@Override
	protected IWorkItem createNewWorkItem(IWorkItemType type, IWorkItemCommon workItemCommon, IProgressMonitor monitor)
			throws TeamRepositoryException {
		return ((IWorkItemServer)workItemCommon).createWorkItem2(type);
	}

    /**
     * While preparing the movement, the old and new work item are saved to this list
     * @return list containing the old and new work item for each item processed
     */
	public List<WorkItemMoveMapper> getMappedWorkItems() {
		return workItems;
	}

    /**
     * Make sure that all work item references are being resolved properly
     * @param workItem the work item for which we will be resolving all its references
     * @param workItemCommon common service
     * @param monitor progress monitor
     * @return list of resolved references
     * @throws TeamRepositoryException in case of an error
     */
	@Override
	protected IWorkItemReferences getWorkItemReferences(IWorkItem workItem, IWorkItemCommon workItemCommon,
			IProgressMonitor monitor) throws TeamRepositoryException {
		return workItemCommon.resolveWorkItemReferences(workItem, null);
	}

    /**
     * time sheet movement not implemented
     */
	protected void saveTimeSheet(ITimeSheetEntry sheet, IWorkItem source, IWorkItem target, IWorkItemCommon common) {
        // do nothing, as the timesheet data will remain in the database in the intended format
	}

    /**
     * make sure that all attachments will be moved to the new project area
     * @param sourceWorkItem the original work item to which the attachments have belonged to
     * @param targetWorkItem work item object is it will be available in the target pa after movement
     * @param workItemCommon common service
     * @param progressMonitor progress monitor
     * @throws TeamRepositoryException whenever an attachment can't be moved
     */
	@Override
	protected void handleAttachments(IWorkItem sourceWorkItem, IWorkItem targetWorkItem, IWorkItemCommon workItemCommon,
			IProgressMonitor progressMonitor) throws TeamRepositoryException {
		ILinkServiceLibrary lsl = (ILinkServiceLibrary) this.service.getService(ILinkService.class)
                .getServiceLibrary(ILinkServiceLibrary.class);
		IWorkItemReferences references = workItemCommon.resolveWorkItemReferences(sourceWorkItem, monitor);
        for (IReference attachmentReference : references.getReferences(WorkItemEndPoints.ATTACHMENT)) {
            if (!attachmentReference.isItemReference()) continue;
            IAttachmentHandle attachmentHandle = (IAttachmentHandle)((IItemReference)attachmentReference)
                    .getReferencedItem();
            IAttachment attachment = workItemCommon.getAuditableCommon()
                    .resolveAuditable(attachmentHandle, IAttachment.SMALL_PROFILE, monitor);
            if (commitChanges) {
                this.moveAttachment(targetWorkItem, workItemCommon, attachment);
                continue;
            }
            this.checkAttachmentReferences(attachmentReference, lsl.findLinksBySource(attachmentReference), sourceWorkItem);
            this.checkAttachmentReferences(attachmentReference, lsl.findLinksByTarget(attachmentReference), sourceWorkItem);
        }
	}

    /**
     * move attachment to the specified work item
     * @param targetWorkItem target workitem to which attachment will be assigned to
     * @param workItemCommon: service supporint attachment save
     * @param attachment: the attachment to be moved
     * @throws TeamRepositoryException in case of an unexprected error
     */
    private void moveAttachment(IWorkItem targetWorkItem, IWorkItemCommon workItemCommon, IAttachment attachment) throws TeamRepositoryException {
        Attachment workingCopy = (Attachment)attachment.getWorkingCopy();
        workingCopy.setProjectArea(targetWorkItem.getProjectArea());
        PermissionContext.setDefault(workingCopy);
        workItemCommon.saveAttachment(workingCopy, monitor);
    }

    /**
     * Attachment references validation routine
     * @param attachmentReference: references the currently processed attachment
     * @param links: links pointing to the attachment which have to be updated
     * @param workItem: the work item to which the attachments belong to
     * @throws TeamRepositoryException if attachment validation results in an error
     */
    private void checkAttachmentReferences(IReference attachmentReference, ILinkQueryPage links, IWorkItem workItem) throws TeamRepositoryException {
        while (links != null) {
            for (ILink link : links.getLinks()) {
                IReference reference = link.getOtherRef(attachmentReference);
                if (reference.isItemReference() && workItem.sameItemId(((IItemReference)reference).getReferencedItem())) continue;
                this.addCopyProblem(new CopyToProjectOperation.CopyProblem("attachment reference copy problem occured"));
            }
            links = links.next();
        }
    }
}