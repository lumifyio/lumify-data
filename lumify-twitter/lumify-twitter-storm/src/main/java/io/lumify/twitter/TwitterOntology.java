package io.lumify.twitter;

import io.lumify.core.model.properties.types.TextLumifyProperty;

public class TwitterOntology {
    public static final String EDGE_LABEL_TWEETED = "http://lumify.io/twitter#tweeted";

    public static final String USER_CONCEPT_TYPE = "http://lumify.io/twitter#user";
    public static final String TWEET_CONCEPT_TYPE = "http://lumify.io/twitter#tweet";

    public static final TextLumifyProperty PROFILE_IMAGE_URL = TextLumifyProperty.all("http://lumify.io/twitter#profileImageUrl");
}
