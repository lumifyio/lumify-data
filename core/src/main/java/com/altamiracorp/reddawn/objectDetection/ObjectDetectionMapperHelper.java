package com.altamiracorp.reddawn.objectDetection;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Mapper.Context;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ObjectDetectionMapperHelper {
    private static final String CONCEPT = "classifier.concept";
    private static final String DEFAULT_CONCEPT = "face";

    public static final String PATH_PREFIX = "openCVConfPathPrefix";
    public static final String DEFAULT_PATH_PREFIX = "hdfs://";
    public static final String CLASSIFIER = "classifier.file";
    public static final String DEFAULT_CLASSIFIER = "haarcascade_frontalface_alt.xml";

    public static String getClassifierConcept (Context context) {
        return context.getConfiguration().get(CONCEPT,DEFAULT_CONCEPT);
    }

    public static String resolveClassifierPath (Context context) throws IOException {
        String classifierPath = null;
        String classifierName = context.getConfiguration().get(CLASSIFIER,DEFAULT_CLASSIFIER);
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
}
