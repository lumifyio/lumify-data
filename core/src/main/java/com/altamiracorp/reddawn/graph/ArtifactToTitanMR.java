package com.altamiracorp.reddawn.graph;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
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

public class ArtifactToTitanMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactToTitanMR.class.getName());

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return ArtifactToTitan.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class ArtifactToTitan extends Mapper<Text, Artifact, Text, Row> {
        private RedDawnSession session;
        private ArtifactRepository artifactRepository = new ArtifactRepository();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
        }

        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            LOGGER.info("Adding artifact to titan: " + artifact.getRowKey().toString());
            try {
                artifactRepository.saveToGraph(session.getModelSession(), session.getGraphSession(), artifact);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            session.close();
            super.cleanup(context);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new ArtifactToTitanMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }
}
