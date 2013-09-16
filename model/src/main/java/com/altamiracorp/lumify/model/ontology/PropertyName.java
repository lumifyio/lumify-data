package com.altamiracorp.lumify.model.ontology;

public enum PropertyName {
    TYPE("_type"),
    SUBTYPE("_subType"),
    DATA_TYPE("_dataType"),
    TITLE("title"),
    ONTOLOGY_TITLE("ontologyTitle"),
    DISPLAY_NAME("displayName"),
    GEO_LOCATION("geoLocation"),
    ROW_KEY("_rowKey"),
    GLYPH_ICON("_glyphIcon"),
    MAP_GLYPH_ICON("_mapGlyphIcon"),
    COLOR("_color"),
    SOURCE("source"),
    START_DATE("startDate"),
    END_DATE("endDate"),
    RELATIONSHIP_TYPE("relationshipType"),
    BOUNDING_BOX("boundingBox"),
    PUBLISHED_DATE("publishedDate"),
    TIME_STAMP("_timeStamp");

    private final String text;

    PropertyName(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return this.text;
    }
}
