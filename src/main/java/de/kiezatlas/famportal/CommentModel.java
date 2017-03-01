package de.kiezatlas.famportal;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author malted
 */
public class CommentModel {
    
    public String message;
    public String contact;

    public CommentModel(JSONObject object) throws JSONException {
        message = object.getString("message");
        contact = object.getString("contact");
    }

}
