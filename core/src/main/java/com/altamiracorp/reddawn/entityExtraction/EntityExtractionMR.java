package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.AccumuloSentenceInputFormat;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Collection;

public class EntityExtractionMR extends ConfigurableMapJobBase {
    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        EntityExtractorMapper.init(job, clazz);
        return EntityExtractorMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloSentenceInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloSentenceInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class EntityExtractorMapper extends RedDawnMapper<Text, Sentence, Text, Row> {
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "entityExtractorClass";
        private EntityExtractor entityExtractor;
        private TermRepository termRepository = new TermRepository();
        private SentenceRepository sentenceRepository = new SentenceRepository();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                entityExtractor = (EntityExtractor) context.getConfiguration().getClass(CONF_ENTITY_EXTRACTOR_CLASS, NullEntityExtractor.class).newInstance();
                entityExtractor.setup(context);
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        public void safeMap(Text rowKey, Sentence sentence, Context context) throws Exception {
            Collection<Term> terms = entityExtractor.extract(sentence);
            writeEntities(terms, sentence);
        }

        private void writeEntities(Collection<Term> terms, Sentence sentence) throws IOException, InterruptedException {
            for (Term term : terms) {
                termRepository.save(getSession().getModelSession(), term);
                sentenceRepository.save(getSession().getModelSession(), sentence, term);
            }
        }

        public static void init(Job job, Class<? extends EntityExtractor> entityExtractor) {
            job.getConfiguration().setClass(CONF_ENTITY_EXTRACTOR_CLASS, entityExtractor, EntityExtractor.class);
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new EntityExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}

