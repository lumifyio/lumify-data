package com.altamiracorp.lumify.twitter.json;

/**
 * A String-valued JSON property.
 */
public class StringJsonProperty extends BaseDirectJsonProperty<String> {
    /**
     * Create a new JsonStringProperty.
     * @param key the property key
     */
    public StringJsonProperty(final String key) {
        super(key, JsonType.STRING);
    }
}
