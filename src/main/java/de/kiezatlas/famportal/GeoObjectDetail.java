package de.kiezatlas.famportal;

import de.deepamehta.core.JSONEnabled;
import de.deepamehta.geomaps.model.GeoCoordinate;

import org.codehaus.jettison.json.JSONObject;



/**
 * A data transfer object as returned by the Familienportal service.
 */
public class GeoObjectDetail implements JSONEnabled {

    // ---------------------------------------------------------------------------------------------- Instance Variables

    private JSONObject json = new JSONObject();

    // -------------------------------------------------------------------------------------------------- Public Methods

    @Override
    public JSONObject toJSON() {
        return json;
    }

    // ----------------------------------------------------------------------------------------- Package Private Methods

    void setName(String name) {
        try {
            json.put("name", name);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a GeoObject failed", e);
        }
    }

    void setStrasseHnr(String strasseHnr) {
        try {
            json.put("strasse_hnr", strasseHnr);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a GeoObject failed", e);
        }
    }

    void setPostleitzahl(String zipCode) {
        try {
            json.put("postleitzahl", zipCode);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a GeoObject failed", e);
        }
    }

    void setStadt(String city) {
        try {
            json.put("stadt", city);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a GeoObject failed", e);
        }
    }

    void setBezirk(String bezirk) {
        try {
            json.put("bezirk", bezirk);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a GeoObject failed", e);
        }
    }

    void setGeoCoordinate(GeoCoordinate geoCoord) {
        try {
            JSONObject geolocation = new JSONObject();
            geolocation.put("lon", geoCoord.lon);
            geolocation.put("lat", geoCoord.lat);
            //
            json.put("geolocation", geolocation);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a GeoObject failed", e);
        }
    }

    void setLink(String link) {
        try {
            json.put("link", link);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a GeoObject failed", e);
        }
    }

    void setId(String id) {
        try {
            json.put("id", id);
        } catch (Exception e) {
            throw new RuntimeException("Constructing a GeoObject failed", e);
        }
    }

}