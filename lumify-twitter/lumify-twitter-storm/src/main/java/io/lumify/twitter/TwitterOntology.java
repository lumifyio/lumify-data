package io.lumify.twitter;

import io.lumify.core.model.properties.types.TextLumifyProperty;

public class TwitterOntology {
    public static final String EDGE_LABEL_TWEETED = "http://lumify.io/twitter#tweeted";
    public static final String EDGE_LABEL_MENTIONED = "http://lumify.io/twitter#mentioned";
    public static final String EDGE_LABEL_REFERENCED_URL = "http://lumify.io/twitter#refUrl";
    public static final String EDGE_LABEL_TAGGED = "http://lumify.io/twitter#tagged";

    public static final String CONCEPT_TYPE_USER = "http://lumify.io/twitter#user";
    public static final String CONCEPT_TYPE_TWEET = "http://lumify.io/twitter#tweet";
    public static final String CONCEPT_TYPE_HASHTAG = "http://lumify.io/twitter#hashtag";
    public static final String CONCEPT_TYPE_URL = "http://lumify.io/twitter#url";

    public static final TextLumifyProperty PROFILE_IMAGE_URL = TextLumifyProperty.all("http://lumify.io/twitter#profileImageUrl");
}
