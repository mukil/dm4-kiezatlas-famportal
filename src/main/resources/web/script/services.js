angular.module("famportal").service("famportalService", function($http) {

    this.getUsername = function(callback) {
        $http.get("/famportal/user").success(callback)
    }

    this.getFamportalTree = function(callback) {
        var FAMPORTAL_ROOT = "famportal.root"
        $http.get("/core/topic/by_value/uri/" + FAMPORTAL_ROOT + "?include_childs=true").success(callback)
    }

    this.getWorkspaceId = function(callback) {
        $http.get("/famportal/workspace").success(callback)
    }

    this.getGeoObjectsByCategory = function(famportalCatId, callback) {
        $http.get("/site/category/" + famportalCatId + "/objects").success(callback)
    }

    this.getFacettedTopic = function(topicId, facetTypeUris, callback) {
        $http.get("/facet/topic/" + topicId + "?" + queryString("facet_type_uri", facetTypeUris)).success(callback)
    }

    /** Switch to name search endpoint of dm4-kiezatlas-website plugin. */
    this.searchGeoObjects = function() {
        var req = new ClockedRequest()
        return function(searchTerm, callback) {
            req.perform("GET", "/famportal/search/name", {search: searchTerm}, callback)
        }
    }()

    this.searchCategories = function() {
        var req = new ClockedRequest()
        return function(searchTerm, callback) {
            req.perform("GET", "/famportal/category/objects", {search: searchTerm}, callback)
        }
    }()

    this.createAssignments = function(famportalCatId, geoObjectIds, callback) {
        $http.put("/famportal/category/" + famportalCatId + "?" + queryString("geo_object", geoObjectIds))
            .success(callback)
    }

    this.createAssignmentsByCategories = function(famportalCatId, kiezatlasCatIds, callback) {
        $http.put("/famportal/category/" + famportalCatId + "/ka_cat?" + queryString("ka_cat", kiezatlasCatIds))
            .success(callback)
    }

    this.deleteAssignments = function(famportalCatId, geoObjectIds, callback) {
        $http.delete("/famportal/category/" + famportalCatId + "?" + queryString("geo_object", geoObjectIds))
            .success(callback)
    }

    this.deleteAssignmentsByCategories = function(famportalCatId, kiezatlasCatIds, callback) {
        $http.delete("/famportal/category/" + famportalCatId + "/ka_cat?" + queryString("ka_cat", kiezatlasCatIds))
            .success(callback)
    }

    this.countAssignments = function(callback) {
        $http.get("/famportal/count").success(callback)
    }

    // ---

    function ClockedRequest() {

        var clock = 0

        this.perform = function(method, url, params, callback) {
            clock++
            params.clock = clock
            $http({method: method, url: url, params: params}).success(function(data) {
                if (data.clock == clock) {
                    callback(data)
                } else {
                    console.log("Obsolete response for", method, url, params,
                        "(client clock=" + clock + ", response clock=" + data.clock + ")")
                }
            })
        }
    }

    // ---

    function queryString(paramName, values) {
        var params = []
        angular.forEach(values, function(value) {
            params.push(paramName + "=" + value)
        })
        return params.join("&")
    }
})
