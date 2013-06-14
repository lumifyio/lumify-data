package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.AccumuloSentenceInputFormat;
import com.altamiracorp.reddawn.ucd.artifactTermIndex.ArtifactTermIndex;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
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

    public static class EntityExtractorMapper extends Mapper<Text, Sentence, Text, Row> {
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "entityExtractorClass";
        private EntityExtractor entityExtractor;

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

        public void map(Text rowKey, Sentence sentence, Context context) throws IOException, InterruptedException {
            try {
                Collection<Term> terms = extractEntities(sentence);
                writeEntities(context, terms, sentence);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private void writeEntities(Context context, Collection<Term> terms, Sentence sentence) throws IOException, InterruptedException {
            for (Term term : terms) {
                context.write(new Text(Term.TABLE_NAME), term);

                // TODO these lines are copied from UcdClient#writeTerm not really sure of a good way to abstract these out
                for (TermMention termMention : term.getTermMentions()) {
                    AddArtifactTermIndex(context, term, termMention);
                    AddSentenceTerms(context, term, termMention, sentence);
                }
            }
        }

        private void AddSentenceTerms(Context context, Term term, TermMention termMention, Sentence sentence) throws IOException, InterruptedException {
//            sentence.addTermMention(termMention);
//            context.write(new Text(Sentence.TABLE_NAME), sentence);
        }

        private void AddArtifactTermIndex(Context context, Term term, TermMention termMention) throws IOException, InterruptedException {
            ArtifactTermIndex artifactTermIndex = new ArtifactTermIndex(termMention.getArtifactKey());
            artifactTermIndex.addTermMention(term.getRowKey(), termMention);
            context.write(new Text(ArtifactTermIndex.TABLE_NAME), artifactTermIndex);
        }

        private Collection<Term> extractEntities(Sentence sentence) throws Exception {
            return entityExtractor.extract(sentence);
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

