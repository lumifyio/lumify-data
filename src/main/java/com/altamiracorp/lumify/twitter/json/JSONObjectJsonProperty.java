package com.altamiracorp.lumify.twitter.json;

import org.json.JSONObject;

/**
 * A JSON property whose value is a sub-JSONObject.
 */
public class JSONObjectJsonProperty extends BaseDirectJsonProperty<JSONObject> {
    /**
     * Create a new JSONObjectJsonProperty.
     * @param key the property key
     */
    public JSONObjectJsonProperty(final String key) {
        super(key, JsonType.OBJECT);
    }
}
