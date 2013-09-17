package com.altamiracorp.lumify.entityHighlight;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
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
        Configuration c = getConfiguration();
        AccumuloArtifactInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class EntityHighlightMapper extends LumifyMapper<Text, Artifact, Text, Artifact> {
        private EntityHighlighter entityHighlighter = new EntityHighlighter();
        private static final Text KEY_ARTIFACT_TABLE = new Text(Artifact.TABLE_NAME);

        @Override
        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            final byte[] docExtractedText = artifact.getContent().getDocExtractedText();
            if (docExtractedText == null || docExtractedText.length < 1) {
                return;
            }

            LOGGER.info("Creating highlight text for artifact rowkey: " + artifact.getRowKey().toString());

            final String highlightedText = entityHighlighter.getHighlightedText(getSession(), artifact);
            if (highlightedText != null) {
                artifact.getContent().setHighlightedText(highlightedText);
                context.write(KEY_ARTIFACT_TABLE, artifact);
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

