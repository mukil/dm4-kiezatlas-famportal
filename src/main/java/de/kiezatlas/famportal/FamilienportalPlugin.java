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
import de.kiezatlas.GeoObjects;
import de.kiezatlas.GroupedGeoObjects;
import de.kiezatlas.comments.CommentsService;
import de.kiezatlas.website.WebsiteService;
import de.kiezatlas.website.model.CommentModel;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private static final String KA1_GEO_OBJECT_URI_PREFIX = "de.kiezatlas.topic.";

    // private static final String KA1_MAP_URL = "http://www.kiezatlas.de/map/%s/p/%s";
    private static final String KA2_OBJ_URL = "http://api.kiezatlas.de/website/geo/%s";

    // ---------------------------------------------------------------------------------------------- Instance Variables

    @Inject private KiezatlasService kiezatlas;
    @Inject private WorkspacesService workspaces; // Used in Migrations
    @Inject private AccessControlService acService;
    @Inject private GeomapsService geomaps;
    @Inject private FacetsService facets;
    @Inject private WebsiteService website;
    @Inject private CommentsService comments;

    Topic famportalWorkspace = null;
    HashMap<String, Long> districtUriMap = null;

    private Logger logger = Logger.getLogger(getClass().getName());

    // -------------------------------------------------------------------------------------------------- Public Methods



    // ****************************************
    // *** Famportal Service Implementation ***
    // ****************************************



    @Override
    public void init() {
        // Populating a map of District URIs : Topic IDs
        districtUriMap = new HashMap<String, Long>();
        List<Topic> topics = dm4.getTopicsByType("ka2.bezirk");
        for (Topic district : topics) {
            districtUriMap.put(district.getUri(), district.getId());
        }
        logger.info("Initialized map with " + districtUriMap.size()
                + " Kiezatlas District Topics for Famportal API Queries");
    }

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
            List<Topic> resultList = null;
            Iterator<CategorySet> iteratorSets = categorySets.iterator();
            while (iteratorSets.hasNext()) {
                CategorySet categorySet = iteratorSets.next();
                // simply adding up all elements related to all categories (contains duplicates)
                List<RelatedTopic> categoryList = uniteAllGeoObjectTopics(categorySet);
                // if there is something to intersect, do so
                resultList = getIntersection(resultList, categoryList);
            }
            List<RelatedTopic> uniqueList = null;
            if (resultList != null) { // is null if NO or WRONG categoryId was requested
                // make resultlist only contain unique topics
                uniqueList = new ArrayList(new HashSet(resultList));
            }
            // apply proximity filter to our resultset and turn them into geo-objects
            return applyProximityFilter(uniqueList, proximityFilter);
        } catch (RuntimeException e) {
            throw new RuntimeException("Fetching geo objects failed (categorySets=" + categorySets + ")", e);
        }
    }

    @GET
    @Path("/search")
    @Override
    public List<GeoObject> searchGeoObjects(@QueryParam("query") String query,
                                            @QueryParam("category") List<CategorySet> categorySets,
                                            @QueryParam("district") String districtUri) {
        isAuthorized();
        List<GeoObject> results = new ArrayList<GeoObject>();
        long districtId = 0;
        if (districtUri != null && districtUriMap.containsKey(districtUri)) districtId = districtUriMap.get(districtUri);
        // 1) Search in all Geo Objects with given fulltext query / with a simple appendingWildcard search
        List<Topic> geoObjects = website.searchFulltextInGeoObjectChilds(query, false, true, false, false);
        // 2) Filter Fulltext Search WITHOUT any category but possibley WITH a district parameter
        if (categorySets.isEmpty()) {
            logger.info("Building response for fulltext query on " + geoObjects.size() + " geo objects WITHOUT "
                    + "category filter, district=" + districtId);
            results = filterFamportalGeoObjects(geoObjects, districtId);
        // 3) Filter Fulltext Search Results WITH category and/or district parameters
        } else {
            logger.info("Building response for fulltext query on " + geoObjects.size() + " geo objects WITH "
                    + "category filter, district=" + districtId);
            List<Topic> categoryResults = null;
            Iterator<CategorySet> iteratorSets = categorySets.iterator();
            while (iteratorSets.hasNext()) {
                CategorySet categorySet = iteratorSets.next();
                // 3.1) Filter out geo objects not related to any of the categories in the categorySet
                List<Topic> categoryList = filterGeoObjectTopics(geoObjects, categorySet, districtId);
                // 3.2) if there is something to intersect, do so
                categoryResults = getIntersection(categoryResults, categoryList);
            }
            // 3.3) turn categoryResults into a result list of geo objects
            results = createGeoObjectResultList(categoryResults);
        }
        logger.info("Orte Search API delivers " + results.size() + " Kiezatlas Orte");
        return results;
    }

    @GET
    @Path("/category/objects")
    public GroupedGeoObjects searchCategories(@QueryParam("search") String query, @QueryParam("clock") long clock) {
        isAuthorized();
        return kiezatlas.searchCategories(query, clock);
    }

    @GET
    @Path("/search/name")
    public GeoObjects searchGeoObjects(@QueryParam("search") String query, @QueryParam("clock") long clock) {
        isAuthorized();
        return kiezatlas.searchGeoObjectNames(query, clock);
    }

    @Path("/geoobject/detail")
    @Override
    public List<GeoObjectDetail> getGeoObjectDetails(@QueryParam("ids") String topicIds) {
        isAuthorized();
        List<Topic> geoObjectTopics = new ArrayList<Topic>();
        List<String> topics = Arrays.asList(topicIds.split(","));
        try {
            if (topics.isEmpty()) {
                throw new RuntimeException("Missing the \"category\" parameter in request");
            }
            if (topics.size() > 15) {
                throw new RuntimeException("Too much information requested - Limit is set to 15 objects max.");
            }
            Iterator<String> iteratorSets = topics.iterator();
            while (iteratorSets.hasNext()) {
                String nextId = iteratorSets.next();
                Topic geoObject = website.getGeoObjectById(nextId);
                if (geoObject != null) {
                    logger.info("Details API \"" + geoObject.getSimpleValue().toString() + "\" (id=\"" + nextId + "\")");
                    geoObjectTopics.add(geoObject);
                } else {
                    logger.warning("Details API Could not fetch a geo object widh id=\"" + nextId + "\"");
                }
            }
        } catch(RuntimeException e) {
            throw new RuntimeException("Fetching geo objects failed (topics=" + topics + ")", e);
        }
        logger.info("Details API delivers " + geoObjectTopics.size() + " Kiezatlas Orte");
        return createGeoObjectDetailResultList(geoObjectTopics);
    }

    @POST
    @Path("/comment/{geoObjectId}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createGeoObjectComment(@PathParam("geoObjectId") String geoObjectId, CommentModel comment) {
        // Note: isAuthorized by comments module
        Topic geoObject = website.getGeoObjectById(geoObjectId);
        if (geoObject == null) {
            return Response.status(404).build();
        } else if (comment.getMessage().isEmpty()) {
            return Response.status(400).build();
        } else if (geoObject.getTypeUri().equals(KiezatlasService.GEO_OBJECT)) {
            logger.info("Comment: Received message from \""+comment.getContact() +"\" on topic \"" + geoObject.getSimpleValue() + "\"");
            Topic topic = comments.createComment(geoObject.getId(), comment.getMessage(), comment.getContact());
            if (topic != null) {
                return Response.noContent().build();
            } else {
                return Response.status(401).build();
            }
        } else {
            logger.severe("Prevented a comment targeted to a non geo topic by user \"" + acService.getUsername() + "\"");
            return Response.status(401).build();
        }
    }

    @GET
    @Path("/user")
    public String getFamportalWorkspaceMember() {
        isAuthorized();
        return acService.getUsername();
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
        logger.info("Create Famportal (Category ID"+famportalCategoryId+") Assignments by "
            + "GeoObject IDs " + geoObjectIds.toString());
        updateFacet(geoObjectIds, createFacetValue(famportalCategoryId));
        addToFamportalWebsite(geoObjectIds);
    }

    @PUT
    @Path("/category/{id}/ka_cat")
    @Override
    @Transactional
    public void createAssignmentsByCategories(@PathParam("id") long famportalCategoryId,
                                              @QueryParam("ka_cat") List<Long> kiezatlasCategoryIds) {
        isAuthorized();
        logger.info("Create Famportal (Category ID"+famportalCategoryId+") Assignments by "
            + "Category IDs " + kiezatlasCategoryIds.toString());
        updateFacetByCategories(kiezatlasCategoryIds, createFacetValue(famportalCategoryId));
        addCategoryToFamportalWebsite(kiezatlasCategoryIds);
    }

    // ---

    @DELETE
    @Path("/category/{id}")
    @Override
    @Transactional
    public void deleteAssignments(@PathParam("id") long famportalCategoryId,
                                  @QueryParam("geo_object") List<Long> geoObjectIds) {
        isAuthorized();
        logger.info("Delete Famportal (Category ID"+famportalCategoryId+") Assignments by "
            + "GeoObject IDs " + geoObjectIds.toString());
        updateFacet(geoObjectIds, createDeletionFacetValue(famportalCategoryId));
        removeFromFamportalWebsite(geoObjectIds);
    }

    @DELETE
    @Path("/category/{id}/ka_cat")
    @Override
    @Transactional
    public void deleteAssignmentsByCategories(@PathParam("id") long famportalCategoryId,
                                              @QueryParam("ka_cat") List<Long> kiezatlasCategoryIds) {
        isAuthorized();
        logger.info("Delete Famportal (Category ID"+famportalCategoryId+") Assignments by "
            + "Category IDs " + kiezatlasCategoryIds.toString());
        updateFacetByCategories(kiezatlasCategoryIds, createDeletionFacetValue(famportalCategoryId));
        removeCategoryFromFamportalWebsite(kiezatlasCategoryIds);
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
            count.addCount(famCatId, kiezatlas.getGeoObjectsByCategory(famCatId).size());
        }
        return count;
    }


    @Override
    public boolean isRelatedToFamportalCategory(Topic geoObject) {
        List<RelatedTopic> facetTopics = facets.getFacets(geoObject, FAMPORTAL_CATEGORY_FACET_URI);
        return (facetTopics.size() >= 1);
    }

    @Override
    public boolean isParentAggregatingTopic(Topic geoObject, long districtOrCategoryId) {
        return (dm4.getAssociation("dm4.core.aggregation", geoObject.getId(), districtOrCategoryId,
            "dm4.core.parent", "dm4.core.child") != null);
    }

    // ------------------------------------------------------------------------------------------------- Private Methods

    private Topic getFamportalWebsite() {
        return dm4.getTopicByUri(FAMPORTAL_WEBSITE_URI);
    }

    /**
     * First checks for a valid session and then it checks fo for a "Membership" association between the
     * requesting username and the Famportal workspace.
     **/
    private void isAuthorized() throws WebApplicationException {
        String username = acService.getUsername();
        if (username == null) throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        if (famportalWorkspace == null) getFamportalWorkspaceId();
        if (!acService.isMember(username, famportalWorkspace.getId())) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    /**
     * Returns a list containing elements which are contained in both lists.
     */
    private List<Topic> getIntersection(List listA, List listB) {
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
    private List<RelatedTopic> uniteAllGeoObjectTopics(CategorySet categorySet) {
        List<RelatedTopic> relatedTopics = null;
        for (String categoryXmlId : categorySet) {
            try {
                long catId = categoryTopic(categoryXmlId).getId();
                List<RelatedTopic> intermediaryList = fetchGeoObjectTopicsInFamportalCategory(catId);
                relatedTopics = getUnion(relatedTopics, intermediaryList);
                logger.info("> Fetched all " + intermediaryList.size() + " Geo Objects in \"" +categoryXmlId+ "\", Union="
                        + relatedTopics.size() + " Categories=" + categorySet.size() + "");
            } catch (RuntimeException rex) {
                logger.warning("Cushioned Famportal query involving an unknown category " + rex.getMessage());
            }
        }
        return relatedTopics;
    }

    /**
     * Returns the union of all related topics for a complete category set.
     */
    private List<Topic> filterGeoObjectTopics(List<Topic> topics, CategorySet categorySet, long districtId) {
        List<Topic> matchingTopics = new ArrayList<Topic>();
        Iterator<Topic> iteratorTopics = topics.iterator();
        while (iteratorTopics.hasNext()) {
            Topic geoObject = iteratorTopics.next();
            for (String categoryXmlId : categorySet) {
                try {
                    long catId = categoryTopic(categoryXmlId).getId();
                    if (isParentAggregatingTopic(geoObject, catId)) {
                        addWithDistrictParameter(matchingTopics, geoObject, districtId);
                    }
                } catch (RuntimeException rex) {
                    logger.warning("Cushioned Famportal query involving an unknown category " + rex.getMessage());
                }
            }
        }
        return matchingTopics;
    }

    private void addWithDistrictParameter(List<Topic> results, Topic geoObject, long districtId) {
         if (districtId == 0) {
            results.add(geoObject);
        } else if (districtId > 0 && isParentAggregatingTopic(geoObject, districtId)) {
            results.add(geoObject);
        }
    }

    private void addGeoObjectWithDistrictParameter(List<GeoObject> results, Topic geoObject, long districtId,
                                                   Topic address, GeoCoordinate coordinates) {
        if (districtId == 0) {
            results.add(createTransferGeoObject(geoObject, coordinates, address));
        } else if (districtId > 0 && isParentAggregatingTopic(geoObject, districtId)) {
            results.add(createTransferGeoObject(geoObject, coordinates, address));
        }
    }

    /**
     * Returns the list of RelatedTopics to the given Familienportal Category topic id.
     */
    private List<RelatedTopic> fetchGeoObjectTopicsInFamportalCategory(long catId) {
        return kiezatlas.getGeoObjectsByCategory(catId);
    }

    /**
     * Filters the given list by proximity filter if such is no null and thus excludes topics related
     * through a higher distance value (or all elements with invalid geo-coordinates).
     */
    private List<GeoObject> applyProximityFilter(List<RelatedTopic> geoObjects, ProximityFilter proximityFilter) {
        List<GeoObject> results = new ArrayList<GeoObject>();
        if (geoObjects == null) return results;
        logger.info("Applying proximityfilter to " + geoObjects.size() + " geo-objects");
        for (Topic geoObjectTopic : geoObjects) {
            try {
                Topic address = getAnschrift(geoObjectTopic);
                GeoCoordinate geoCoord = geoCoordinate(address);
                if (proximityFilter != null && geoCoord != null) {
                    double distance = geomaps.getDistance(geoCoord, proximityFilter.geoCoordinate);
                    if (distance > proximityFilter.radius) {
                        continue;
                    }
                }
                GeoObject result = createTransferGeoObject(geoObjectTopic, geoCoord, address);
                if (result != null) results.add(result);
            } catch (Exception e) {
                logger.warning("### Excluding geo object " + geoObjectTopic.getId() + " (\"" +
                    geoObjectTopic.getSimpleValue() + "\") from result (" + e + ")");
            }
        }
        return results;
    }

    /**
     * Filters the given list about all elements with invalid geo-coordinates.
     */
    private List<GeoObject> createGeoObjectResultList(List<Topic> geoObjects) {
        List<GeoObject> results = new ArrayList<GeoObject>();
        for (Topic geoObjectTopic : geoObjects) {
            try {
                Topic address = getAnschrift(geoObjectTopic);
                GeoCoordinate geoCoord = geoCoordinate(address);
                GeoObject result = createTransferGeoObject(geoObjectTopic, geoCoord, address);
                if (result != null) results.add(result);
            } catch (Exception e) {
                logger.warning("### Excluding geo object " + geoObjectTopic.getId() + " (\"" +
                    geoObjectTopic.getSimpleValue() + "\") from result (" + e + ")");
            }
        }
        return results;
    }

    /**
     * Filters the given list about all elements with invalid geo-coordinates.
     */
    private List<GeoObjectDetail> createGeoObjectDetailResultList(List<Topic> geoObjects) {
        List<GeoObjectDetail> results = new ArrayList<GeoObjectDetail>();
        for (Topic geoObjectTopic : geoObjects) {
            try {
                Topic address = getAnschrift(geoObjectTopic);
                GeoCoordinate geoCoord = geoCoordinate(address);
                GeoObjectDetail result = createTransferGeoObjectDetail(geoObjectTopic, geoCoord, address);
                if (result != null) results.add(result);
            } catch (Exception e) {
                logger.warning("### Excluding geo object " + geoObjectTopic.getId() + " (\"" +
                    geoObjectTopic.getSimpleValue() + "\") from result (" + e + ")");
            }
        }
        return results;
    }

    private List<GeoObject> filterFamportalGeoObjects(List<Topic> geoObjects, long districtId) {
        List<GeoObject> results = new ArrayList<GeoObject>();
        for (Topic geoObject : geoObjects) {
            if (isRelatedToFamportalCategory(geoObject)) {
                Topic address = getAnschrift(geoObject);
                GeoCoordinate coordinates = geoCoordinate(address);
                if (coordinates != null) {
                    addGeoObjectWithDistrictParameter(results, geoObject, districtId, address, coordinates);
                } else {
                    logger.warning("Skipping valid fulltext search response - MISSES COORDINATES");
                }
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

    private Topic getAnschrift(Topic geoObject) {
        return geoObject.getChildTopics().getTopicOrNull("dm4.contacts.address");
    }

    private String getId(Topic geoObject) {
        String value = null;
        if (geoObject.getUri().contains(KA1_GEO_OBJECT_URI_PREFIX)) {
            value = geoObject.getUri().substring(KA1_GEO_OBJECT_URI_PREFIX.length());
        } else {
            value = "" + geoObject.getId();
        }
        return value;
    }

    private GeoCoordinate geoCoordinate(Topic address) {
        GeoCoordinate geoCoord = null;
        if (address != null) {
            geoCoord = geomaps.getGeoCoordinate(address);
            if (geoCoord == null) {
                // throw new RuntimeException("Geo coordinate is unknown");
                logger.fine("No Geo Coordinate assigned to " + address.getSimpleValue() + " address=" + address.getSimpleValue());
            }
            logger.fine("No Address assigned to " + address.getSimpleValue());
        }
        return geoCoord;
    }

    // --- Create result GeoObject ---

    /**
     * @param   geoCoord    the geo coordinate already looked up.
     */
    private GeoObject createTransferGeoObject(Topic geoObjectTopic, GeoCoordinate geoCoord, Topic address) {
        // ...
        if (geoCoord == null) return null; // deal breaker
        // ..
        GeoObject geoObject = new GeoObject();
        geoObject.setName(geoObjectTopic.getSimpleValue().toString());
        Topic bezirk = bezirk(geoObjectTopic);
        if (bezirk != null) {
            geoObject.setBezirk(bezirk.getSimpleValue().toString());
        }
        if (address != null) {
            geoObject.setAnschrift(address.getSimpleValue().toString());
        }
        geoObject.setGeoCoordinate(geoCoord);
        geoObject.setLink(link(geoObjectTopic, bezirk));
        geoObject.setId(getId(geoObjectTopic));
        return geoObject;
    }

    /**
     * @param   geoCoord    the geo coordinate already looked up.
     */
    private GeoObjectDetail createTransferGeoObjectDetail(Topic geoObjectTopic, GeoCoordinate geoCoord, Topic address) {
        // ...
        if (geoCoord == null) return null; // deal breaker
        // ..
        GeoObjectDetail geoObject = new GeoObjectDetail();
        geoObject.setName(geoObjectTopic.getSimpleValue().toString());
        Topic bezirk = bezirk(geoObjectTopic);
        if (bezirk != null) {
            geoObject.setBezirk(bezirk.getSimpleValue().toString());
        }
        if (address != null) {
            String street = address.getChildTopics().getStringOrNull("dm4.contacts.street");
            if (street != null) geoObject.setStrasseHnr(street);
            String plz = address.getChildTopics().getStringOrNull("dm4.contacts.postal_code");
            if (plz != null) geoObject.setPostleitzahl(plz);
            String stadt = address.getChildTopics().getStringOrNull("dm4.contacts.city");
            if (stadt != null) geoObject.setStadt(stadt);
        }
        geoObject.setGeoCoordinate(geoCoord);
        geoObject.setLink(link(geoObjectTopic, bezirk));
        geoObject.setId(getId(geoObjectTopic));
        return geoObject;
    }

    /**
     * Returns the Kontakt topic assigned to the given Geo Object.
     * If no Bezirk topic is assigned an exception is thrown.
     */
    private String anschrift(Topic geoObjectTopic) {
        Topic anschrift = geoObjectTopic.getChildTopics().getTopic("dm4.contacts.address");
        if (anschrift == null) {
            logger.warning("Geo Object \"" + geoObjectTopic.getSimpleValue() + "\" has no Anschrift set");
            return anschrift.getSimpleValue().toString(); // .replaceAll(" Deutschland", "");
        }
        return null;
    }

    /**
     * Returns the Bezirk topic assigned to the given Geo Object.
     * If no Bezirk topic is assigned an exception is thrown.
     */
    private Topic bezirk(Topic geoObjectTopic) {
        Topic bezirk = facets.getFacet(geoObjectTopic, "ka2.bezirk.facet");
        if (bezirk == null) {
            // ### throw new RuntimeException("No Bezirk is assigned"); // May happen with fulltext search
            logger.warning("Skipping Result \"" + geoObjectTopic.getSimpleValue() + "\" MISSES BEZIRK");
        }
        return bezirk;
    }

    /**
     * Generates a valid link to a detail resource for a Kiezatlas1 and Kiezatlas2 Geo Object.
     * @param   bezirk      the Bezirk topic already looked up. Not null.
     */
    private String link(Topic geoObjectTopic, Topic bezirk) {
        String geoObjectId = "";
        try { // handle as if it were a ka1 object
            geoObjectId = uriPostfix(geoObjectTopic.getUri(), KA1_GEO_OBJECT_URI_PREFIX, "geo object");
        } catch (Exception e) { // is ka2 object
            geoObjectId = "" + geoObjectTopic.getId();
        }
        // String mapAlias = ka1MapAlias(geoObjectTopic, bezirk);
        return String.format(KA2_OBJ_URL, geoObjectId);
    }

    private String ka1MapAlias(Topic geoObjectTopic, Topic bezirk) {
        String ka1MapAlias;
        Topic bezirksregion = facets.getFacet(geoObjectTopic, "ka2.bezirksregion.facet");
        if (bezirksregion != null) {
            ka1MapAlias = uriPostfix(bezirksregion.getUri(), KA2_BEZIRKSREGION_URI_PREFIX, "Bezirksregion");
        } else if (bezirk != null) {
            // Fallback: link to Bezirksgesamtkarte when Bezirksregion is unknown
            ka1MapAlias = uriPostfix(bezirk.getUri(), KA2_BEZIRK_URI_PREFIX, "Bezirk");
        } else {
            ka1MapAlias = ""; // ### each geo object must have a district assignment
        }
        return ka1MapAlias;
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

    private void addToFamportalWebsite(List<Long> geoObjectIds) {
        Topic famportalWebsite = getFamportalWebsite();
        for (long geoObjectId : geoObjectIds) {
            Topic geoObject = dm4.getTopic(geoObjectId);
            kiezatlas.addGeoObjectToWebsite(geoObject, famportalWebsite);
        }
    }

    private void removeFromFamportalWebsite(List<Long> geoObjectIds) {
        Topic famportalWebsite = getFamportalWebsite();
        for (long geoObjectId : geoObjectIds) {
            Topic geoObject = dm4.getTopic(geoObjectId);
            if (!isRelatedToFamportalCategory(geoObject)) { // checks if there is still another relation
                kiezatlas.removeGeoObjectFromWebsite(geoObject, famportalWebsite);
            } else {
                // geo object can be in many famportal categories
                logger.info("SKIPPED removing famportal website assignment for Geo Object \"" + geoObject.getSimpleValue() + "\"");
            }
        }
    }

    private void addCategoryToFamportalWebsite(List<Long> kiezatlasCategoryIds) {
        Topic famportalWebsite = getFamportalWebsite();
        for (long catId : kiezatlasCategoryIds) {
            List<RelatedTopic> geoObjects = kiezatlas.getGeoObjectsByCategory(catId);
            for (Topic geoObject : geoObjects) {
                kiezatlas.addGeoObjectToWebsite(geoObject, famportalWebsite);
            }
        }
    }

    private void removeCategoryFromFamportalWebsite(List<Long> kiezatlasCategoryIds) {
        Topic famportalWebsite = getFamportalWebsite();
        for (long catId : kiezatlasCategoryIds) {
            List<RelatedTopic> geoObjects = kiezatlas.getGeoObjectsByCategory(catId);
            for (Topic geoObject : geoObjects) {
                if (!isRelatedToFamportalCategory(geoObject)) { // checks if there is still another relation
                    kiezatlas.removeGeoObjectFromWebsite(geoObject, famportalWebsite);
                } else {
                    // geo object can be in many famportal categories
                    logger.info("SKIPPED removing famportal website assignment for Geo Object " + geoObject.getSimpleValue());
                }
            }
        }
    }

    private void updateFacet(List<Long> geoObjectIds, FacetValueModel value) {
        for (long geoObjectId : geoObjectIds) {
            facets.updateFacet(geoObjectId, FAMPORTAL_CATEGORY_FACET_URI, value);
        }
    }

    private void updateFacetByCategories(List<Long> kiezatlasCategoryIds, FacetValueModel value) {
        for (long catId : kiezatlasCategoryIds) {
            List<RelatedTopic> geoObjects = kiezatlas.getGeoObjectsByCategory(catId);
            for (Topic geoObject : geoObjects) {
                facets.updateFacet(geoObject, FAMPORTAL_CATEGORY_FACET_URI, value);
            }
        }
    }

}
