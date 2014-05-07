package io.lumify.foodTruck;

import io.lumify.core.model.properties.types.TextLumifyProperty;

public class FoodTruckOntology {
    public static final String EDGE_LABEL_HAS_KEYWORD = "http://lumify.io/foodtruck#tweetHasKeyword";

    public static final String CONCEPT_TYPE_FOOD_TRUCK = "http://lumify.io/foodtruck#foodTruck";
    public static final String CONCEPT_TYPE_LOCATION_KEYWORD = "http://lumify.io/foodtruck#locationKeyword";

    public static final TextLumifyProperty GEO_LOCATION = TextLumifyProperty.all("http://lumify.io/foodtruck#geoLocation");
}
