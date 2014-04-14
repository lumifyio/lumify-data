package com.altamiracorp.lumify.javaCodeIngest;

import com.altamiracorp.lumify.core.model.properties.types.TextLumifyProperty;
import com.altamiracorp.securegraph.TextIndexHint;

public class Ontology {
    public static final String EDGE_LABEL_JAR_CONTAINS = "http://lumify.io/java-code-ingest#jarFileContains";
    public static final String EDGE_LABEL_CLASS_FILE_CONTAINS_CLASS = "http://lumify.io/java-code-ingest#classFileContainsClass";
    public static final String EDGE_LABEL_CLASS_CONTAINS = "http://lumify.io/java-code-ingest#classContains";
    public static final String EDGE_LABEL_INVOKED = "http://lumify.io/java-code-ingest#invoked";
    public static final String CONCEPT_TYPE_JAR_FILE = "http://lumify.io/java-code-ingest#jarFile";
    public static final String CONCEPT_TYPE_CLASS_FILE = "http://lumify.io/java-code-ingest#classFile";
    public static final String CONCEPT_TYPE_CLASS = "http://lumify.io/java-code-ingest#class";
    public static final String CONCEPT_TYPE_INTERFACE = "http://lumify.io/java-code-ingest#interface";
    public static final String CONCEPT_TYPE_METHOD = "http://lumify.io/java-code-ingest#method";
    public static final String CONCEPT_TYPE_FIELD = "http://lumify.io/java-code-ingest#field";

    public static final TextLumifyProperty CLASS_NAME = new TextLumifyProperty("http://lumify.io/java-code-ingest#className", TextIndexHint.EXACT_MATCH);
}
