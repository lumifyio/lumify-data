package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.model.graph.GraphGeoLocation;
import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphNodeImpl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;


public class ArtifactRepository extends Repository<Artifact> {
    @Override
    public Row toRow(Artifact artifact) {
        return artifact;
    }

    @Override
    public String getTableName() {
        return Artifact.TABLE_NAME;
    }

    @Override
    public Artifact fromRow(Row row) {
        Artifact artifact = new Artifact(row.getRowKey());
        Collection<ColumnFamily> families = row.getColumnFamilies();
        for (ColumnFamily columnFamily : families) {
            if (columnFamily.getColumnFamilyName().equals(ArtifactContent.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactContent().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(ArtifactGenericMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactGenericMetadata().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(ArtifactDynamicMetadata.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactDynamicMetadata().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(ArtifactDetectedObjects.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactDetectedObjects().addColumns(columns));
            } else if (columnFamily.getColumnFamilyName().equals(ArtifactExtractedText.NAME)) {
                Collection<Column> columns = columnFamily.getColumns();
                artifact.addColumnFamily(new ArtifactExtractedText().addColumns(columns));
            } else {
                artifact.addColumnFamily(columnFamily);
            }
        }
        return artifact;
    }

    public SaveFileResults saveFile(Session session, InputStream in) {
        return session.saveFile(in);
    }

    public InputStream getRaw(Session session, Artifact artifact) {
        byte[] bytes = artifact.getContent().getDocArtifactBytes();
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }

        String hdfsPath = artifact.getGenericMetadata().getHdfsFilePath();
        if (hdfsPath != null) {
            return session.loadFile(hdfsPath);
        }

        return null;
    }

    public BufferedImage getRawAsImage(Session session, Artifact artifact) {
        InputStream in = getRaw(session, artifact);
        try {
            try {
                return ImageIO.read(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read image", e);
        }
    }

    public InputStream getRawMp4(Session session, Artifact artifact) {
        String path = artifact.getGenericMetadata().getMp4HdfsFilePath();
        if (path == null) {
            throw new RuntimeException("MP4 Video file path not set.");
        }
        return session.loadFile(path);
    }

    public long getRawMp4Length(Session session, Artifact artifact) {
        String path = artifact.getGenericMetadata().getMp4HdfsFilePath();
        if (path == null) {
            throw new RuntimeException("MP4 Video file path not set.");
        }
        return session.getFileLength(path);
    }

    public InputStream getRawWebm(Session session, Artifact artifact) {
        String path = artifact.getGenericMetadata().getWebmHdfsFilePath();
        if (path == null) {
            throw new RuntimeException("WebM Video file path not set.");
        }
        return session.loadFile(path);
    }

    public long getRawWebmLength(Session session, Artifact artifact) {
        String path = artifact.getGenericMetadata().getWebmHdfsFilePath();
        if (path == null) {
            throw new RuntimeException("WebM Video file path not set.");
        }
        return session.getFileLength(path);
    }

    public InputStream getRawPosterFrame(Session session, Artifact artifact) {
        String path = artifact.getGenericMetadata().getPosterFrameHdfsFilePath();
        if (path == null) {
            throw new RuntimeException("Poster Frame file path not set.");
        }
        return session.loadFile(path);
    }

    public InputStream getVideoPreviewImage(Session session, Artifact artifact) {
        String path = artifact.getGenericMetadata().getVideoPreviewImageHdfsFilePath();
        if (path == null) {
            throw new RuntimeException("Video preview image path not set.");
        }
        return session.loadFile(path);
    }

    public void saveToGraph(Session session, GraphSession graphSession, Artifact artifact) {
        String suggestedNodeId = artifact.getGraphNodeId();
        GraphNode node = new GraphNodeImpl(suggestedNodeId);
        node.setProperty("type", "artifact");
        node.setProperty("subType", artifact.getType().toString().toLowerCase());
        node.setProperty(GraphSession.PROPERTY_NAME_ROW_KEY, artifact.getRowKey().toString());
        if (artifact.getDynamicMetadata().getLatitude() != null) {
            double latitude = artifact.getDynamicMetadata().getLatitude();
            double longitude = artifact.getDynamicMetadata().getLongitude();
            node.setProperty(GraphSession.PROPERTY_NAME_GEO_LOCATION, new GraphGeoLocation(latitude, longitude));
        }
        if (artifact.getGenericMetadata().getSubject() != null) {
            node.setProperty("title", artifact.getGenericMetadata().getSubject());
        }

        String nodeId = graphSession.save(node);
        if (!nodeId.equals(suggestedNodeId)) {
            artifact.setGraphNodeId(nodeId);
            this.save(session, artifact);
        }
    }
}
