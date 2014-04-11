package com.altamiracorp.lumify.javaCodeIngest;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorker;
import com.altamiracorp.securegraph.Property;
import com.altamiracorp.securegraph.Vertex;

import java.io.InputStream;

public class JarFileGraphPropertyWorker extends GraphPropertyWorker {
    @Override
    public void execute(InputStream in, GraphPropertyWorkData data) throws Exception {

    }

    @Override
    public boolean isHandled(Vertex vertex, Property property) {
        return false;
    }
}
