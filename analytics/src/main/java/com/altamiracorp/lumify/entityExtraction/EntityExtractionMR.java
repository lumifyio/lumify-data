package com.altamiracorp.lumify.entityExtraction;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.ConfigurableMapJobBase;
import com.altamiracorp.lumify.LumifyMapper;
import com.altamiracorp.lumify.config.Configuration;
import com.altamiracorp.lumify.model.AccumuloModelOutputFormat;
import com.altamiracorp.lumify.model.Row;
import com.altamiracorp.lumify.model.graph.GraphRepository;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.Concept;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class EntityExtractionMR extends ConfigurableMapJobBase {
    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        EntityExtractorMapper.init(job, clazz);
        return EntityExtractorMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        Configuration c = getConfiguration();
        AccumuloArtifactInputFormat.init(job, c.getDataStoreUserName(), c.getDataStorePassword(), getAuthorizations(), c.getZookeeperInstanceName(), c.getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    public static class EntityExtractorMapper extends LumifyMapper<Text, Artifact, Text, Row> {
        /**
         * The time period (in milliseconds) that progress should be reported to the framework
         */
        private static final int PROGRESS_REPORT_PERIOD_MS = 5000;

        private static final Logger LOGGER = LoggerFactory.getLogger(EntityExtractorMapper.class);
        public static final String CONF_ENTITY_EXTRACTOR_CLASS = "entityExtractorClass";

        private EntityExtractor entityExtractor;
        private TermMentionRepository termMentionRepository;
        private OntologyRepository ontologyRepository;
        private GraphRepository graphRepository;
        private final HashMap<String, Concept> conceptMap = new HashMap<String, Concept>();
        private final ExecutorService executorService = Executors.newFixedThreadPool(1);



        @Override
        protected void setup(Context context, Injector injector) throws IOException, IllegalAccessException, InstantiationException {
            entityExtractor = getAndInjectClassFromConfiguration(context, injector, CONF_ENTITY_EXTRACTOR_CLASS);
            entityExtractor.setup(context, getUser());
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            super.cleanup(context);

            if( executorService != null ) {
                executorService.shutdownNow();
            }
        }

        @Override
        public void safeMap(Text rowKey, Artifact artifact, Context context) throws Exception {
            throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//            if (artifact.getGenericMetadata().getMappingJson() != null) {
//                LOGGER.info("Skipping extracting entities from artifact: " + artifact.getRowKey().toString() + " (cause: structured data)");
//                return;
//            }
//
//            LOGGER.info("Extracting entities from artifact: " + artifact.getRowKey().toString());
//
//            String artifactText = artifact.getContent().getDocExtractedTextString();
//            if (artifactText == null) {
//                return;
//            }
//
//            final List<ExtractedEntity> extractedEntities = extractEntities(artifact, artifactText, context);
//
//            LOGGER.info("Processing extracted entities");
//            for (ExtractedEntity extractedEntity : extractedEntities) {
//                TermMention termMention = extractedEntity.getTermMention();
//
//                Concept concept = getConcept(termMention);
//                termMention.getMetadata().setConceptGraphVertexId(concept.getId());
//
//                TermMention existingTermMention = termMentionRepository.findByRowKey(termMention.getRowKey().toString(), getUser());
//                if (existingTermMention != null) {
//                    existingTermMention.update(termMention);
//                } else {
//                    existingTermMention = termMention;
//                }
//
//                if (extractedEntity.getGraphVertex() != null) {
//                    if (existingTermMention.getMetadata().getGraphVertexId() != null) {
//                        if (LOGGER.isDebugEnabled()) {
//                            LOGGER.debug("Skipping resolve entity, term already resolved: " + existingTermMention.getRowKey().toString() + " to " + existingTermMention.getMetadata().getGraphVertexId());
//                        }
//                    } else {
//                        String graphVertexId = createEntityGraphVertex(artifact, extractedEntity, concept);
//                        if (graphVertexId != null) {
//                            existingTermMention.getMetadata().setGraphVertexId(graphVertexId);
//                        }
//                    }
//                }
//
//                termMentionRepository.save(existingTermMention, getUser());
//            }
//            LOGGER.info("Processing complete");
        }

        private List<ExtractedEntity> extractEntities(final Artifact artifact, final String artifactText, final Context context) throws Exception {
            final Callable<List<ExtractedEntity>> callable = new EntityExtractorCallable(entityExtractor, artifact, artifactText);
            final Future<List<ExtractedEntity>> future = executorService.submit(callable);

            final long startTime = System.currentTimeMillis();

            // Report progress to the framework while waiting for extraction to be completed
            while(!future.isDone()) {
                LOGGER.debug("Reporting progress to the framework");
                context.progress();

                // Report progress periodically
                Thread.sleep(PROGRESS_REPORT_PERIOD_MS);
            }

            final List<ExtractedEntity> extractedEntities = future.get();
            LOGGER.info(String.format("Extraction ran for %d seconds", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)));
            LOGGER.info("Number of entities extracted: " + extractedEntities.size());

            return extractedEntities;
        }

        private String createEntityGraphVertex(Artifact artifact, ExtractedEntity extractedEntity, Concept concept) {
            throw new RuntimeException("storm refactor - not implemented"); // TODO storm refactor
//            GraphVertex graphVertex = extractedEntity.getGraphVertex();
//
//            graphVertex.setProperty(PropertyName.SUBTYPE.toString(), concept.getId());
//            graphVertex.setType(VertexType.ENTITY);
//
//            GraphVertex existingGraphVertex = graphRepository.findVertexByTitleAndType((String) graphVertex.getProperty(PropertyName.TITLE), VertexType.ENTITY, getUser());
//            if (existingGraphVertex != null) {
//                existingGraphVertex.update(graphVertex);
//                graphVertex.update(existingGraphVertex);
//                graphVertex = existingGraphVertex;
//            }
//
//            graphRepository.saveVertex(graphVertex, getUser());
//            graphRepository.commit();
//
//            graphRepository.saveRelationship(artifact.getGenericMetadata().getGraphVertexId(), graphVertex.getId(), LabelName.HAS_ENTITY, getUser());
//            graphRepository.commit();
//
//            return graphVertex.getId();
        }

        private Concept getConcept(TermMention termMention) {
            String conceptLabel = termMention.getMetadata().getConcept();
            Concept concept = conceptMap.get(conceptLabel);
            if (concept == null) {
                concept = ontologyRepository.getConceptByName(conceptLabel, getUser());
                if (concept == null) {
                    throw new RuntimeException("Could not find concept: " + conceptLabel);
                }
                conceptMap.put(conceptLabel, concept);
            }
            return concept;
        }

        public static void init(Job job, Class<? extends EntityExtractor> entityExtractor) {
            job.getConfiguration().setClass(CONF_ENTITY_EXTRACTOR_CLASS, entityExtractor, EntityExtractor.class);
        }

        @Inject
        public void setTermMentionRepository(TermMentionRepository termMentionRepository) {
            this.termMentionRepository = termMentionRepository;
        }

        @Inject
        public void setOntologyRepository(OntologyRepository ontologyRepository) {
            this.ontologyRepository = ontologyRepository;
        }

        @Inject
        public void setGraphRepository(GraphRepository graphRepository) {
            this.graphRepository = graphRepository;
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new EntityExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}

