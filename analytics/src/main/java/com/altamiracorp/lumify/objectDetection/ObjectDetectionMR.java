package com.altamiracorp.lumify.objectDetection;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.core.ingest.ArtifactDetectedObject;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.model.AccumuloVideoFrameInputFormat;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.videoFrames.VideoFrame;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.google.inject.Injector;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
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
            com.altamiracorp.lumify.config.Configuration c = getConfiguration();
            AccumuloVideoFrameInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
            inputFormatClass = AccumuloVideoFrameInputFormat.class;
        } else {
            com.altamiracorp.lumify.config.Configuration c = getConfiguration();
            AccumuloArtifactInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
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
        Class<? extends ObjectDetectionMapper> mapperClass;
        String type = job.getConfiguration().get(JOB_TYPE, DEFAULT_JOB_TYPE);
        try {
            ObjectDetectionMapper.init(job, clazz);
            if (type.equals("videoFrame")) {
                mapperClass = VideoFrameObjectDetectionMapper.class;
            } else {
                mapperClass = ArtifactObjectDetectionMapper.class;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return mapperClass;
    }

    public static abstract class ObjectDetectionMapper<T extends Row> extends LumifyMapper<Text, T, Text, T> {
        private static final String OPEN_CV_CONF_DIR = "/conf/opencv/";
        private static final String CONCEPT = "classifier.concept";
        private static final String DEFAULT_CONCEPT = "face";

        public static final String PATH_PREFIX = "openCVConfPathPrefix";
        public static final String DEFAULT_PATH_PREFIX = "hdfs://";
        public static final String CLASSIFIER = "classifier.file";
        public static final String DEFAULT_CLASSIFIER = "haarcascade_frontalface_alt.xml";
        public static final String DICTIONARY = "dictionary.file";

        public static final String OBJECT_DETECTOR_CLASS = "objectDetectorClass";

        protected ObjectDetector objectDetector;
        protected String classifierPath;
        protected String classifierConcept;

        @Override
        protected void setup(Context context, Injector injector) throws IOException, IllegalAccessException, InstantiationException {
            classifierConcept = context.getConfiguration().get(CONCEPT, DEFAULT_CONCEPT);
            classifierPath = resolveClassifierPath(context);
            objectDetector = getAndInjectClassFromConfiguration(context, injector, OBJECT_DETECTOR_CLASS);
            String dictionaryFile = context.getConfiguration().get(DICTIONARY);
            if (dictionaryFile != null) {
                FileSystem fs = FileSystem.get(context.getConfiguration());
                Path dictionaryPath = new Path(context.getConfiguration().get(PATH_PREFIX, DEFAULT_PATH_PREFIX) + OPEN_CV_CONF_DIR + dictionaryFile);
                objectDetector.setup(classifierPath, fs.open(dictionaryPath));
            } else {
                objectDetector.setup(classifierPath);
            }
        }

        private String resolveClassifierPath(Context context) throws IOException {
            FileSystem fs = FileSystem.get(context.getConfiguration());
            String classifierName = context.getConfiguration().get(CLASSIFIER, DEFAULT_CLASSIFIER);
            String pathPrefix = context.getConfiguration().get(PATH_PREFIX, DEFAULT_PATH_PREFIX);
            String classifierPath = pathPrefix + OPEN_CV_CONF_DIR + classifierName;

            if (pathPrefix.startsWith("hdfs://")) { //if it is in HDFS, copy it to local disk so opencv can read it
                LocalFileSystem localFS = FileSystem.getLocal(context.getConfiguration());
                classifierPath = localFS.getWorkingDirectory().toUri().getPath() + OPEN_CV_CONF_DIR + classifierName;
                File localDir = new File(classifierPath).getParentFile();
                if (!localDir.exists()) {
                    localDir.mkdirs();
                }

                Path hdfsPath = new Path(pathPrefix + OPEN_CV_CONF_DIR + classifierName);
                fs.copyToLocalFile(false, hdfsPath, new Path(classifierPath));
            }

            return classifierPath;
        }

        public static void init(Job job, Class<? extends ObjectDetector> objectDetector) throws URISyntaxException {
            job.getConfiguration().setClass(OBJECT_DETECTOR_CLASS, objectDetector, ObjectDetector.class);
        }
    }

    public static class ArtifactObjectDetectionMapper extends ObjectDetectionMapper<Artifact> {
        private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactObjectDetectionMapper.class);

        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//            if (artifact.getType() != ArtifactType.IMAGE) {
//                return;
//            }
//
//            LOGGER.info("Detecting objects of concept " + classifierConcept + " for artifact " + rowKey.toString());
//            List<DetectedObject> detectedObjects = objectDetector.detectObjects(artifact, getUser());
//            if (!detectedObjects.isEmpty()) {
//                for (DetectedObject detectedObject : detectedObjects) {
//                    artifact.getArtifactDetectedObjects().addDetectedObject(classifierConcept, objectDetector.getModelName(),
//                            detectedObject.getX1(), detectedObject.getY1(), detectedObject.getX2(), detectedObject.getY2());
//                }
//                context.write(new Text(Artifact.TABLE_NAME), artifact);
//            }
        }
    }

    public static class VideoFrameObjectDetectionMapper extends ObjectDetectionMapper<VideoFrame> {
        private static final Logger LOGGER = LoggerFactory.getLogger(VideoFrameObjectDetectionMapper.class);

        public void safeMap(Text rowKey, VideoFrame videoFrame, Context context) throws Exception {
            LOGGER.info("Detecting objects of concept " + classifierConcept + " for video frame " + rowKey.toString());
            List<ArtifactDetectedObject> detectedObjects = objectDetector.detectObjects(videoFrame, getUser());
            if (!detectedObjects.isEmpty()) {
                for (ArtifactDetectedObject detectedObject : detectedObjects) {
                    videoFrame.getDetectedObjects().addDetectedObject(classifierConcept, objectDetector.getModelName(), detectedObject.getX1(), detectedObject.getY1(), detectedObject.getX2(), detectedObject.getY2());
                }
                context.write(new Text(VideoFrame.TABLE_NAME), videoFrame);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ObjectDetectionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
