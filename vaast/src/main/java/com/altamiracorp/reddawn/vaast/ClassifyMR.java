package com.altamiracorp.reddawn.vaast;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.vaast.model.subFrames.AccumuloSubFrameInputFormat;
import com.altamiracorp.reddawn.vaast.model.subFrames.SubFrame;
import com.altamiracorp.reddawn.vaast.model.subFrames.SubFrameRepository;
import com.altamiracorp.reddawn.vaast.model.subFrames.SubFrameRowKey;
import com.altamiracorp.vaast.core.classifier.Classifier;
import com.altamiracorp.vaast.core.classifier.SvmClassifier;
import com.altamiracorp.vaast.core.exception.VaastRangeException;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class ClassifyMR extends ConfigurableMapJobBase {

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloSubFrameInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloSubFrameInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        try {
            ClassifyMapper.init(job, clazz);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return ClassifyMapper.class;
    }

    public static class ClassifyMapper extends Mapper<Text, SubFrame, Text, Artifact> {
        private static final String CLASSIFIER_CLASS = "classifierClass";
        private static final String CLASSIFIER_PATH = "classifierPath";
        private static final String CLASS_MAPPING_PATH = "classMappingPath";
        private static final String DEFAULT_CLASS_MAPPING_PATH = "hdfs:///conf/vaast/classMap.json";
        private static final String DEFAULT_CLASSIFIER_PATH = "hdfs:///conf/vaast/";
        private static final String USE_SPARSE = "useSparse";
        private static final Boolean DEFAULT_USE_SPARSE = true;

        private RedDawnSession session;
        private SubFrameRepository subFrameRepository = new SubFrameRepository();
        private ArtifactRepository artifactRepository = new ArtifactRepository();
        private Classifier classifier;

        @Override
        public void setup(Context context) throws IOException {
            try {
                session = RedDawnSession.create(context);
                classifier = context.getConfiguration().getClass(CLASSIFIER_CLASS, SvmClassifier.class, Classifier.class).newInstance();

                FileSystem fs = FileSystem.get(context.getConfiguration());
                classifier.setup(fs.open(new Path(context.getConfiguration().get(CLASS_MAPPING_PATH,DEFAULT_CLASS_MAPPING_PATH))),resolveClassifierPath(context));
            } catch (Exception e) {
                throw new IOException(e);
            }

        }

        @Override
        public void map(Text rowKey, SubFrame subFrame, Context context) throws IOException {
            InputStream subFrameIn = null;
            try {
                String artifactRowKey = subFrame.getRowKey().getArtifactRowKey();
                Artifact artifact = artifactRepository.findByRowKey(session.getModelSession(), artifactRowKey);
                if (context.getConfiguration().getBoolean(USE_SPARSE, DEFAULT_USE_SPARSE)) {
                    subFrameIn = subFrameRepository.getSparseRaw(session.getModelSession(), subFrame);
                } else {
                    subFrameIn = subFrameRepository.getRaw(session.getModelSession(), subFrame);
                }

                List<String> detectedClasses = classifier.classify(subFrameIn);

                //add the detected classes to the detected objects of the artifact
                for (String detectedClass : detectedClasses) {
                    artifact.getArtifactDetectedObjects().addDetectedObject(detectedClass,
                            classifier.getModelName(),
                            subFrame.getMetadata().getX1(),
                            subFrame.getMetadata().getY1(),
                            subFrame.getMetadata().getX2(),
                            subFrame.getMetadata().getY2());
                }

                context.write(new Text(Artifact.TABLE_NAME), artifact);
            } catch (Exception e) {
                throw new IOException(e);
            } finally {
                IOUtils.closeQuietly(subFrameIn);
            }
        }

        private String resolveClassifierPath(Context context) throws IOException {
            String classifierPath = context.getConfiguration().get(CLASSIFIER_PATH, DEFAULT_CLASSIFIER_PATH);
            String classifierName = FilenameUtils.getName(classifierPath);

            if (classifierPath.startsWith("hdfs://")) {
                //get the classifier file from the distributed cache
                Path[] localFiles = DistributedCache.getLocalCacheFiles(context.getConfiguration());
                for (Path path : localFiles) {
                    if (path.toString().contains(classifierName)) {
                        classifierPath = path.toString();
                        break;
                    }
                }
            } else if (classifierPath.startsWith("file://")) {
                try {
                    File classifierFile = new File(new URI(classifierPath));
                    classifierPath = classifierFile.getAbsolutePath();
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            }

            return classifierPath;
        }

        public static void init(Job job, Class<? extends Classifier> classifierClass) throws URISyntaxException {
            job.getConfiguration().setClass(CLASSIFIER_CLASS, classifierClass, Classifier.class);
            Configuration conf = job.getConfiguration();
            String path = conf.get(CLASSIFIER_PATH, DEFAULT_CLASSIFIER_PATH);
            if (path.startsWith("hdfs://")) {
                DistributedCache.addCacheFile(new URI(path), conf);
            }
        }

    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ClassifyMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
