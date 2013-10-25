package com.altamiracorp.lumify.core.model.artifact;

import com.altamiracorp.lumify.core.ingest.ArtifactExtractedInfo;
import com.altamiracorp.lumify.core.model.*;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.model.search.ArtifactSearchResult;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.User;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class ArtifactRepository extends Repository<Artifact> {
    public static final String LUMIFY_VIDEO_PREVIEW_HDFS_PATH = "/lumify/artifacts/video/preview/";
    public static final String LUMIFY_VIDEO_POSTER_FRAME_HDFS_PATH = "/lumify/artifacts/video/posterFrame/";
    public static final int FRAMES_PER_PREVIEW = 20;
    public static final int PREVIEW_FRAME_WIDTH = 360;
    public static final int PREVIEW_FRAME_HEIGHT = 240;
    private final ArtifactBuilder artifactBuilder = new ArtifactBuilder();
    private final GraphSession graphSession;
    private final SearchProvider searchProvider;

    @Inject
    public ArtifactRepository(
            final ModelSession modelSession,
            final GraphSession graphSession,
            final SearchProvider searchProvider) {
        super(modelSession);
        this.graphSession = graphSession;
        this.searchProvider = searchProvider;
    }

    @Override
    public Row toRow(Artifact artifact) {
        return artifact;
    }

    @Override
    public String getTableName() {
        return artifactBuilder.getTableName();
    }

    @Override
    public Artifact fromRow(Row row) {
        return artifactBuilder.fromRow(row);
    }

    public SaveFileResults saveFile(InputStream in, User user) {
        return getModelSession().saveFile(in, user);
    }

    public InputStream getRaw(Artifact artifact, GraphVertex vertex, User user) {
        byte[] bytes = artifact.getMetadata().getRaw();
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }

        String hdfsPath = vertex.getProperty(PropertyName.RAW_HDFS_PATH).toString();
        if (hdfsPath != null) {
            return getModelSession().loadFile(hdfsPath, user);
        }

        return null;
    }

    public GraphVertex saveToGraph(Artifact artifact, ArtifactExtractedInfo artifactExtractedInfo, User user) {
//        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        GraphVertex vertex = null;
//        String oldGraphVertexId = artifact.getGenericMetadata().getGraphVertexId();
//        if (oldGraphVertexId != null) {
//            vertex = graphSession.findGraphVertex(oldGraphVertexId, user);
//        }
//
//        if (vertex == null) {
//            vertex = new InMemoryGraphVertex();
//        }
//
//        vertex.setProperty(PropertyName.TYPE.toString(), VertexType.ARTIFACT.toString());
//        if (artifact.getType() != null) {
//            vertex.setProperty(PropertyName.SUBTYPE.toString(), artifact.getType().toString().toLowerCase());
//        }
//        vertex.setProperty(PropertyName.ROW_KEY.toString(), artifact.getRowKey().toString());
//        if (artifact.getDynamicMetadata().getLatitude() != null) {
//            double latitude = artifact.getDynamicMetadata().getLatitude();
//            double longitude = artifact.getDynamicMetadata().getLongitude();
//            vertex.setProperty(PropertyName.GEO_LOCATION.toString(), new GraphGeoLocation(latitude, longitude));
//        }
//        if (artifact.getGenericMetadata().getSubject() != null) {
//            vertex.setProperty(PropertyName.TITLE.toString(), artifact.getGenericMetadata().getSubject());
//        }
//
//        if (artifact.getGenericMetadata().getSource() != null) {
//            vertex.setProperty(PropertyName.SOURCE.toString(), artifact.getGenericMetadata().getSource());
//        }
//
//        if (artifact.getPublishedDate() != null) {
//            vertex.setProperty(PropertyName.PUBLISHED_DATE.toString(), artifact.getPublishedDate().getTime());
//        }
//
//        String vertexId = graphSession.save(vertex, user);
//        graphSession.commit();
//        if (!vertexId.equals(oldGraphVertexId)) {
//            artifact.getGenericMetadata().setGraphVertexId(vertexId);
//            save(artifact, user);
//        }
//
//        return vertex;

        GraphVertex artifactVertex = null;
        String oldGraphVertexId = artifact.getMetadata().getGraphVertexId();
        if (oldGraphVertexId != null) {
            artifactVertex = graphSession.findGraphVertex(oldGraphVertexId, user);
        }
        if (artifactVertex == null) {
            artifactVertex = new InMemoryGraphVertex();
        }

        artifactVertex.setProperty(PropertyName.ROW_KEY.toString(), artifact.getRowKey().toString());
        artifactVertex.setProperty(PropertyName.TYPE, VertexType.ARTIFACT.toString());
        artifactVertex.setProperty(PropertyName.SUBTYPE, artifactExtractedInfo.getArtifactType());
        artifactVertex.setProperty(PropertyName.TITLE, artifactExtractedInfo.getTitle());
        if (artifactExtractedInfo.getRawHdfsPath() != null) {
            artifactVertex.setProperty(PropertyName.RAW_HDFS_PATH, artifactExtractedInfo.getRawHdfsPath());
        }
        if (artifactExtractedInfo.getTextHdfsPath() != null) {
            artifactVertex.setProperty(PropertyName.TEXT_HDFS_PATH, artifactExtractedInfo.getTextHdfsPath());
            artifactVertex.setProperty(PropertyName.HIGHLIGHTED_TEXT_HDFS_PATH, artifactExtractedInfo.getTextHdfsPath());
        }
        if (artifactExtractedInfo.getDetectedObjects() != null) {
            artifactVertex.setProperty(PropertyName.DETECTED_OBJECTS, artifactExtractedInfo.getDetectedObjects());
        }
        String vertexId = graphSession.save(artifactVertex, user);
        graphSession.commit();

        if (!vertexId.equals(oldGraphVertexId)) {
            artifact.getMetadata().setGraphVertexId(vertexId);
            save(artifact, user);
        }
        return artifactVertex;
    }

    public Artifact createArtifactFromInputStream(long size, InputStream in, String fileName, long fileTimestamp, User user) throws IOException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        Artifact artifact;
//
//        if (size > Artifact.MAX_SIZE_OF_INLINE_FILE) {
//            try {
//                SaveFileResults saveResults = saveFile(in, user);
//                artifact = new Artifact(saveResults.getRowKey());
//                artifact.getGenericMetadata()
//                        .setHdfsFilePath(saveResults.getFullPath())
//                        .setFileSize(size);
//            } finally {
//                in.close();
//            }
//        } else {
//            artifact = new Artifact();
//            byte[] data = IOUtils.toByteArray(in);
//            artifact.getContent().setDocArtifactBytes(data);
//            artifact.getGenericMetadata().setFileSize((long) data.length);
//        }
//
//        artifact.getContent()
//                .setSecurity("U"); // TODO configurable?
//        artifact.getGenericMetadata()
//                .setFileName(FilenameUtils.getBaseName(fileName))
//                .setFileExtension(FilenameUtils.getExtension(fileName))
//                .setFileTimestamp(fileTimestamp);
//
//        return artifact;
    }

    public List<GraphVertex> search(String query, JSONArray filter, User user) throws Exception {
        Collection<ArtifactSearchResult> artifactSearchResults = searchProvider.searchArtifacts(query, user);
        List<String> artifactGraphVertexIds = getGraphVertexIds(artifactSearchResults);
        return graphSession.searchVerticesWithinGraphVertexIds(artifactGraphVertexIds, filter, user);
    }

    private List<String> getGraphVertexIds(Collection<ArtifactSearchResult> artifactSearchResults) {
        ArrayList<String> results = new ArrayList<String>();
        for (ArtifactSearchResult artifactSearchResult : artifactSearchResults) {
            Preconditions.checkNotNull(artifactSearchResult.getGraphVertexId(), "graph vertex cannot be null for artifact " + artifactSearchResult.getRowKey());
            results.add(artifactSearchResult.getGraphVertexId());
        }
        return results;
    }

    public void writeHighlightedTextTo(GraphVertex artifactVertex, OutputStream out, User user) throws IOException {
        checkNotNull(artifactVertex);
        checkNotNull(out);
        checkNotNull(user);

        String hdfsPath = (String) artifactVertex.getProperty(PropertyName.HIGHLIGHTED_TEXT_HDFS_PATH);
        if (hdfsPath == null) {
            String artifactRowKey = (String) artifactVertex.getProperty(PropertyName.ROW_KEY);
            Artifact artifact = findByRowKey(artifactRowKey, user);
            if( artifact != null ) {
                ArtifactMetadata metadata = artifact.getMetadata();
                if( metadata != null ) {
                    String highlightedText = metadata.getHighlightedText();
                    if( highlightedText != null ) {
                        out.write(highlightedText.getBytes());
                    }
                }
            }
        } else {
            InputStream in = getModelSession().loadFile(hdfsPath, user);
            IOUtils.copy(in, out);
        }
    }

    public InputStream getVideoPreviewImage(ArtifactRowKey artifactRowKey, User user) {
        return getModelSession().loadFile(getVideoPreviewPath(artifactRowKey.toString()), user);
    }

    public static String getVideoPreviewPath(String artifactRowKey) {
        return LUMIFY_VIDEO_PREVIEW_HDFS_PATH + artifactRowKey;
    }

    public InputStream getRawPosterFrame(String artifactRowKey, User user) {
        return getModelSession().loadFile(LUMIFY_VIDEO_POSTER_FRAME_HDFS_PATH + artifactRowKey, user);
    }

}
