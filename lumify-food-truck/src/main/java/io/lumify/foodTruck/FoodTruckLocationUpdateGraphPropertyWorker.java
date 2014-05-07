package io.lumify.foodTruck;

import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.util.CollectionUtil;
import io.lumify.twitter.TwitterOntology;
import org.securegraph.*;
import org.securegraph.type.GeoPoint;

import java.io.InputStream;

public class FoodTruckLocationUpdateGraphPropertyWorker extends GraphPropertyWorker {
    private static final String MULTI_VALUE_KEY = FoodTruckLocationUpdateGraphPropertyWorker.class.getName();

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        Edge hasKeywordEdge = (Edge) data.getElement();

        Vertex tweetVertex = hasKeywordEdge.getVertex(Direction.OUT, getAuthorizations());
        Vertex keywordVertex = hasKeywordEdge.getVertex(Direction.IN, getAuthorizations());
        Vertex tweeter = CollectionUtil.single(tweetVertex.getVertices(Direction.BOTH, TwitterOntology.EDGE_LABEL_TWEETED, getAuthorizations()));
        Vertex foodTruck = CollectionUtil.singleOrDefault(tweeter.getVertices(Direction.BOTH, FoodTruckOntology.EDGE_LABEL_HAS_TWITTER_USER, getAuthorizations()), null);
        if (foodTruck == null) {
            return;
        }

        GeoPoint geoLocation = FoodTruckOntology.GEO_LOCATION.getPropertyValue(keywordVertex);
        if (geoLocation != null) {
            FoodTruckOntology.GEO_LOCATION.addPropertyValue(foodTruck, MULTI_VALUE_KEY, geoLocation, data.getVisibility());
            getWorkQueueRepository().pushGraphPropertyQueue(foodTruck, FoodTruckOntology.GEO_LOCATION.getProperty(foodTruck));
        }
    }

    @Override
    public boolean isHandled(Element element, Property property) {
        if (!(element instanceof Edge)) {
            return false;
        }

        Edge edge = (Edge) element;
        if (!edge.getLabel().equals(FoodTruckOntology.EDGE_LABEL_HAS_KEYWORD)) {
            return false;
        }

        return true;
    }
}
