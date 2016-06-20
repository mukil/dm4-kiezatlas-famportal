package de.kiezatlas.famportal;

import de.kiezatlas.KiezatlasService;

import de.deepamehta.accesscontrol.AccessControlService;
import de.deepamehta.workspaces.WorkspacesService;
import de.deepamehta.facets.FacetsService;
import de.deepamehta.geomaps.GeomapsService;
import de.deepamehta.geomaps.model.GeoCoordinate;

import de.deepamehta.core.RelatedTopic;
import de.deepamehta.core.Topic;
import de.deepamehta.core.model.facets.FacetValueModel;
import de.deepamehta.core.osgi.PluginActivator;
import de.deepamehta.core.service.Inject;
import de.deepamehta.core.service.Transactional;
import de.kiezatlas.website.WebsiteService;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.ListUtils;



@Path("/famportal")
@Consumes("application/json")
@Produces("application/json")
public class FamilienportalPlugin extends PluginActivator implements FamilienportalService {

    // ------------------------------------------------------------------------------------------------------- Constants

    private static final String FAMPORTAL_CATEGORY_URI        = "famportal.category";
    private static final String FAMPORTAL_CATEGORY_FACET_URI  = "famportal.category.facet";

    // The URIs of Familienportal Kategorie topics have this prefix.
    // The remaining part of the URI is the original Familienportal category XML ID.
    private static final String FAMPORTAL_CATEGORY_URI_PREFIX = "famportal.";

    // The URIs of KA2 Bezirksregion topics have this prefix.
    // The remaining part of the URI is the original KA1 map alias.
    private static final String KA2_BEZIRKSREGION_URI_PREFIX = "ka2.bezirksregion.";

    // The URIs of KA2 Bezirk topics have this prefix.
    // The remaining part of the URI is the original KA1 overall map alias.
    private static final String KA2_BEZIRK_URI_PREFIX = "ka2.bezirk.";

    // The URIs of KA2 Geo Object topics have this prefix.
    // The remaining part of the URI is the original KA1 topic id.
    private static final String KA2_GEO_OBJECT_URI_PREFIX = "de.kiezatlas.topic.";

    private static final String KA1_MAP_URL = "http://www.kiezatlas.de/map/%s/p/%s";

    // ---------------------------------------------------------------------------------------------- Instance Variables

    @Inject private KiezatlasService kiezatlasService;
    @Inject private WorkspacesService workspacesService;
    @Inject private AccessControlService accessControlService;
    @Inject private GeomapsService geomapsService;
    @Inject private FacetsService facetsService;
    @Inject private WebsiteService websiteService;

    Topic famportalWorkspace = null;

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods



    // ************************************
    // *** FacetsService Implementation ***
    // ************************************



    // --- Retrieval API ---

    @GET
    @Path("/geoobject")
    @Override
    public List<GeoObject> getGeoObjects(@QueryParam("category") List<CategorySet> categorySets,
                                         @QueryParam("proximity") ProximityFilter proximityFilter) {
        isAuthorized();
        try {
            if (categorySets.isEmpty()) {
                throw new RuntimeException("Missing the \"category\" parameter in request");
            }
            List<RelatedTopic> resultList = null;
            Iterator<CategorySet> iteratorSets = categorySets.iterator();
            while (iteratorSets.hasNext()) {
                CategorySet categorySet = iteratorSets.next();
                // simply adding up all elements related to all categories (contains duplicates)
                List<RelatedTopic> categoryList = uniteAllGeoObjects(categorySet);
                // if there is something to intersect, do so
                resultList = getIntersection(resultList, categoryList);
            }
            // make resultlist only contain unique topics
            List<RelatedTopic> uniqueList = new ArrayList(new HashSet(resultList));
            // apply proximity filter to our resultset and turn them into geo-objects
            return applyProximityFilter(uniqueList, proximityFilter);
        } catch (Exception e) {
            throw new RuntimeException("Fetching geo objects failed (categorySets=" + categorySets + ")", e);
        }
    }

    @GET
    @Path("/search")
    public List<GeoObject> searchGeoObjects(@QueryParam("query") String query) {
        isAuthorized();
        List<GeoObject> results = new ArrayList<GeoObject>();
        List<Topic> geoObjects = websiteService.searchFulltextInGeoObjectChilds(query);
        logger.info("Start building response for " + geoObjects.size() + " and FILTER by FAMPORTAL CATEGORY");
        for (Topic geoObject: geoObjects) {
            if (hasRelatedFamportalCategory(geoObject)) {
                GeoCoordinate coordinates = geoCoordinate(geoObject);
                if (coordinates != null) {
                    results.add(createGeoObject(geoObject, coordinates));
                } else {
                    logger.warning("Skipping valid fulltext search response - MISSES COORDINATES");
                }
            }
        }
        logger.info("Build up response " + results.size() + " geo objects in famportal categories");
        return results;
    }

    private boolean hasRelatedFamportalCategory(Topic geoObject) {
        List<RelatedTopic> facetTopics = facetsService.getFacets(geoObject, FAMPORTAL_CATEGORY_FACET_URI);
        return (facetTopics.size() >= 1);
    }



    // --- Redaktionswerkzeug ---

    @GET
    @Path("/workspace")
    public long getFamportalWorkspaceId() {
        if (famportalWorkspace != null) return famportalWorkspace.getId();
        famportalWorkspace = dm4.getTopicByUri("de.kiezatlas.familienportal_ws");
        return famportalWorkspace.getId();
    }

    @PUT
    @Path("/category/{id}")
    @Override
    @Transactional
    public void createAssignments(@PathParam("id") long famportalCategoryId,
                                  @QueryParam("geo_object") List<Long> geoObjectIds) {
        isAuthorized();
        updateFacet(geoObjectIds, createFacetValue(famportalCategoryId));
    }

    @PUT
    @Path("/category/{id}/ka_cat")
    @Override
    @Transactional
    public void createAssignmentsByCategories(@PathParam("id") long famportalCategoryId,
                                              @QueryParam("ka_cat") List<Long> kiezatlasCategoryIds) {
        isAuthorized();
        updateFacetByCategories(kiezatlasCategoryIds, createFacetValue(famportalCategoryId));
    }

    // ---

    @DELETE
    @Path("/category/{id}")
    @Override
    @Transactional
    public void deleteAssignments(@PathParam("id") long famportalCategoryId,
                                  @QueryParam("geo_object") List<Long> geoObjectIds) {
        isAuthorized();
        updateFacet(geoObjectIds, createDeletionFacetValue(famportalCategoryId));
    }

    @DELETE
    @Path("/category/{id}/ka_cat")
    @Override
    @Transactional
    public void deleteAssignmentsByCategories(@PathParam("id") long famportalCategoryId,
                                              @QueryParam("ka_cat") List<Long> kiezatlasCategoryIds) {
        isAuthorized();
        updateFacetByCategories(kiezatlasCategoryIds, createDeletionFacetValue(famportalCategoryId));
    }

    // ---

    @GET
    @Path("/count")
    @Override
    public GeoObjectCount countAssignments() {
        GeoObjectCount count = new GeoObjectCount();
        isAuthorized();
        for (Topic famportalCategory : dm4.getTopicsByType(FAMPORTAL_CATEGORY_URI)) {
            long famCatId = famportalCategory.getId();
            count.addCount(famCatId, kiezatlasService.getGeoObjectsByCategory(famCatId).size());
        }
        return count;
    }



    // ------------------------------------------------------------------------------------------------- Private Methods

    /**
     * New as of DeepaMehta 4.7.
     * It Checks for a "Membership" association between the requesting username and the Famportal workspace. **/
    private void isAuthorized() throws WebApplicationException {
        if (famportalWorkspace == null) getFamportalWorkspaceId();
        if (!accessControlService.isMember(accessControlService.getUsername(), famportalWorkspace.getId())) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }
    /**
     * Returns a list containing elements which are contained in both lists.
     */
    private List<RelatedTopic> getIntersection(List listA, List listB) {
        if (listA == null) return listB;
        logger.info("> Intersecting " + listA.size() + " Geo Objects AND " + listB.size() + " Geo Objects");
        return ListUtils.intersection(listA, listB);
    }

    /**
     * Returns a list containing elements which are in any of the lists.
     */
    private List<RelatedTopic> getUnion(List listA, List listB) {
        if (listA == null) return listB;
        return ListUtils.union(listA, listB);
    }

    /**
     * Returns the union of all related topics for a complete category set.
     */
    private List<RelatedTopic> uniteAllGeoObjects(CategorySet categorySet) {
        List<RelatedTopic> relatedTopics = null;
        for (String categoryXmlId : categorySet) {
            try {
                long catId = categoryTopic(categoryXmlId).getId();
                List<RelatedTopic> intermediaryList = fetchGeoObjectTopicsInFamportalCategory(catId);
                relatedTopics = getUnion(relatedTopics, intermediaryList);
                logger.info("> Fetched all " + intermediaryList.size() + " Geo Objects in \"" +categoryXmlId+ "\", Union="
                        + relatedTopics.size() + " Categories=" + categorySet.size() + "");
            } catch (RuntimeException rex) {
                logger.warning("> Cushioned Famportal query involving an unknown category " + rex.getMessage());
            }
        }
        return relatedTopics;
    }

    /**
     * Returns the list of RelatedTopics to the given Familienportal Category topic id.
     */
    private List<RelatedTopic> fetchGeoObjectTopicsInFamportalCategory(long catId) {
        return kiezatlasService.getGeoObjectsByCategory(catId);
    }

    /**
     * Filters the given list by proximity filter if such is no null and thus excludes topics related
     * through a higher distance value (or all elements with invalid geo-coordinates).
     */
    private List<GeoObject> applyProximityFilter(List<RelatedTopic> geoObjects, ProximityFilter proximityFilter) {
        logger.info("Applying proximityfilter to a list of " + geoObjects.size() + " geo-objects");
        List<GeoObject> results = new ArrayList<GeoObject>();
        for (Topic geoObjectTopic : geoObjects) {
            try {
                GeoCoordinate geoCoord = geoCoordinate(geoObjectTopic);
                if (proximityFilter != null) {
                    double distance = geomapsService.getDistance(geoCoord, proximityFilter.geoCoordinate);
                    if (distance > proximityFilter.radius) {
                        continue;
                    }
                }
                results.add(createGeoObject(geoObjectTopic, geoCoord));
            } catch (Exception e) {
                logger.warning("### Excluding geo object " + geoObjectTopic.getId() + " (\"" +
                    geoObjectTopic.getSimpleValue() + "\") from result (" + e + ")");
            }
        }
        return results;
    }

    /**
     * Returns the Familienportal Kategorie topic that corresponds to the original Familienportal category XML ID.
     */
    private Topic categoryTopic(String famportalCategoryXmlId) {
        Topic cat = dm4.getTopicByUri(FAMPORTAL_CATEGORY_URI_PREFIX + famportalCategoryXmlId);
        if (cat == null) {
            throw new RuntimeException("\"" + famportalCategoryXmlId + "\" is an unknown Familienportal category " +
                "XML ID (no corresponding topic found)");
        }
        return cat;
    }

    private GeoCoordinate geoCoordinate(Topic geoObjectTopic) {
        Topic address = geoObjectTopic.getChildTopics().getTopic("dm4.contacts.address");
        GeoCoordinate geoCoord = geomapsService.getGeoCoordinate(address);
        if (geoCoord == null) {
            // throw new RuntimeException("Geo coordinate is unknown");
            logger.fine("No Geo Coordinate assigned to " + geoObjectTopic.getSimpleValue() + " address=" + address.getSimpleValue());
        }
        return geoCoord;
    }

    // --- Create result GeoObject ---

    /**
     * @param   geoCoord    the geo coordinate already looked up.
     */
    private GeoObject createGeoObject(Topic geoObjectTopic, GeoCoordinate geoCoord) {
        if (geoCoord == null) return null; // deal breaker
        GeoObject geoObject = new GeoObject();
        Topic bezirk = bezirk(geoObjectTopic);
        geoObject.setName(geoObjectTopic.getSimpleValue().toString());
        if (bezirk != null) geoObject.setBezirk(bezirk.getSimpleValue().toString()); // formerly deal breaker
        geoObject.setGeoCoordinate(geoCoord);
        geoObject.setLink(link(geoObjectTopic, bezirk)); // adapt to new responsive resource
        return geoObject;
    }

    /**
     * Returns the Bezirk topic assigned to the given Geo Object.
     * If no Bezirk topic is assigned an exception is thrown.
     */
    private Topic bezirk(Topic geoObjectTopic) {
        Topic bezirk = facetsService.getFacet(geoObjectTopic, "ka2.bezirk.facet");
        if (bezirk == null) {
            // ### throw new RuntimeException("No Bezirk is assigned"); // May happen with fulltext search
            logger.warning("Skipping Geo Object " + geoObjectTopic.getSimpleValue() + " MISSES BEZIRK");
        }
        return bezirk;
    }

    /**
     * @param   bezirk      the Bezirk topic already looked up. Not null.
     */
    private String link(Topic geoObjectTopic, Topic bezirk) {
        String ka1TopicId, ka1MapAlias;
        //
        ka1TopicId = uriPostfix(geoObjectTopic.getUri(), KA2_GEO_OBJECT_URI_PREFIX, "geo object");
        //
        Topic bezirksregion = facetsService.getFacet(geoObjectTopic, "ka2.bezirksregion.facet");
        if (bezirksregion != null) {
            ka1MapAlias = uriPostfix(bezirksregion.getUri(), KA2_BEZIRKSREGION_URI_PREFIX, "Bezirksregion");
        } else if (bezirk != null) {
            // Fallback: link to Bezirksgesamtkarte when Bezirksregion is unknown
            ka1MapAlias = uriPostfix(bezirk.getUri(), KA2_BEZIRK_URI_PREFIX, "Bezirk");
        } else {
            ka1MapAlias = ""; // ### each geo object must have a district assignment
        }
        return String.format(KA1_MAP_URL, ka1MapAlias, ka1TopicId);
    }

    private String uriPostfix(String uri, String uriPrefix, String entityName) {
        if (!uri.startsWith(uriPrefix)) {
            throw new RuntimeException("The " + entityName + " URI does not start with \"" + uriPrefix +
                "\" but is \"" + uri + "\"");
        }
        //
        return uri.substring(uriPrefix.length());
    }

    // --- Update Famportal Category facet ---

    private FacetValueModel createFacetValue(long famportalCategoryId) {
        // Prerequisite: categories are modeled per 1) Aggregation Def, 2) Cardinality Many
        return mf.newFacetValueModel(FAMPORTAL_CATEGORY_URI).addRef(famportalCategoryId);
    }

    private FacetValueModel createDeletionFacetValue(long famportalCategoryId) {
        // Prerequisite: categories are modeled per 1) Aggregation Def, 2) Cardinality Many
        return mf.newFacetValueModel(FAMPORTAL_CATEGORY_URI).addDeletionRef(famportalCategoryId);
    }

    // ---

    private void updateFacet(List<Long> geoObjectIds, FacetValueModel value) {
        for (long geoObjectId : geoObjectIds) {
            facetsService.updateFacet(geoObjectId, FAMPORTAL_CATEGORY_FACET_URI, value);
        }
    }

    private void updateFacetByCategories(List<Long> kiezatlasCategoryIds, FacetValueModel value) {
        for (long catId : kiezatlasCategoryIds) {
            List<RelatedTopic> geoObjects = kiezatlasService.getGeoObjectsByCategory(catId);
            for (Topic geoObject : geoObjects) {
                facetsService.updateFacet(geoObject, FAMPORTAL_CATEGORY_FACET_URI, value);
            }
        }
    }
}
