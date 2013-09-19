package com.altamiracorp.lumify.videoConversion;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.ucd.artifact.ArtifactType;
import com.google.inject.Inject;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoConversionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoConversionMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return VideoConversionMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        Configuration c = getConfiguration();
        AccumuloArtifactInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class VideoConversionMapper extends LumifyMapper<Text, Artifact, Text, Artifact> {
        private FFMPEGVideoConversion videoConversion;

        @Override
        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            if (artifact.getType() != ArtifactType.VIDEO) {
                return;
            }
            LOGGER.info("Converting video for artifact: " + artifact.getRowKey().toString());
            videoConversion.convert(artifact, getUser());
            context.write(new Text(Artifact.TABLE_NAME), artifact);
        }

        @Inject
        public void setVideoConversion(FFMPEGVideoConversion videoConversion) {
            this.videoConversion = videoConversion;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new VideoConversionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}

