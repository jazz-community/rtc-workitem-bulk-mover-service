package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.model.CategoryId;
import com.ibm.team.workitem.common.model.ICategory;
import com.ibm.team.workitem.common.model.ICategoryHandle;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CategoryHelpers {

    public static final AttributeValue getCategory(Object t_val,
                                      IWorkItemServer workItemServer, TeamRawService service, IProgressMonitor monitor) throws TeamRepositoryException {
        IRepositoryItemService itemService = service.getService(IRepositoryItemService.class);
        ICategoryHandle catHandle = (ICategoryHandle)t_val;
        ICategory category = (ICategory) itemService.fetchItem(catHandle, null);
        CategoryId id = category.getCategoryId();
        String idString = id.getInternalRepresentation();
        String fullPathname = workItemServer.resolveHierarchicalName(category, monitor);
        return new AttributeValue(idString, fullPathname);
    }

    public static final void setCategory(IWorkItem workItem, String categoryId,
                            IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IProjectAreaHandle ipa = workItem.getProjectArea();
        CategoryId cid =  CategoryId.createCategoryId(categoryId);
        ICategoryHandle cat = workItemServer.findCategoryById2(ipa, cid, monitor);
        workItem.setCategory(cat);
    }

    public static final List<AttributeValue> addCategoriesAsValues(IProjectAreaHandle pa,
                                                      IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        List<ICategory> categories = workItemServer.findCategories(pa, ICategory.SMALL_PROFILE, monitor);
        for (ICategory category : categories) {
            if (category.isArchived() || category.isUnassigned())
                continue;
            CategoryId id = category.getCategoryId();
            String idString = id.getInternalRepresentation();
            String fullPathname = workItemServer.resolveHierarchicalName(category, monitor);
            values.add(new AttributeValue(idString, fullPathname));
        }
        return values;
    }
}
