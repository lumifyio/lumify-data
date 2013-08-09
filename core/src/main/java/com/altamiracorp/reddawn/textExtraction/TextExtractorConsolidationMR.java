package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.model.Column;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;


public class TextExtractorConsolidationMR extends ConfigurableMapJobBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractorConsolidationMR.class.getName());

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return TextExtractorConsolidationMapper.class;
    }

    public static class TextExtractorConsolidationMapper extends Mapper<Text, Artifact, Text, Artifact> {

        @Override
        public void map(Text rowKey, Artifact artifact, Context context) throws IOException {
            try {
                LOGGER.info("Consolidating extracted text for artifact: " + artifact.getRowKey().toString());
                StringBuilder consolidatedText = new StringBuilder();
                Iterator<Column> columnIterator = artifact.getArtifactExtractedText().getColumns().iterator();
                while (columnIterator.hasNext()) {
                    consolidatedText.append(columnIterator.next().getValue().toString());
                    if (columnIterator.hasNext()) {
                        consolidatedText.append("\n\n");
                    }
                }

                if (StringUtils.isBlank(consolidatedText.toString())) {
                    artifact.getContent().setDocExtractedText((artifact.getGenericMetadata().getFileName()
                            + "."
                            + artifact.getGenericMetadata().getFileExtension()).getBytes());
                } else {
                    artifact.getContent().setDocExtractedText(consolidatedText.toString().getBytes());
                }

                context.write(new Text(Artifact.TABLE_NAME), artifact);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

    }

    @Override
    protected boolean hasConfigurableClassname (){
        return false;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new TextExtractorConsolidationMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
