package com.altamiracorp.reddawn.videoConversion;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class VideoConversionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoConversionMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return VideoConversionMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class VideoConversionMapper extends Mapper<Text, Artifact, Text, Artifact> {
        public static final String CONF_FFMPEG_BIN_DIR = "ffmpeg.bin.dir";
        public static final String CONF_FFMPEG_LIB_DIR = "ffmpeg.lib.dir";
        private RedDawnSession session;
        private FFMPEGVideoConversion videoConversion = new FFMPEGVideoConversion();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
            videoConversion.setFFMPEGBinDir(context.getConfiguration().get(CONF_FFMPEG_BIN_DIR, FFMPEGVideoConversion.DEFAULT_FFMPEG_BIN_DIR));
            videoConversion.setFFMPEGLibDir(context.getConfiguration().get(CONF_FFMPEG_LIB_DIR, FFMPEGVideoConversion.DEFAULT_FFMPEG_LIB_DIR));
        }

        @Override
        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try {
                if (artifact.getType() != ArtifactType.VIDEO) {
                    return;
                }
                LOGGER.info("Converting video for artifact: " + artifact.getRowKey().toString());
                videoConversion.convert(session, artifact);
                context.write(new Text(Artifact.TABLE_NAME), artifact);
            } catch (Exception e) {
                throw new IOException(e);
            }
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

