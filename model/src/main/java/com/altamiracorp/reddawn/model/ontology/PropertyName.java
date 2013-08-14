package com.altamiracorp.reddawn.model.ontology;

public enum PropertyName {
    TYPE("type"),
    SUBTYPE("subType"),
    DATA_TYPE("dataType"),
    TITLE("title"),
    ONTOLOGY_TITLE("ontologyTitle"),
    GEO_LOCATION("geoLocation"),
    ROW_KEY("_rowKey"),
    COLUMN_FAMILY_NAME("_columnFamilyName"),
    GLYPH_ICON("_glyphIcon"),
    COLOR("_color");

    private final String text;

    PropertyName(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
