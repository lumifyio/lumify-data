package com.altamiracorp.lumify.ucd.artifact;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.Column;
import com.altamiracorp.lumify.model.ColumnFamily;
import com.altamiracorp.lumify.model.GraphSession;
import com.altamiracorp.lumify.model.ModelSession;
import com.altamiracorp.lumify.model.Repository;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.SaveFileResults;
import com.altamiracorp.lumify.model.graph.GraphGeoLocation;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.google.inject.Inject;


public class ArtifactRepository extends Repository<Artifact> {
    private final GraphSession graphSession;

    @Inject
    public ArtifactRepository(final ModelSession modelSession, final GraphSession graphSession) {
        super(modelSession);
        this.graphSession = graphSession;
    }

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

    public SaveFileResults saveFile(InputStream in, User user) {
        return getModelSession().saveFile(in, user);
    }

    public InputStream getRaw(Artifact artifact, User user) {
        byte[] bytes = artifact.getContent().getDocArtifactBytes();
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }

        String hdfsPath = artifact.getGenericMetadata().getHdfsFilePath();
        if (hdfsPath != null) {
            return getModelSession().loadFile(hdfsPath, user);
        }

        return null;
    }

    public BufferedImage getRawAsImage(Artifact artifact, User user) {
        InputStream in = getRaw(artifact, user);
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

    public InputStream getRawMp4(Artifact artifact, User user) {
        String path = artifact.getGenericMetadata().getMp4HdfsFilePath();
        if (path == null) {
            throw new RuntimeException("MP4 Video file path not set.");
        }
        return getModelSession().loadFile(path, user);
    }

    public long getRawMp4Length(Artifact artifact, User user) {
        String path = artifact.getGenericMetadata().getMp4HdfsFilePath();
        if (path == null) {
            throw new RuntimeException("MP4 Video file path not set.");
        }
        return getModelSession().getFileLength(path, user);
    }

    public InputStream getRawWebm(Artifact artifact, User user) {
        String path = artifact.getGenericMetadata().getWebmHdfsFilePath();
        if (path == null) {
            throw new RuntimeException("WebM Video file path not set.");
        }
        return getModelSession().loadFile(path, user);
    }

    public long getRawWebmLength(Artifact artifact, User user) {
        String path = artifact.getGenericMetadata().getWebmHdfsFilePath();
        if (path == null) {
            throw new RuntimeException("WebM Video file path not set.");
        }
        return getModelSession().getFileLength(path, user);
    }

    public InputStream getRawPosterFrame(Artifact artifact, User user) {
        String path = artifact.getGenericMetadata().getPosterFrameHdfsFilePath();
        if (path == null) {
            throw new RuntimeException("Poster Frame file path not set.");
        }
        return getModelSession().loadFile(path, user);
    }

    public InputStream getVideoPreviewImage(Artifact artifact, User user) {
        String path = artifact.getGenericMetadata().getVideoPreviewImageHdfsFilePath();
        if (path == null) {
            throw new RuntimeException("Video preview image path not set.");
        }
        return getModelSession().loadFile(path, user);
    }

    public GraphVertex saveToGraph(Artifact artifact, User user) {
        GraphVertex vertex = null;
        String oldGraphVertexId = artifact.getGenericMetadata().getGraphVertexId();
        if (oldGraphVertexId != null) {
            vertex = graphSession.findGraphVertex(oldGraphVertexId, user);
        }

        if (vertex == null) {
            vertex = new InMemoryGraphVertex();
        }

        vertex.setProperty(PropertyName.TYPE.toString(), VertexType.ARTIFACT.toString());
        if (artifact.getType() != null) {
            vertex.setProperty(PropertyName.SUBTYPE.toString(), artifact.getType().toString().toLowerCase());
        }
        vertex.setProperty(PropertyName.ROW_KEY.toString(), artifact.getRowKey().toString());
        if (artifact.getDynamicMetadata().getLatitude() != null) {
            double latitude = artifact.getDynamicMetadata().getLatitude();
            double longitude = artifact.getDynamicMetadata().getLongitude();
            vertex.setProperty(PropertyName.GEO_LOCATION.toString(), new GraphGeoLocation(latitude, longitude));
        }
        if (artifact.getGenericMetadata().getSubject() != null) {
            vertex.setProperty(PropertyName.TITLE.toString(), artifact.getGenericMetadata().getSubject());
        }

        if (artifact.getGenericMetadata().getSource() != null) {
            vertex.setProperty(PropertyName.SOURCE.toString(), artifact.getGenericMetadata().getSource());
        }

        if (artifact.getPublishedDate() != null) {
            vertex.setProperty(PropertyName.PUBLISHED_DATE.toString(), artifact.getPublishedDate().getTime());
        }

        String vertexId = graphSession.save(vertex, user);
        graphSession.commit();
        if (!vertexId.equals(oldGraphVertexId)) {
            artifact.getGenericMetadata().setGraphVertexId(vertexId);
            save(artifact, user);
        }

        return vertex;
    }

    public Artifact createArtifactFromInputStream(long size, InputStream in, String fileName, long fileTimestamp, User user) throws IOException {
        Artifact artifact;

        if (size > Artifact.MAX_SIZE_OF_INLINE_FILE) {
            try {
                SaveFileResults saveResults = saveFile(in, user);
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
