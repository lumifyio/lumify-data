package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
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

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TextExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractionMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        TextExtractorMapper.init(job, clazz);
        return TextExtractorMapper.class;
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

    public static class TextExtractorMapper extends Mapper<Text, Artifact, Text, Artifact> {
        private ArtifactRepository artifactRepository = new ArtifactRepository();
        public static final String CONF_TEXT_EXTRACTOR_CLASS = "textExtractorClass";
        private TextExtractor textExtractor;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                textExtractor = (TextExtractor) context.getConfiguration().getClass(CONF_TEXT_EXTRACTOR_CLASS, AsciiTextExtractor.class).newInstance();
                textExtractor.setup(context);
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try {
                LOGGER.info("Extracting text from artifact: " + artifact.getRowKey().toString());
                ExtractedInfo extractedInfo = textExtractor.extract(new ByteArrayInputStream(artifact.getContent().getDocArtifactBytes()));
                artifact.getContent().setDocExtractedText(extractedInfo.getText().getBytes());
                artifact.getGenericMetadata()
                        .setSubject(extractedInfo.getSubject())
                        .setMimeType(extractedInfo.getMediaType());
                context.write(new Text(Artifact.TABLE_NAME), artifact);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public static void init(Job job, Class<? extends TextExtractor> textExtractorClass) {
            job.getConfiguration().setClass(CONF_TEXT_EXTRACTOR_CLASS, textExtractorClass, TextExtractor.class);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new TextExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}

