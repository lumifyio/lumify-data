package com.altamiracorp.lumify.storm;

import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class BaseTextProcessingBolt extends BaseLumifyBolt {
    protected InputStream getInputStream(GraphVertex graphVertex) throws Exception {
        checkNotNull(graphVertex, "graphVertex cannot be null");

        InputStream textIn;
        String textHdfsPath = (String) graphVertex.getProperty(PropertyName.TEXT_HDFS_PATH);
        if (textHdfsPath != null) {
            textIn = openFile(textHdfsPath);
        } else {
            String artifactRowKey = (String) graphVertex.getProperty(PropertyName.ROW_KEY);
            Artifact artifact = artifactRepository.findByRowKey(artifactRowKey, getUser());
            textIn = new ByteArrayInputStream(artifact.getMetadata().getText().getBytes());
        }
        return textIn;
    }

    protected String getText(GraphVertex graphVertex) throws Exception {
        return IOUtils.toString(getInputStream(graphVertex));
    }
}
