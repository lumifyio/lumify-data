package io.lumify.twitter;

import io.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import io.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import io.lumify.core.model.ontology.LabelName;
import io.lumify.core.model.ontology.OntologyLumifyProperties;
import io.lumify.core.model.properties.EntityLumifyProperties;
import io.lumify.core.model.properties.LumifyProperties;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.util.LumifyLogger;
import io.lumify.core.util.LumifyLoggerFactory;
import org.securegraph.Property;
import org.securegraph.Text;
import org.securegraph.Vertex;
import org.securegraph.VertexBuilder;
import org.securegraph.property.StreamingPropertyValue;

import java.io.InputStream;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

public class TwitterProfileImageDownloadGraphPropertyWorker extends GraphPropertyWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TwitterProfileImageDownloadGraphPropertyWorker.class);

    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        String profileImageUrlString = getStringFromPropertyValue(data.getProperty().getValue());
        if (profileImageUrlString == null || profileImageUrlString.trim().length() == 0) {
            return;
        }

        String profileImageId = "TWITTER_PROFILE_IMAGE_" + profileImageUrlString;

        Vertex profileImageVertex = getGraph().getVertex(profileImageId, getAuthorizations());
        if (profileImageVertex != null) {
            return;
        }

        LOGGER.debug("downloading: %s", profileImageUrlString);
        URL profileImageUrl = new URL(profileImageUrlString);
        InputStream imageData = profileImageUrl.openStream();
        try {
            String userTitle = LumifyProperties.TITLE.getPropertyValue(data.getVertex());

            StreamingPropertyValue imageValue = new StreamingPropertyValue(imageData, byte[].class);
            imageValue.searchIndex(false);

            VertexBuilder v = getGraph().prepareVertex(profileImageId, data.getVisibility(), getAuthorizations());
            LumifyProperties.TITLE.setProperty(v, "Profile Image of " + userTitle, data.getVisibility());
            RawLumifyProperties.RAW.setProperty(v, imageValue, data.getVisibility());
            OntologyLumifyProperties.CONCEPT_TYPE.setProperty(v, TwitterOntology.CONCEPT_TYPE_PROFILE_IMAGE, data.getVisibility());
            profileImageVertex = v.save();
            LOGGER.debug("created vertex: %s", profileImageVertex.getId());

            getGraph().addEdge(data.getVertex(), profileImageVertex, LabelName.ENTITY_HAS_IMAGE_RAW.toString(), data.getVisibility(), getAuthorizations());
            EntityLumifyProperties.IMAGE_VERTEX_ID.setProperty(data.getVertex(), profileImageVertex.getId().toString(), data.getVisibility());

            getGraph().flush();
        } finally {
            imageData.close();
        }
    }

    private String getStringFromPropertyValue(Object value) {
        checkNotNull(value, "property value cannot be null");
        if (value instanceof String) {
            return (String) value;
        }
        if (value instanceof Text) {
            Text t = (Text) value;
            return t.getText();
        }
        throw new ClassCastException("Could not convert " + value.getClass().getName() + " to string");
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        return property.getName().equals(TwitterOntology.PROFILE_IMAGE_URL.getKey());
    }
}
