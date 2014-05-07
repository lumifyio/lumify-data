package io.lumify.foodTruck;

import io.lumify.core.model.properties.types.GeoPointLumifyProperty;

public class FoodTruckOntology {
    public static final String EDGE_LABEL_HAS_KEYWORD = "http://lumify.io/foodtruck#tweetHasKeyword";
    public static final String EDGE_LABEL_HAS_TWITTER_USER = "http://lumify.io/foodtruck#foodTruckHasTwitterUser";

    public static final String CONCEPT_TYPE_FOOD_TRUCK = "http://lumify.io/foodtruck#foodTruck";
    public static final String CONCEPT_TYPE_LOCATION_KEYWORD = "http://lumify.io/foodtruck#locationKeyword";

    public static final GeoPointLumifyProperty GEO_LOCATION = new GeoPointLumifyProperty("http://lumify.io/foodtruck#geoLocation");
}
