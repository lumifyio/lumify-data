package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.model.ontology.Concept;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.termMention.TermMention;
import com.altamiracorp.reddawn.model.termMention.TermMentionRepository;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class EntityExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenNlpEntityExtractor.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        EntityExtractorMapper.init(job, clazz);
        return EntityExtractorMapper.class;
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

    public static class EntityExtractorMapper extends RedDawnMapper<Text, Artifact, Text, Row> {
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "entityExtractorClass";
        private EntityExtractor entityExtractor;
        private TermMentionRepository termMentionRepository = new TermMentionRepository();
        private OntologyRepository ontologyRepository = new OntologyRepository();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                entityExtractor = (EntityExtractor) context.getConfiguration().getClass(CONF_ENTITY_EXTRACTOR_CLASS, null).newInstance();
                entityExtractor.setup(context);
            } catch (InstantiationException e) {
                throw new IOException(e);
            } catch (IllegalAccessException e) {
                throw new IOException(e);
            }
        }

        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            LOGGER.info("Extracting entities from artifact: " + artifact.getRowKey().toString());

            String artifactText = artifact.getContent().getDocExtractedTextString();
            Collection<TermMention> termMentions = entityExtractor.extract(artifact, artifactText);
            for (TermMention termMention : termMentions) {
                Concept concept = ontologyRepository.getConceptByName(getSession().getGraphSession(), termMention.getMetadata().getConcept());
                if (concept == null) {
                    throw new RuntimeException("Could not find concept: " + termMention.getMetadata().getConcept());
                }
                termMention.getMetadata().setConceptGraphVertexId(concept.getId());

                TermMention existingTermMention = termMentionRepository.findByRowKey(getSession().getModelSession(), termMention.getRowKey().toString());
                if (existingTermMention != null) {
                    existingTermMention.update(termMention);
                }

                termMentionRepository.save(getSession().getModelSession(), existingTermMention);
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

