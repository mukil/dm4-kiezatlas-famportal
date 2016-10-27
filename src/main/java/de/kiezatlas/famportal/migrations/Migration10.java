package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.Association;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Migration;
import de.deepamehta.workspaces.WorkspacesService;
import de.kiezatlas.KiezatlasService;
import de.kiezatlas.famportal.FamilienportalService;
import static de.kiezatlas.famportal.migrations.Migration8.FAMPORTAL_WORKSPACE_URI;
import java.util.List;
import java.util.logging.Logger;



public class Migration10 extends Migration {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger logger = Logger.getLogger(getClass().getName());

    @Inject private KiezatlasService kiezService;
    @Inject private FamilienportalService famportalService;
    @Inject private WorkspacesService workspacesService;

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {
        logger.info("### Migration Nr.10 Create Familienportal Site Topic and assign all famportal Geo Objects to it");
        // 1) Create new Familienportal Site Topic
        Topic kiezatlasWorkspace = workspacesService.getWorkspace(KiezatlasService.KIEZATLAS_WORKSPACE_URI);
        Topic newKiezatlasSite = kiezService.createKiezatlasWebsite("Familienportal Stadtplan", FamilienportalService.FAMPORTAL_WEBSITE_URI);
        logger.info("Creating new Website \"" + newKiezatlasSite.getSimpleValue() + "\", assigned to \"Kiezatlas\" workspace");
        workspacesService.assignToWorkspace(newKiezatlasSite, kiezatlasWorkspace.getId());
        // 2) Giving all existing geo objects with a familienportal category relation a famportal site assignment
        Topic famportalWorkspace = workspacesService.getWorkspace(FAMPORTAL_WORKSPACE_URI);
        List<Topic> geoObjects = dm4.getTopicsByType("ka2.geo_object");
        for (Topic geoObject : geoObjects) {
            if (famportalService.isRelatedToFamportalCategory(geoObject)) {
                Association assignment = kiezService.addGeoObjectToWebsite(geoObject, newKiezatlasSite);
                logger.info("Assigning Geo Object " + geoObject.getSimpleValue() + " related to Famportal Category "
                    + " to Website \"" + newKiezatlasSite.getSimpleValue() + "\"");
                workspacesService.assignToWorkspace(assignment, famportalWorkspace.getId());
            }
        }
        logger.info("### Migration Nr.10 Famportal Site Creation COMPLETE");
    }

}
