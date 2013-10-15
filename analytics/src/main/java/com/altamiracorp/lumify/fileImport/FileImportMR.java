package com.altamiracorp.lumify.fileImport;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.FileImporter;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactRepository;
import com.altamiracorp.lumify.core.ingest.video.VideoTranscript;
import com.altamiracorp.lumify.videoConversion.SubRip;
import com.altamiracorp.lumify.videoConversion.YoutubeccReader;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class FileImportMR extends ConfigurableMapJobBase {

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        return WholeFileInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return FileImportMapper.class;
    }

    public static class FileImportMapper extends LumifyMapper<MapWritable, Text, Text, Artifact> {
        private ArtifactRepository artifactRepository;
        private FileSystem fs;
        private final YoutubeccReader youtubeccReader = new YoutubeccReader();

        @Override
        public void setup(Context context, Injector injector) throws IOException {
            fs = FileSystem.get(context.getConfiguration());
        }

        @Override
        protected void safeMap(MapWritable metadata, Text value, Context context) throws Exception {
            long length = ((LongWritable) metadata.get(new Text("length"))).get();
            String name = metadata.get(new Text("name")).toString();
            long lastModified = ((LongWritable) metadata.get(new Text("lastModified"))).get();

            if (isSupportingFile(name)) {
                return;
            }
            Path p = new Path(value.toString());
            Artifact artifact = artifactRepository.createArtifactFromInputStream(
                    length,
                    fs.open(p),
                    name,
                    lastModified,
                    getUser());

            JSONObject mappingJson = readMappingJsonFile(p);
            VideoTranscript videoTranscript = readVideoTranscript(p);

            artifactRepository.saveToGraph(artifact, getUser());
            context.write(new Text(Artifact.TABLE_NAME), artifact);
        }

        private boolean isSupportingFile(String name) {
            if (name.endsWith(FileImporter.MAPPING_JSON_FILE_NAME_SUFFIX)) {
                return true;
            }
            if (name.endsWith(FileImporter.YOUTUBE_CC_FILE_NAME_SUFFIX)) {
                return true;
            }
            if (name.endsWith((FileImporter.SRT_CC_FILE_NAME_SUFFIX))) {
                return true;
            }
            return false;
        }

        private JSONObject readMappingJsonFile(Path p) throws JSONException, IOException {
            Path mappingJsonFile = new Path(p.toString() + FileImporter.MAPPING_JSON_FILE_NAME_SUFFIX);
            JSONObject mappingJson = null;
            if (fs.exists(mappingJsonFile)) {
                InputStream mappingJsonFileIn = fs.open(mappingJsonFile);
                try {
                    mappingJson = new JSONObject(IOUtils.toString(mappingJsonFileIn));
                } finally {
                    mappingJsonFileIn.close();
                }
            }
            return mappingJson;
        }

        private VideoTranscript readVideoTranscript(Path p) throws Exception {
            VideoTranscript videoTranscript = null;
            Path youtubeccFile = new Path(p.toString() + FileImporter.YOUTUBE_CC_FILE_NAME_SUFFIX);
            if (fs.exists(youtubeccFile)) {
                videoTranscript = new VideoTranscript();
                VideoTranscript youtubeccTranscript = youtubeccReader.read(fs.open(youtubeccFile));
                videoTranscript.merge(youtubeccTranscript);
            }

            Path srtccFile = new Path(p.toString() + FileImporter.SRT_CC_FILE_NAME_SUFFIX);
            if ( fs.exists(srtccFile) ) {
                videoTranscript = videoTranscript == null ? new VideoTranscript() : videoTranscript;
                VideoTranscript youtubeccTranscript = SubRip.read(fs.open(srtccFile));
                videoTranscript.merge(youtubeccTranscript);
            }
            return videoTranscript;
        }

        @Inject
        public void setArtifactRepository(ArtifactRepository artifactRepository) {
            this.artifactRepository = artifactRepository;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new FileImportMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
