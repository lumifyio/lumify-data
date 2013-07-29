package com.altamiracorp.reddawn.objectDetection;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.AccumuloVideoFrameInputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.videoFrames.VideoFrame;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
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

    public static class ObjectDetectionMapper<T extends Row> extends Mapper<Text, T, Text, T> {
        private static final String CONCEPT = "classifier.concept";
        private static final String DEFAULT_CONCEPT = "face";

        public static final String PATH_PREFIX = "openCVConfPathPrefix";
        public static final String DEFAULT_PATH_PREFIX = "hdfs://";
        public static final String CLASSIFIER = "classifier.file";
        public static final String DEFAULT_CLASSIFIER = "haarcascade_frontalface_alt.xml";

        public static final String OBJECT_DETECTOR_CLASS = "objectDetectorClass";

        protected ObjectDetector objectDetector;
        protected RedDawnSession session;
        protected String classifierPath;
        protected String classifierConcept;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                session = createRedDawnSession(context);
                classifierConcept = context.getConfiguration().get(CONCEPT, DEFAULT_CONCEPT);
                classifierPath = resolveClassifierPath(context);
                objectDetector = (ObjectDetector) context.getConfiguration().getClass(OBJECT_DETECTOR_CLASS, OpenCVObjectDetector.class).newInstance();
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        private String resolveClassifierPath(Context context) throws IOException {
            String classifierPath = null;
            String classifierName = context.getConfiguration().get(CLASSIFIER, DEFAULT_CLASSIFIER);
            String pathPrefix = context.getConfiguration().get(PATH_PREFIX, DEFAULT_PATH_PREFIX);

            if (pathPrefix.startsWith("hdfs://")) {
                //get the classifier file from the distributed cache
                Path[] localFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
                for (Path path : localFiles) {
                    if (path.toString().contains(classifierName)) {
                        classifierPath = path.toString();
                        break;
                    }
                }
            } else if (pathPrefix.startsWith("file://")) {
                try {
                    File classifierFile = new File(new URI(pathPrefix + "/conf/opencv/" + classifierName));
                    classifierPath = classifierFile.getAbsolutePath();
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            } else {
                classifierPath = pathPrefix + "/conf/opencv/" + classifierName;
            }

            return classifierPath;
        }

        public static void init(Job job, Class<? extends ObjectDetector> objectDetector) throws URISyntaxException {
            Configuration conf = job.getConfiguration();
            String pathPrefix = conf.get(PATH_PREFIX, DEFAULT_PATH_PREFIX);
            if (pathPrefix.startsWith("hdfs://")) {
                String classifierName = conf.get(CLASSIFIER, DEFAULT_CLASSIFIER);
                DistributedCache.addCacheFile(new URI(pathPrefix + "/conf/opencv/" + classifierName), conf);
            }

            job.getConfiguration().setClass(OBJECT_DETECTOR_CLASS, objectDetector, ObjectDetector.class);
        }
    }

    public static class ArtifactObjectDetectionMapper extends ObjectDetectionMapper<Artifact> {
        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            if (artifact.getType() != ArtifactType.IMAGE) {
                return;
            }

            List<DetectedObject> detectedObjects = objectDetector.detectObjects(session, artifact, classifierPath);
            if (!detectedObjects.isEmpty()) {
                for (DetectedObject detectedObject : detectedObjects) {
                    artifact.getArtifactDetectedObjects().addDetectedObject(classifierConcept, ObjectDetector.MODEL, detectedObject.getX1(), detectedObject.getY1(), detectedObject.getY1(), detectedObject.getY2());
                }
                context.write(new Text(Artifact.TABLE_NAME), artifact);
            }
        }
    }

    public static class VideoFrameObjectDetectionMapper extends ObjectDetectionMapper<VideoFrame> {
        public void map(Text rowKey, VideoFrame videoFrame, Context context) throws IOException, InterruptedException {
            List<DetectedObject> detectedObjects = objectDetector.detectObjects(session, videoFrame, classifierPath);
            if (!detectedObjects.isEmpty()) {
                for (DetectedObject detectedObject : detectedObjects) {
                    videoFrame.getDetectedObjects().addDetectedObject(classifierConcept, ObjectDetector.MODEL, detectedObject.getX1(), detectedObject.getY1(), detectedObject.getY1(), detectedObject.getY2());
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
