package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
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

public class EntityHighlightMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHighlightMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return EntityHighlightMapper.class;
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

    public static class EntityHighlightMapper extends Mapper<Text, Artifact, Text, Artifact> {
        private EntityHighlighter entityHighlighter = new EntityHighlighter();
        private RedDawnSession session;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
        }

        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            byte[] docExtractedText = artifact.getContent().getDocExtractedText();
            if (docExtractedText == null || docExtractedText.length < 1) {
                return;
            }

            try {
                LOGGER.info("Creating highlight text for: " + artifact.getRowKey().toString());
                String highlightedText = entityHighlighter.getHighlightedText(session, artifact);
                if (highlightedText != null) {
                    artifact.getContent().setHighlightedText(highlightedText);
                    context.write(new Text(Artifact.TABLE_NAME), artifact);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new EntityHighlightMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }

}

