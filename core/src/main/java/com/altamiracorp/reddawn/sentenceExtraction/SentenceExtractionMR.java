package com.altamiracorp.reddawn.sentenceExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class SentenceExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceExtractionMR.class.getName());

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
        return SentenceExtractorMapper.class;
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

    public static class SentenceExtractorMapper extends Mapper<Text, Artifact, Text, Sentence> {
        private SentenceExtractor sentenceExtractor;
        public static final String CONF_SENTENCE_EXTRACTOR_CLASS = "sentenceExtractorClass";

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                sentenceExtractor = (SentenceExtractor) context.getConfiguration().getClass(CONF_SENTENCE_EXTRACTOR_CLASS, OpenNlpSentenceExtractor.class).newInstance();
                sentenceExtractor.setup(context);
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        @Override
        protected void map(Text key, Artifact artifact, Context context) throws IOException, InterruptedException {
            if (artifact.getType() != ArtifactType.DOCUMENT) {
                return;
            }

            LOGGER.info("Extracting sentences for artifact: " + artifact.getRowKey().toString());

            try {
                Collection<Sentence> sentences = sentenceExtractor.extractSentences(artifact);
                writeSentences(context, sentences);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private void writeSentences(Mapper.Context context, Collection<Sentence> sentences) throws IOException, InterruptedException {
            for (Sentence sentence : sentences) {
                context.write(new Text(Sentence.TABLE_NAME), sentence);
            }
        }

        public static void init(Job job, Class<? extends SentenceExtractor> sentenceExtractor) {
            job.getConfiguration().setClass(CONF_SENTENCE_EXTRACTOR_CLASS, sentenceExtractor, SentenceExtractor.class);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new SentenceExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
