package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.ucd.model.Artifact;
import com.altamiracorp.reddawn.ucd.model.ArtifactContent;
import com.altamiracorp.reddawn.ucd.model.ArtifactGenericMetadata;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TextExtractionMR extends ConfigurableMapJobBase {
    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        TextExtractorMapper.init(job, clazz);
        return TextExtractorMapper.class;
    }

    public static class TextExtractorMapper extends Mapper<Text, Artifact, Text, Mutation> {
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

        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try {
                ExtractedInfo extractedInfo = textExtractor.extract(new ByteArrayInputStream(artifact.getContent().getDocArtifactBytes()));
                Mutation mutation = new Mutation(artifact.getKey().toString());
                mutation.put(ArtifactContent.COLUMN_FAMILY_NAME, ArtifactContent.COLUMN_DOC_EXTRACTED_TEXT, extractedInfo.getText());
                mutation.put(ArtifactGenericMetadata.COLUMN_FAMILY_NAME, ArtifactGenericMetadata.COLUMN_SUBJECT, extractedInfo.getSubject());
                mutation.put(ArtifactGenericMetadata.COLUMN_FAMILY_NAME, ArtifactGenericMetadata.COLUMN_MIME_TYPE, extractedInfo.getMediaType());
                context.write(new Text(Artifact.TABLE_NAME), mutation);
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

