package com.altamiracorp.lumify.javaCodeIngest;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties;
import com.altamiracorp.lumify.core.model.properties.LumifyProperties;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.securegraph.EdgeBuilder;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.VertexBuilder;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;

import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarFileGraphPropertyWorker extends GraphPropertyWorker {
    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {
        OntologyLumifyProperties.CONCEPT_TYPE.setProperty(data.getVertex(), Ontology.CONCEPT_TYPE_JAR_FILE, data.getProperty().getVisibility());
        RawLumifyProperties.MIME_TYPE.setProperty(data.getVertex(), "application/java-archive", data.getProperty().getVisibility());

        JarInputStream jarInputStream = new JarInputStream(in);
        JarEntry jarEntry;
        while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
            if (jarEntry.isDirectory()) {
                continue;
            }

            StreamingPropertyValue rawValue = new StreamingPropertyValue(jarInputStream, byte[].class);
            rawValue.searchIndex(false);

            Vertex jarEntryVertex = createFileVertex(jarEntry, rawValue, data);

            createJarContainsFileEdge(jarEntryVertex, data);

            getGraph().flush();

            getWorkQueueRepository().pushGraphPropertyQueue(jarEntryVertex.getId(), RawLumifyProperties.RAW.getProperty(jarEntryVertex));
        }
    }

    private void createJarContainsFileEdge(Vertex jarEntryVertex, GraphPropertyWorkData data) {
        EdgeBuilder jarContainsEdgeBuilder = getGraph().prepareEdge(data.getVertex(), jarEntryVertex, Ontology.EDGE_LABEL_JAR_CONTAINS, data.getProperty().getVisibility(), getAuthorizations());
        jarContainsEdgeBuilder.save();
    }

    private Vertex createFileVertex(JarEntry jarEntry, StreamingPropertyValue rawValue, GraphPropertyWorkData data) {
        VertexBuilder jarEntryVertexBuilder = getGraph().prepareVertex(data.getProperty().getVisibility(), getAuthorizations());
        LumifyProperties.TITLE.setProperty(jarEntryVertexBuilder, jarEntry.getName(), data.getProperty().getVisibility());
        OntologyLumifyProperties.CONCEPT_TYPE.setProperty(jarEntryVertexBuilder, Ontology.CONCEPT_TYPE_CLASS_FILE, data.getProperty().getVisibility());
        RawLumifyProperties.MIME_TYPE.setProperty(jarEntryVertexBuilder, "application/octet-stream", data.getProperty().getVisibility());
        RawLumifyProperties.FILE_NAME.setProperty(jarEntryVertexBuilder, jarEntry.getName(), data.getProperty().getVisibility());
        RawLumifyProperties.RAW.setProperty(jarEntryVertexBuilder, rawValue, data.getProperty().getVisibility());
        return jarEntryVertexBuilder.save();
    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        if (!property.getName().equals(RawLumifyProperties.RAW.getKey())) {
            return false;
        }

        String fileName = RawLumifyProperties.FILE_NAME.getPropertyValue(vertex);
        if (fileName == null || !fileName.endsWith(".jar")) {
            return false;
        }

        return true;
    }
}
