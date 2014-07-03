package io.lumify.core.ingest;

import com.google.inject.Inject;
import io.lumify.core.bootstrap.InjectHelper;
import io.lumify.core.model.properties.LumifyProperties;
import io.lumify.core.model.properties.RawLumifyProperties;
import io.lumify.core.model.workQueue.WorkQueueRepository;
import io.lumify.core.model.workspace.Workspace;
import io.lumify.core.model.workspace.WorkspaceRepository;
import io.lumify.core.security.LumifyVisibility;
import io.lumify.core.security.LumifyVisibilityProperties;
import io.lumify.core.security.VisibilityTranslator;
import io.lumify.core.user.User;
import io.lumify.core.util.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.securegraph.*;
import org.securegraph.property.StreamingPropertyValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static org.securegraph.util.IterableUtils.toList;

public class FileImport {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(FileImport.class);
    private static final String MULTI_VALUE_KEY = FileImport.class.getName();
    private final VisibilityTranslator visibilityTranslator;
    private List<FileImportSupportingFileHandler> fileImportSupportingFileHandlers;
    private Graph graph;
    private WorkQueueRepository workQueueRepository;
    private WorkspaceRepository workspaceRepository;

    @Inject
    public FileImport(VisibilityTranslator visibilityTranslator) {
        this.visibilityTranslator = visibilityTranslator;
    }

    public List<Vertex> importDirectory(File dataDir, boolean queueDuplicates, String visibilitySource, Workspace workspace, User user, Authorizations authorizations) throws IOException {
        ensureInitialized();

        ArrayList<Vertex> results = new ArrayList<Vertex>();

        LOGGER.debug("Importing files from %s", dataDir);
        File[] files = dataDir.listFiles();
        if (files == null || files.length == 0) {
            return results;
        }

        int totalFileCount = files.length;
        int fileCount = 0;
        int importedFileCount = 0;
        try {
            for (File f : files) {
                if (f.getName().startsWith(".") || f.length() == 0) {
                    continue;
                }
                if (isSupportingFile(f)) {
                    continue;
                }

                LOGGER.debug("Importing file (%d/%d): %s", fileCount + 1, totalFileCount, f.getAbsolutePath());
                try {
                    Vertex vertex = importFile(f, queueDuplicates, visibilitySource, workspace, user, authorizations);
                    results.add(vertex);
                    importedFileCount++;
                } catch (Exception ex) {
                    LOGGER.error("Could not import %s", f.getAbsolutePath(), ex);
                }
                fileCount++;
            }
        } finally {
            graph.flush();
        }

        LOGGER.debug(String.format("Imported %d, skipped %d files from %s", importedFileCount, fileCount - importedFileCount, dataDir));
        return results;
    }

    private boolean isSupportingFile(File f) {
        for (FileImportSupportingFileHandler fileImportSupportingFileHandler : this.fileImportSupportingFileHandlers) {
            if (fileImportSupportingFileHandler.isSupportingFile(f)) {
                return true;
            }
        }
        return false;
    }

    public Vertex importFile(File f, boolean queueDuplicates, String visibilitySource, Workspace workspace, User user, Authorizations authorizations) throws Exception {
        ensureInitialized();

        String hash = calculateFileHash(f);

        Vertex vertex = findExistingVertexWithHash(hash, authorizations);
        if (vertex != null) {
            LOGGER.warn("vertex already exists with hash %s", hash);
            if (queueDuplicates) {
                pushOnQueue(vertex);
            }
            return vertex;
        }

        List<FileImportSupportingFileHandler.AddSupportingFilesResult> addSupportingFilesResults = new ArrayList<FileImportSupportingFileHandler.AddSupportingFilesResult>();

        FileInputStream fileInputStream = new FileInputStream(f);
        try {
            JSONObject metadataJson = loadMetadataJson(f);
            String predefinedId = null;
            if (metadataJson != null) {
                predefinedId = metadataJson.optString("id", null);
                String metadataVisibilitySource = metadataJson.optString("visibilitySource", null);
                if (metadataVisibilitySource != null) {
                    visibilitySource = metadataVisibilitySource;
                }
            }

            StreamingPropertyValue rawValue = new StreamingPropertyValue(fileInputStream, byte[].class);
            rawValue.searchIndex(false);

            JSONObject visibilityJson = GraphUtil.updateVisibilitySourceAndAddWorkspaceId(null, visibilitySource, workspace == null ? null : workspace.getId());
            LumifyVisibility lumifyVisibility = this.visibilityTranslator.toVisibility(visibilityJson);
            Visibility visibility = lumifyVisibility.getVisibility();
            Map<String, Object> propertyMetadata = new HashMap<String, Object>();
            LumifyProperties.CONFIDENCE.setMetadata(propertyMetadata, 0.1);
            LumifyVisibilityProperties.VISIBILITY_JSON_PROPERTY.setMetadata(propertyMetadata, visibilityJson);

            VertexBuilder vertexBuilder;
            if (predefinedId == null) {
                vertexBuilder = this.graph.prepareVertex(visibility);
            } else {
                vertexBuilder = this.graph.prepareVertex(predefinedId, visibility);
            }
            LumifyVisibilityProperties.VISIBILITY_JSON_PROPERTY.addPropertyValue(vertexBuilder, MULTI_VALUE_KEY, visibilityJson, visibility);
            RawLumifyProperties.RAW.addPropertyValue(vertexBuilder, MULTI_VALUE_KEY, rawValue, propertyMetadata, visibility);
            LumifyProperties.TITLE.addPropertyValue(vertexBuilder, MULTI_VALUE_KEY, f.getName(), propertyMetadata, visibility);
            LumifyProperties.CONTENT_HASH.addPropertyValue(vertexBuilder, MULTI_VALUE_KEY, hash, propertyMetadata, visibility);
            RawLumifyProperties.FILE_NAME.addPropertyValue(vertexBuilder, MULTI_VALUE_KEY, f.getName(), propertyMetadata, visibility);
            RawLumifyProperties.FILE_NAME_EXTENSION.addPropertyValue(vertexBuilder, MULTI_VALUE_KEY, FilenameUtils.getExtension(f.getName()), propertyMetadata, visibility);
            RawLumifyProperties.CREATE_DATE.addPropertyValue(vertexBuilder, MULTI_VALUE_KEY, new Date(f.lastModified()), propertyMetadata, visibility);

            for (FileImportSupportingFileHandler fileImportSupportingFileHandler : this.fileImportSupportingFileHandlers) {
                FileImportSupportingFileHandler.AddSupportingFilesResult addSupportingFilesResult = fileImportSupportingFileHandler.addSupportingFiles(vertexBuilder, f, visibility);
                if (addSupportingFilesResult != null) {
                    addSupportingFilesResults.add(addSupportingFilesResult);
                }
            }

            vertex = vertexBuilder.save(authorizations);
            graph.flush();

            if (workspace != null) {
                workspaceRepository.updateEntityOnWorkspace(workspace, vertex.getId(), null, null, null, user);
            }

            LOGGER.debug("File %s imported. vertex id: %s", f.getAbsolutePath(), vertex.getId().toString());
            pushOnQueue(vertex);
            return vertex;
        } finally {
            fileInputStream.close();
            for (FileImportSupportingFileHandler.AddSupportingFilesResult addSupportingFilesResult : addSupportingFilesResults) {
                addSupportingFilesResult.close();
            }
        }
    }

    private JSONObject loadMetadataJson(File f) throws IOException {
        File metadataFile = MetadataFileImportSupportingFileHandler.getMetadataFile(f);
        if (metadataFile.exists()) {
            FileInputStream in = new FileInputStream(metadataFile);
            try {
                String fileContents = IOUtils.toString(in);
                return new JSONObject(fileContents);
            } finally {
                in.close();
            }
        }
        return null;
    }

    private void ensureInitialized() {
        if (fileImportSupportingFileHandlers == null) {
            fileImportSupportingFileHandlers = toList(ServiceLoaderUtil.load(FileImportSupportingFileHandler.class));
            for (FileImportSupportingFileHandler fileImportSupportingFileHandler : fileImportSupportingFileHandlers) {
                InjectHelper.inject(fileImportSupportingFileHandler);
            }
        }
    }

    private void pushOnQueue(Vertex vertex) {
        LOGGER.debug("pushing %s on to %s queue", vertex.getId().toString(), WorkQueueRepository.GRAPH_PROPERTY_QUEUE_NAME);
        this.workQueueRepository.pushElement(vertex);
        this.workQueueRepository.pushGraphPropertyQueue(vertex, MULTI_VALUE_KEY, RawLumifyProperties.RAW.getPropertyName());
    }

    private Vertex findExistingVertexWithHash(String hash, Authorizations authorizations) {
        Iterator<Vertex> existingVertices = this.graph.query(authorizations)
                .has(LumifyProperties.CONTENT_HASH.getPropertyName(), hash)
                .vertices()
                .iterator();
        if (existingVertices.hasNext()) {
            return existingVertices.next();
        }
        return null;
    }

    private String calculateFileHash(File f) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(f);
        try {
            return RowKeyHelper.buildSHA256KeyString(fileInputStream);
        } finally {
            fileInputStream.close();
        }
    }

    @Inject
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    @Inject
    public void setWorkQueueRepository(WorkQueueRepository workQueueRepository) {
        this.workQueueRepository = workQueueRepository;
    }

    @Inject
    public void setWorkspaceRepository(WorkspaceRepository workspaceRepository) {
        this.workspaceRepository = workspaceRepository;
    }
}
