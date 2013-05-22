package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.ucd.models.*;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.Collection;

public class EntityExtractionMR extends ConfigurableMapJobBase {
    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        EntityExtractorMapper.init(job, clazz);
        return EntityExtractorMapper.class;
    }

    public static class EntityExtractorMapper extends Mapper<Text, Artifact, Text, Mutation> {
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

        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try {
                Collection<Term> terms = extractEntities(artifact);
                writeEntities(context, terms);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        private void writeEntities(Context context, Collection<Term> terms) throws IOException, InterruptedException {
            for (Term term : terms) {
                context.write(new Text(Term.TABLE_NAME), term.getMutation());

                // TODO these lines are copied from UcdClient#writeTerm not really sure of a good way to abstract these out
                for (TermMetadata termMetadata : term.getMetadata()) {
                    ArtifactTermIndex artifactTermIndex = ArtifactTermIndex.newBuilder()
                            .artifactKey(termMetadata.getArtifactKey())
                            .termMention(term.getKey(), termMetadata.getColumnFamilyName())
                            .build();
                    context.write(new Text(ArtifactTermIndex.TABLE_NAME), artifactTermIndex.getMutation());
                }
            }
        }

        private Collection<Term> extractEntities(Artifact artifact) throws Exception {
            ArtifactKey artifactKey = artifact.getKey();
            String text = artifact.getContent().getDocExtractedText();
            return entityExtractor.extract(artifactKey, text);
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

