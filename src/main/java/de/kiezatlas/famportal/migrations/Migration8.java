package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.ChildTopics;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.SimpleValue;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.accesscontrol.SharingMode;
import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.core.TopicType;
import de.deepamehta.workspaces.WorkspacesService;

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
        Topic categoryRoot = dm4.getTopicByUri("famportal.root");
        workspaceService.assignToWorkspace(categoryRoot, famportalWorkspace.getId());
        //
        List<Topic> famportalTopics = dm4.getTopicsByValue("uri", new SimpleValue("famportal.category*"));
        Iterator<Topic> i = famportalTopics.iterator();
        while (i.hasNext()) {
            Topic categoryTopic = i.next();
            /** Check if the types are assigned allright in 4.7 */
            if (categoryTopic.getUri().equals("famportal.category.name")
                || categoryTopic.getUri().equals("famportal.category.order")
                || categoryTopic.getUri().equals("famportal.category.facet")
                || categoryTopic.getUri().equals("famportal.category.category")
                || categoryTopic.getUri().equals("famportal.category")) {
                // These types do NOT NEED a workspace assignment (as long as they stay readable)
                TopicType famportalTypeTopic = dm4.getTopicType(categoryTopic.getUri());
                workspaceService.assignTypeToWorkspace(famportalTypeTopic, famportalWorkspace.getId());
                log.info("Assigned famportal topic type " + categoryTopic.getSimpleValue() + " to confidential workspace " +
                    "\"Familienportal\"");
            } else {
                workspaceService.assignToWorkspace(categoryTopic, famportalWorkspace.getId());
                log.info("Assigned famportal category " + categoryTopic.getSimpleValue() + " (uri:"+categoryTopic.getUri()+","
                    + "typeUri:"+categoryTopic.getTypeUri()+") to confidential workspace \"Familienportal\"");
                ChildTopics childs = categoryTopic.loadChildTopics().getChildTopics();
                if (childs.getTopicOrNull("famportal.category.name") != null) {
                    Topic nameTopic = childs.getTopic("famportal.category.name");
                    Topic orderTopic = childs.getTopic("famportal.category.order");
                    workspaceService.assignToWorkspace(nameTopic, famportalWorkspace.getId());
                    workspaceService.assignToWorkspace(orderTopic, famportalWorkspace.getId());
                    log.info("Assigned famportal name & order childs " + categoryTopic.getSimpleValue() + " to confidential " +
                            "workspace \"Familienportal\"");
                }
            }
        }
        // ### Fix association (famportal.category to geo-objects) workspace assignments, too.

    }
    
}
