package com.altamiracorp.reddawn.model.ontology;

public enum PropertyType {
    DATE("date"),
    STRING("string"),
    GEO_LOCATION("geoLocation"),
    CURRENCY("currency");

    private final String text;

    PropertyType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
