package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.ChildTopics;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.accesscontrol.SharingMode;
import de.deepamehta.plugins.accesscontrol.AccessControlService;
import de.deepamehta.plugins.workspaces.WorkspacesService;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


public class Migration8 extends Migration {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger log = Logger.getLogger(getClass().getName());

    static final String FAMPORTAL_WORKSPACE_NAME = "Familienportal";
    static final String FAMPORTAL_WORKSPACE_URI = "de.kiezatlas.familienportal_ws";
    static final SharingMode FAMPORTAL_WORKSPACE_SHARING_MODE = SharingMode.CONFIDENTIAL;

    @Inject private WorkspacesService workspaceService;

    @Inject private AccessControlService accessControlService;

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {

        Topic famportalWorkspace = workspaceService.createWorkspace(FAMPORTAL_WORKSPACE_NAME, FAMPORTAL_WORKSPACE_URI,
                FAMPORTAL_WORKSPACE_SHARING_MODE);
        accessControlService.setWorkspaceOwner(famportalWorkspace, "admin");
        // These types do NOT need a workspace assignment (as long as they stay readable)
        /** TopicType famportalCategoryType = dms.getTopicType("famportal.category");
        TopicType categoryOrderType = dms.getTopicType("famportal.category.order");
        TopicType categoryNameType = dms.getTopicType("famportal.category.name");
        TopicType categoryFacetType = dms.getTopicType("famportal.category.facet"); **/
        // ### Types miss workspaceAssignments to any workspace
        Topic categoryRoot = dms.getTopic("uri", new SimpleValue("famportal.root"));
        workspaceService.assignToWorkspace(categoryRoot, famportalWorkspace.getId());
        //
        List<Topic> categories = dms.getTopics("uri", new SimpleValue("famportal.category*"));
        Iterator<Topic> i = categories.iterator();
        while (i.hasNext()) {
            Topic topic = i.next();
            workspaceService.assignToWorkspace(topic, famportalWorkspace.getId());
            log.info("Assigned famportal category " + topic.getSimpleValue() + " to confidential workspace " +
                    "\"Familienportal\"");
            ChildTopics childs = topic.loadChildTopics().getChildTopics();
            if (childs.has("famportal.category.name")) {
                Topic nameTopic = childs.getTopic("famportal.category.name");
                Topic orderTopic = childs.getTopic("famportal.category.order");
                workspaceService.assignToWorkspace(nameTopic, famportalWorkspace.getId());
                workspaceService.assignToWorkspace(orderTopic, famportalWorkspace.getId());
                log.info("Assigned famportal name & order childs " + topic.getSimpleValue() + " to confidential " +
                        "workspace " +
                        "\"Familienportal\"");
            }
        }
        // ### Fix association (famportal.category to geo-objects) workspace assignments, too.

    }
    
}
