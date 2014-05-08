package io.lumify.foodTruck;

import io.lumify.core.model.properties.types.DateLumifyProperty;
import io.lumify.core.model.properties.types.GeoPointLumifyProperty;

public class FoodTruckOntology {
    public static final String EDGE_LABEL_HAS_KEYWORD = "http://lumify.io/foodtruck#tweetHasKeyword";
    public static final String EDGE_LABEL_HAS_TWITTER_USER = "http://lumify.io/foodtruck#foodTruckHasTwitterUser";

    public static final String CONCEPT_TYPE_FOOD_TRUCK = "http://lumify.io/foodtruck#foodTruck";
    public static final String CONCEPT_TYPE_LOCATION_KEYWORD = "http://lumify.io/foodtruck#locationKeyword";

    public static final DateLumifyProperty GEO_LOCATION_DATE = new DateLumifyProperty("http://lumify.io/foodtruck#geoLocationDate");
}
