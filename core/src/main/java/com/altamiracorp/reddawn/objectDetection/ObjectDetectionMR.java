package com.altamiracorp.reddawn.objectDetection;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.AccumuloVideoFrameInputFormat;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ObjectDetectionMR extends ConfigurableMapJobBase {

    private static final String JOB_TYPE = "jobType";
    private static final String DEFAULT_JOB_TYPE = "artifact";

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        Class<? extends InputFormat> inputFormatClass;
        String type = job.getConfiguration().get(JOB_TYPE, DEFAULT_JOB_TYPE);

        if (type.equals("videoFrame")) {
            AccumuloVideoFrameInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
            inputFormatClass = AccumuloVideoFrameInputFormat.class;
        } else {
            AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
            inputFormatClass = AccumuloArtifactInputFormat.class;
        }

        return inputFormatClass;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        Class<? extends Mapper> mapperClass;
        String type = job.getConfiguration().get(JOB_TYPE, DEFAULT_JOB_TYPE);
        try {
            if (type.equals("videoFrame")) {
                VideoFrameObjectDetectionMapper.init(job);
                mapperClass = VideoFrameObjectDetectionMapper.class;
            } else {
                ArtifactObjectDetectionMapper.init(job);
                mapperClass = ArtifactObjectDetectionMapper.class;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return mapperClass;
    }

    public static class ArtifactObjectDetectionMapper extends Mapper<Text, Artifact, Text, Artifact> {

        private ObjectDetector objectDetector = new ObjectDetector();
        private RedDawnSession session;
        private String classifierPath;
        private String classifierConcept;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = createRedDawnSession(context);
            classifierConcept = ObjectDetectionMapperHelper.getClassifierConcept(context);
            classifierPath = ObjectDetectionMapperHelper.resolveClassifierPath(context);
        }

        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            if (artifact.getType() != ArtifactType.IMAGE) {
                return;
            }

            List<DetectedObject> detectedObjects = objectDetector.detectObjects(session, artifact, classifierPath);
            if (!detectedObjects.isEmpty()) {
                for (DetectedObject detectedObject : detectedObjects) {
                    artifact.getArtifactDetectedObjects().addDetectedObject(classifierConcept, ObjectDetector.MODEL, detectedObject.getCoordStrings());
                }
                context.write(new Text(Artifact.TABLE_NAME), artifact);
            }
        }

        public static void init(Job job) throws URISyntaxException {
            Configuration conf = job.getConfiguration();
            String pathPrefix = conf.get(ObjectDetectionMapperHelper.PATH_PREFIX, ObjectDetectionMapperHelper.DEFAULT_PATH_PREFIX);
            if (pathPrefix.startsWith("hdfs://")) {
                String classifierName = conf.get(ObjectDetectionMapperHelper.CLASSIFIER, ObjectDetectionMapperHelper.DEFAULT_CLASSIFIER);
                DistributedCache.addCacheFile(new URI(pathPrefix + "/conf/opencv/" + classifierName), conf);
            }
        }
    }

    public static class VideoFrameObjectDetectionMapper extends Mapper<Text, VideoFrame, Text, VideoFrame> {

        private ObjectDetector objectDetector = new ObjectDetector();
        private RedDawnSession session;
        private String classifierPath;
        private String classifierConcept;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = createRedDawnSession(context);
            classifierConcept = ObjectDetectionMapperHelper.getClassifierConcept(context);
            classifierPath = ObjectDetectionMapperHelper.resolveClassifierPath(context);
        }

        public void map(Text rowKey, VideoFrame videoFrame, Context context) throws IOException, InterruptedException {
            List<DetectedObject> detectedObjects = objectDetector.detectObjects(session, videoFrame, classifierPath);
            if (!detectedObjects.isEmpty()) {
                for (DetectedObject detectedObject : detectedObjects) {
                    videoFrame.getDetectedObjects().addDetectedObject(classifierConcept, ObjectDetector.MODEL, detectedObject.getCoordStrings());
                }
                context.write(new Text(VideoFrame.TABLE_NAME), videoFrame);
            }
        }

        public static void init(Job job) throws URISyntaxException {
            Configuration conf = job.getConfiguration();
            String pathPrefix = conf.get(ObjectDetectionMapperHelper.PATH_PREFIX, ObjectDetectionMapperHelper.DEFAULT_PATH_PREFIX);
            if (pathPrefix.startsWith("hdfs://")) {
                String classifierName = conf.get(ObjectDetectionMapperHelper.CLASSIFIER, ObjectDetectionMapperHelper.DEFAULT_CLASSIFIER);
                DistributedCache.addCacheFile(new URI(pathPrefix + "/conf/opencv/" + classifierName), conf);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ObjectDetectionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
