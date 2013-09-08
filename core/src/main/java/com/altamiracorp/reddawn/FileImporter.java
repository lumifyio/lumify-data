package com.altamiracorp.reddawn;

import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class FileImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImporter.class.getName());
    public static final String MAPPING_JSON_FILE_NAME_SUFFIX = ".mapping.json";

    private ArtifactRepository artifactRepository = new ArtifactRepository();

    public Result writeFile(RedDawnSession redDawnSession, File file, String source, JSONObject mappingJson) throws IOException, MutationsRejectedException {
        if (file.getName().startsWith(".")) {
            return null;
        }
        Artifact artifact = artifactRepository.createArtifactFromInputStream(
                redDawnSession.getModelSession(),
                file.length(),
                new FileInputStream(file),
                file.getName(),
                file.lastModified());
        artifact.getGenericMetadata().setSource(source);
        if (mappingJson != null) {
            artifact.getGenericMetadata().setMappingJson(mappingJson);
        }

        LOGGER.info("Writing artifact: " + artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension() + " (rowId: " + artifact.getRowKey().toString() + ")");
        artifactRepository.save(redDawnSession.getModelSession(), artifact);
        GraphVertex graphVertex = artifactRepository.saveToGraph(redDawnSession.getModelSession(), redDawnSession.getGraphSession(), artifact);
        return new Result(file, artifact, graphVertex);
    }

    public ArrayList<Result> writePackage(RedDawnSession session, File file, String source) throws Exception {
        ArrayList<Result> results;
        ZipFile zipped = new ZipFile(file);
        if (zipped.isValidZipFile()) {
            File tempDir = Files.createTempDir();
            try {
                LOGGER.info("Extracting: " + file.getAbsoluteFile() + " to " + tempDir.getAbsolutePath());
                zipped.extractAll(tempDir.getAbsolutePath());

                results = writeDirectory(session, tempDir, "*", source);
            } finally {
                FileUtils.deleteDirectory(tempDir);
            }
        } else {
            results = new ArrayList<Result>();
            Result r = writeFile(session, file, source, null);
            results.add(r);
        }
        return results;
    }

    public ArrayList<Result> writeDirectory(RedDawnSession redDawnSession, File directory, String pattern, String source) throws Exception {
        ArrayList<Result> results = new ArrayList<Result>();
        IOFileFilter fileFilter = new WildcardFileFilter(pattern);
        IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
        Iterator<File> fileIterator = FileUtils.iterateFiles(directory, fileFilter, directoryFilter);

        while (fileIterator.hasNext()) {
            File f = fileIterator.next();
            if (f.isFile() && !f.getName().endsWith(FileImporter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
                JSONObject mappingJson = readMappingJsonFile(f);
                Result r = writeFile(redDawnSession, f, source, mappingJson);
                if (r != null) {
                    results.add(r);
                }
            }
        }
        return results;
    }

    private JSONObject readMappingJsonFile(File f) throws JSONException, IOException {
        File mappingJsonFile = new File(f.getAbsolutePath() + FileImporter.MAPPING_JSON_FILE_NAME_SUFFIX);
        JSONObject mappingJson = null;
        if (mappingJsonFile.exists()) {
            FileInputStream mappingJsonFileIn = new FileInputStream(mappingJsonFile);
            try {
                mappingJson = new JSONObject(IOUtils.toString(mappingJsonFileIn));
            } finally {
                mappingJsonFileIn.close();
            }
        }
        return mappingJson;
    }

    public static class Result {
        private final String name;
        private final String artifactRowKey;
        private final String graphVertexId;

        public Result(File file, Artifact artifact, GraphVertex graphVertex) {
            this.name = file.getName();
            this.artifactRowKey = artifact.getRowKey().toString();
            this.graphVertexId = graphVertex.getId();
        }

        public String getName() {
            return name;
        }

        public String getArtifactRowKey() {
            return artifactRowKey;
        }

        public String getGraphVertexId() {
            return graphVertexId;
        }

        public JSONObject toJson() {
            try {
                JSONObject json = new JSONObject();
                json.put("name", getName());
                json.put("artifactRowKey", getArtifactRowKey());
                json.put("graphVertexId", getGraphVertexId());
                return json;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        public static JSONArray toJson(Collection<Result> results) {
            JSONArray r = new JSONArray();
            for (Result result : results) {
                r.put(result.toJson());
            }
            return r;
        }
    }
}
