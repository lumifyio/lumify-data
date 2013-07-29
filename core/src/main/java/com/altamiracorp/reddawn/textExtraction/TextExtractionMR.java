package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
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
        private RedDawnSession session;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                textExtractor = (TextExtractor) context.getConfiguration().getClass(CONF_TEXT_EXTRACTOR_CLASS, TikaTextExtractor.class).newInstance();
                textExtractor.setup(context);
                session = ConfigurableMapJobBase.createRedDawnSession(context);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try {
                LOGGER.info("Extracting text from artifact: " + artifact.getRowKey().toString());
                ArtifactExtractedInfo extractedInfo = textExtractor.extract(session.getModelSession(), artifact);
                if (extractedInfo == null) {
                    return;
                }

                if (extractedInfo.getText() != null) {
                    artifact.getContent().setDocExtractedText(extractedInfo.getText().getBytes());
                }

                if (extractedInfo.getSubject() != null) {
                    artifact.getGenericMetadata().setSubject(extractedInfo.getSubject());
                }

                if (extractedInfo.getDate() != null) {
                    artifact.getGenericMetadata().setDocumentDtg(extractedInfo.getDate());
                }

                if (extractedInfo.getType() != null) {
                    artifact.getGenericMetadata().setDocumentType(extractedInfo.getType());
                }

                if (extractedInfo.getUrl() != null) {
                    artifact.getGenericMetadata().setSource(extractedInfo.getUrl());
                }

                if (extractedInfo.getExtUrl() != null) {
                    artifact.getGenericMetadata().setExternalUrl(extractedInfo.getExtUrl());
                }

                if (extractedInfo.getSrcType() != null) {
                    artifact.getGenericMetadata().setSourceType(extractedInfo.getSrcType());
                }

                if (extractedInfo.getDate() != null) {
                    artifact.getGenericMetadata().setFileTimestamp(extractedInfo.getDate().getTime() / 1000);
                }

                if (extractedInfo.getRetrievalTime() != null) {
                    artifact.getGenericMetadata().setLoadTimestamp(extractedInfo.getRetrievalTime());
                }

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

