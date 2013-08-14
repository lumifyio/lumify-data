package com.altamiracorp.reddawn.entityHighlight;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static class EntityHighlightMapper extends RedDawnMapper<Text, Artifact, Text, Artifact> {
        private EntityHighlighter entityHighlighter = new EntityHighlighter();

        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            byte[] docExtractedText = artifact.getContent().getDocExtractedText();
            if (docExtractedText == null || docExtractedText.length < 1) {
                return;
            }

            LOGGER.info("Creating highlight text for: " + artifact.getRowKey().toString());
            String highlightedText = entityHighlighter.getHighlightedText(getSession(), artifact);
            if (highlightedText != null) {
                artifact.getContent().setHighlightedText(highlightedText);
                context.write(new Text(Artifact.TABLE_NAME), artifact);
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

