package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.Topic;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.facets.FacetsService;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;



public class Migration9 extends Migration {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private int noBezirksAssignment = 0, noAddressAssignment = 0;
    private Logger log = Logger.getLogger(getClass().getName());
    @Inject private FacetsService facetsService;

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {
        log.info("### Migration Nr.9 STARTING Report: Geo Object with NO BEZIRK or NO ADDRESS assignment");
        List<Topic> geoObjects = dm4.getTopicsByType("ka2.geo_object");
        Iterator<Topic> i = geoObjects.iterator();
        while (i.hasNext()) {
            Topic geoObject = i.next();
            bezirk(geoObject);
            address(geoObject);
        }
        log.info("### Migration Nr.9 Reporting COMPLETE: Geo Objects with NO BEZIRK ("+ noBezirksAssignment + ") "
            + "or NO ADDRESS ("+noAddressAssignment+")");
    }

    private void bezirk(Topic geoObjectTopic) {
        Topic bezirk = facetsService.getFacet(geoObjectTopic, "ka2.bezirk.facet");
        if (bezirk == null) {
            log.warning("Geo Object \"" + geoObjectTopic + "\" MISSES a BEZIRKs FACET");
            noBezirksAssignment++;
        }
    }

    private void address(Topic geoObjectTopic) {
        Topic address = geoObjectTopic.getChildTopics().getTopicOrNull("dm4.contacts.address");
        if (address == null) {
            log.warning("Geo Object \"" + geoObjectTopic + "\" MISSES an ADDRESS TOPIC");
            noAddressAssignment++;
        }
    }

}
