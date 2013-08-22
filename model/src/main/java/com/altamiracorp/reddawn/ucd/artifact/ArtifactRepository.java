package com.altamiracorp.reddawn.ucd.artifact;

import com.altamiracorp.reddawn.model.*;
import com.altamiracorp.reddawn.model.graph.GraphGeoLocation;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.GraphVertexImpl;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

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

    public GraphVertex saveToGraph(Session session, GraphSession graphSession, Artifact artifact) {
        GraphVertex vertex = null;
        String oldGraphVertexId = artifact.getGenericMetadata().getGraphVertexId();
        if (oldGraphVertexId != null) {
            vertex = graphSession.findGraphVertex(oldGraphVertexId);
        }

        if (vertex == null) {
            vertex = new GraphVertexImpl();
        }

        vertex.setProperty(PropertyName.TYPE.toString(), VertexType.ARTIFACT.toString());
        vertex.setProperty(PropertyName.SUBTYPE.toString(), artifact.getType().toString().toLowerCase());
        vertex.setProperty(PropertyName.ROW_KEY.toString(), artifact.getRowKey().toString());
        if (artifact.getDynamicMetadata().getLatitude() != null) {
            double latitude = artifact.getDynamicMetadata().getLatitude();
            double longitude = artifact.getDynamicMetadata().getLongitude();
            vertex.setProperty(PropertyName.GEO_LOCATION.toString(), new GraphGeoLocation(latitude, longitude));
        }
        if (artifact.getGenericMetadata().getSubject() != null) {
            vertex.setProperty(PropertyName.TITLE.toString(), artifact.getGenericMetadata().getSubject());
        }

        String vertexId = graphSession.save(vertex);
        if (!vertexId.equals(oldGraphVertexId)) {
            artifact.getGenericMetadata().setGraphVertexId(vertexId);
            this.save(session, artifact);
        }

        return vertex;
    }

    public Artifact createArtifactFromInputStream(Session session, long size, InputStream in, String fileName, long fileTimestamp) throws IOException {
        Artifact artifact;

        if (size > Artifact.MAX_SIZE_OF_INLINE_FILE) {
            try {
                SaveFileResults saveResults = saveFile(session, in);
                artifact = new Artifact(saveResults.getRowKey());
                artifact.getGenericMetadata()
                        .setHdfsFilePath(saveResults.getFullPath())
                        .setFileSize(size);
            } finally {
                in.close();
            }
        } else {
            artifact = new Artifact();
            byte[] data = IOUtils.toByteArray(in);
            artifact.getContent().setDocArtifactBytes(data);
            artifact.getGenericMetadata().setFileSize((long) data.length);
        }

        artifact.getContent()
                .setSecurity("U"); // TODO configurable?
        artifact.getGenericMetadata()
                .setFileName(FilenameUtils.getBaseName(fileName))
                .setFileExtension(FilenameUtils.getExtension(fileName))
                .setFileTimestamp(fileTimestamp);

        return artifact;
    }
}
