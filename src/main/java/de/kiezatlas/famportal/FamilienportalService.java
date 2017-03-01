package de.kiezatlas.famportal;

import de.deepamehta.core.Topic;

import java.util.List;



public interface FamilienportalService {

    public static final String FAMPORTAL_WEBSITE_URI = "de.kiezatlas.site_famportal";

    // --- Retrieval API ---

    List<GeoObjectDetail> getGeoObjects(String topicIds);

    List<GeoObject> getGeoObjects(List<CategorySet> categorySets, ProximityFilter proximity);

    List<GeoObject> searchGeoObjects(String query, List<CategorySet> categorySets, String districtUri);

    // --- Redaktionswerkzeug ---

    void createAssignments(long famportalCategoryId, List<Long> geoObjectIds);

    void createAssignmentsByCategories(long famportalCategoryId, List<Long> kiezatlasCategoryIds);

    // ---

    void deleteAssignments(long famportalCategoryId, List<Long> geoObjectIds);

    void deleteAssignmentsByCategories(long famportalCategoryId, List<Long> kiezatlasCategoryIds);

    // ---

    boolean isRelatedToFamportalCategory(Topic geoObject);

    boolean isParentAggregatingTopic(Topic geoObject, long catId);

    GeoObjectCount countAssignments();

}
