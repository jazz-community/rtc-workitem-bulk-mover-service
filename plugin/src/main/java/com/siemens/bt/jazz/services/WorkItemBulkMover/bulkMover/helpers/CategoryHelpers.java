package com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.helpers;

import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.repository.service.IRepositoryItemService;
import com.ibm.team.repository.service.TeamRawService;
import com.ibm.team.workitem.common.model.*;
import com.ibm.team.workitem.service.IWorkItemServer;
import com.siemens.bt.jazz.services.WorkItemBulkMover.bulkMover.models.AttributeValue;
import org.eclipse.core.runtime.IProgressMonitor;

import java.util.ArrayList;
import java.util.List;

public final class CategoryHelpers {

    static AttributeValue getCategory(Object t_val,
                                      IWorkItemServer workItemServer, TeamRawService service, IProgressMonitor monitor) throws TeamRepositoryException {
        IRepositoryItemService itemService = service.getService(IRepositoryItemService.class);
        ICategoryHandle catHandle = (ICategoryHandle)t_val;
        ICategory category = (ICategory) itemService.fetchItem(catHandle, null);
        CategoryId id = category.getCategoryId();
        String idString = id.getInternalRepresentation();
        String fullPathname = workItemServer.resolveHierarchicalName(category, monitor);
        return new AttributeValue(idString, fullPathname);
    }

    static void setCategory(IWorkItem workItem, IAttribute attribute, String categoryId,
                            IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        IProjectAreaHandle ipa = workItem.getProjectArea();
        if(isValidCategoryId(categoryId)) {
            CategoryId cid =  CategoryId.createCategoryId(categoryId);
            ICategoryHandle cat = workItemServer.findCategoryById2(ipa, cid, monitor);
            workItem.setValue(attribute, cat);
        }
    }

    static List<AttributeValue> addCategoriesAsValues(IProjectAreaHandle pa,
                                                      IWorkItemServer workItemServer, IProgressMonitor monitor) throws TeamRepositoryException {
        List<AttributeValue> values = new ArrayList<AttributeValue>();
        List<ICategory> categories = workItemServer.findCategories(pa, ICategory.SMALL_PROFILE, monitor);
        for (ICategory category : categories) {
            if (category.isArchived() || category.isUnassigned())
                continue;
            CategoryId id = category.getCategoryId();
            String idString = id.getInternalRepresentation();
            String name = category.getName();
            values.add(new AttributeValue(idString, name));
        }
        return values;
    }

    public static boolean isArchivedOrUnassigned(Object value, TeamRawService service) throws TeamRepositoryException {
        IRepositoryItemService itemService = service.getService(IRepositoryItemService.class);
        ICategoryHandle catHandle = (ICategoryHandle)value;
        ICategory category = (ICategory) itemService.fetchItem(catHandle, null);
        return category.isArchived() || category.isUnassigned();
    }

    private static boolean isValidCategoryId(String string) {
        return string.startsWith("/") && string.indexOf("/", "/".length()) != -1;
    }
}
