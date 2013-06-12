package com.altamiracorp.reddawn.sentenceExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceData;
import com.altamiracorp.reddawn.ucd.sentence.SentenceMetadata;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class SentenceExtractorMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceExtractorMR.class.getName());
    public static final String EXTRACTOR_ID = "RD";

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return SentenceExtractorMapper.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class SentenceExtractorMapper extends Mapper<Text, Artifact, Text, Sentence> {
        private RedDawnSession session;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            session = ConfigurableMapJobBase.createRedDawnSession(context);
        }

        @Override
        protected void map(Text key, Artifact artifact, Context context) throws IOException, InterruptedException {
            super.map(key, artifact, context);
            LOGGER.info("Creating highlight text for: " + artifact.getRowKey().toString());

            try {
                Collection<Sentence> terms = extractSentences(artifact);
                writeSentences(context, terms);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private Collection<Sentence> extractSentences(Artifact artifact) {
            ArrayList<Sentence> result = new ArrayList<Sentence>();
            Sentence sentence = new Sentence();
            SentenceData data = sentence.getData();
            data.setArtifactId(artifact.getRowKey().toString());
            data.setStart(0L);
            data.setEnd(100L);
            data.setText("Hello World!");
            SentenceMetadata metaData = sentence.getMetadata();
            metaData.setAuthor(artifact.getGenericMetadata().getAuthor());
            metaData.setContentHash("Hello World!".getBytes());
            metaData.setDate(new Date().getTime());
            metaData.setExtractorId(EXTRACTOR_ID);
            metaData.setSecurityMarking("U");
            result.add(sentence);
            return result;
        }

        private void writeSentences(Mapper.Context context, Collection<Sentence> sentences) throws IOException, InterruptedException {
            for (Sentence sentence : sentences) {
                context.write(new Text(Sentence.TABLE_NAME), sentence);
            }
        }
    }
}
