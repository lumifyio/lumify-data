package com.altamiracorp.lumify.twitter.json;

/**
 * An Integer-valued JSON property.
 */
public class IntegerJsonProperty extends BaseDirectJsonProperty<Integer> {
    /**
     * Create a new IntegerJsonProperty.
     * @param key the property key
     */
    public IntegerJsonProperty(final String key) {
        super(key, JsonType.INTEGER);
    }
}
