package com.altamiracorp.lumify.twitter.json;

/**
 * Base class for JsonProperty subtypes whose output and JSON values
 * are the same type.
 */
public abstract class BaseDirectJsonProperty<T> extends JsonProperty<T, T> {
    /**
     * Create a new BaseDirectJsonProperty.
     * @param key the property key
     * @param type the JsonType
     */
    protected BaseDirectJsonProperty(final String key, final JsonType type) {
        super(key, type);
    }

    @Override
    protected final T fromJSON(final T jsonValue) {
        return jsonValue;
    }

    @Override
    protected final T toJSON(final T value) {
        return value;
    }
}
