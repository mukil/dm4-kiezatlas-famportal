package de.kiezatlas.famportal.migrations;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.service.Migration;
import de.deepamehta.core.service.Inject;
import de.deepamehta.facets.FacetsService;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;



public class Migration9 extends Migration {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private Logger log = Logger.getLogger(getClass().getName());
    @Inject private FacetsService facetsService;

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public void run() {
        log.info("### Migration9 STARTING: Generating a Report on Geo Object with NO BEZIRK assignment");
        List<Topic> geoObjects = dm4.getTopicsByType("ka2.geo_object");
        Iterator<Topic> i = geoObjects.iterator();
        while (i.hasNext()) {
            Topic geoObject = i.next();
            bezirk(geoObject);
        }
        log.info("### Migration9 COMPLETE: Generating a Report on Geo Object with NO BEZIRK assignemnt");

    }

    private Topic bezirk(Topic geoObjectTopic) {
        Topic bezirk = facetsService.getFacet(geoObjectTopic, "ka2.bezirk.facet");
        if (bezirk == null) {
            log.warning("Reporting that Geo Object \"" + geoObjectTopic + "\" MISSES a BEZIRK");
        }
        return bezirk;
    }

}
