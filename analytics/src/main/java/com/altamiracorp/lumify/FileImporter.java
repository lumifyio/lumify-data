package com.altamiracorp.lumify;

import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.google.common.io.Files;
import com.google.inject.Inject;
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
    public static final String YOUTUBE_CC_FILE_NAME_SUFFIX = ".youtubecc";
    public static final String SRT_CC_FILE_NAME_SUFFIX = ".srt";
    private final ArtifactRepository artifactRepository;
    // TODO storm refactor
    //private final YoutubeccReader youtubeccReader = new YoutubeccReader();

    @Inject
    public FileImporter(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    public Result writeFile(File file, String source, JSONObject mappingJson, VideoTranscript videoTranscript, User user) throws IOException, MutationsRejectedException {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        if (file.getName().startsWith(".")) {
//            return null;
//        }
//        if (isSupportingFile(file)) {
//            return null;
//        }
//        Artifact artifact = artifactRepository.createArtifactFromInputStream(
//                file.length(),
//                new FileInputStream(file),
//                file.getName(),
//                file.lastModified(),
//                user);
//        artifact.getGenericMetadata().setSubject(artifact.getGenericMetadata().getFileName());
//        artifact.getGenericMetadata().setSource(source);
//        if (mappingJson != null) {
//            artifact.getGenericMetadata().setMappingJson(mappingJson);
//        }
//        if (videoTranscript != null) {
//            artifact.getContent().mergeVideoTranscript(videoTranscript);
//        }
//
//        LOGGER.info("Writing artifact: " + artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension() + " (rowId: " + artifact.getRowKey().toString() + ")");
//        artifactRepository.save(artifact, user);
//        artifact = artifactRepository.findByRowKey(artifact.getRowKey().toString(), user);
//        GraphVertex graphVertex = artifactRepository.saveToGraph(artifact, user);
//        return new Result(file, artifact, graphVertex);
    }

    public ArrayList<Result> writePackage(File file, String source, User user) throws Exception {
        ArrayList<Result> results;
        ZipFile zipped = new ZipFile(file);
        if (zipped.isValidZipFile()) {
            File tempDir = Files.createTempDir();
            try {
                LOGGER.info("Extracting: " + file.getAbsoluteFile() + " to " + tempDir.getAbsolutePath());
                zipped.extractAll(tempDir.getAbsolutePath());

                results = writeDirectory(tempDir, "*", source, user);
            } finally {
                FileUtils.deleteDirectory(tempDir);
            }
        } else {
            results = new ArrayList<Result>();
            Result r = writeFile(file, source, null, null, user);
            results.add(r);
        }
        return results;
    }

    public ArrayList<Result> writeDirectory(File directory, String pattern, String source, User user) throws Exception {
        ArrayList<Result> results = new ArrayList<Result>();
        IOFileFilter fileFilter = new WildcardFileFilter(pattern);
        IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
        Iterator<File> fileIterator = FileUtils.iterateFiles(directory, fileFilter, directoryFilter);

        while (fileIterator.hasNext()) {
            File f = fileIterator.next();
            if (f.isFile() && !isSupportingFile(f)) {
                JSONObject mappingJson = readMappingJsonFile(f);
                VideoTranscript videoTranscript = readVideoTranscript(f);
                Result r = writeFile(f, source, mappingJson, videoTranscript, user);
                if (r != null) {
                    results.add(r);
                }
            }
        }
        return results;
    }

    private boolean isSupportingFile(File f) {
        if (f.getName().endsWith(FileImporter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
            return true;
        }
        if (f.getName().endsWith(FileImporter.YOUTUBE_CC_FILE_NAME_SUFFIX)) {
            return true;
        }
        if (f.getName().endsWith((FileImporter.SRT_CC_FILE_NAME_SUFFIX))) {
            return true;
        }
        return false;
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

    private VideoTranscript readVideoTranscript(File f) throws Exception {
        throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//        VideoTranscript videoTranscript = null;
//        File youtubeccFile = new File(f.getAbsolutePath() + FileImporter.YOUTUBE_CC_FILE_NAME_SUFFIX);
//        if (youtubeccFile.exists()) {
//            videoTranscript = new VideoTranscript();
//            VideoTranscript youtubeccTranscript = youtubeccReader.read(youtubeccFile);
//            videoTranscript.merge(youtubeccTranscript);
//        }
//
//        File srtccFile = new File(f.getAbsolutePath() + FileImporter.SRT_CC_FILE_NAME_SUFFIX);
//        if ( srtccFile.exists() ) {
//            videoTranscript = videoTranscript == null ? new VideoTranscript() : videoTranscript;
//            VideoTranscript youtubeccTranscript = SubRip.read(srtccFile);
//            videoTranscript.merge(youtubeccTranscript);
//        }
//        return videoTranscript;
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
