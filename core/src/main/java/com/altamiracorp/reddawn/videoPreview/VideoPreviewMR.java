package com.altamiracorp.reddawn.videoPreview;

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

public class VideoPreviewMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoPreviewMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return VideoPreviewMapper.class;
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

    public static class VideoPreviewMapper extends Mapper<Text, Artifact, Text, Artifact> {
        private RedDawnSession session;
        private ViewPreviewGenerator viewPreviewGenerator = new ViewPreviewGenerator();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
        }

        @Override
        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try {
                if (artifact.getType() != ArtifactType.VIDEO) {
                    return;
                }
                LOGGER.info("Creating video preview for artifact: " + artifact.getRowKey().toString());
                viewPreviewGenerator.createPreview(session, artifact);
                context.write(new Text(Artifact.TABLE_NAME), artifact);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new VideoPreviewMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}

