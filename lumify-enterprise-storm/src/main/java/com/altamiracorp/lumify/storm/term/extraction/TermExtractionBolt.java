package com.altamiracorp.lumify.storm.term.extraction;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.InjectHelper;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.core.model.ontology.*;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.storm.BaseTextProcessingBolt;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class TermExtractionBolt extends BaseTextProcessingBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TermExtractionBolt.class);
    private ThreadedInputStreamProcess<TermExtractionResult, TermExtractionAdditionalWorkData> termExtractionStreamProcess;
    private TermMentionRepository termMentionRepository;
    private OntologyRepository ontologyRepository;
    private WorkQueueRepository workQueueRepository;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        try {
            List<ThreadedTeeInputStreamWorker<TermExtractionResult, TermExtractionAdditionalWorkData>> workers = loadWorkers(stormConf);
            termExtractionStreamProcess = new ThreadedInputStreamProcess<TermExtractionResult, TermExtractionAdditionalWorkData>("termExtractionBoltWorker", workers);
        } catch (Exception ex) {
            collector.reportError(ex);
        }
    }

    private List<ThreadedTeeInputStreamWorker<TermExtractionResult, TermExtractionAdditionalWorkData>> loadWorkers(Map stormConf) throws Exception {
        List<ThreadedTeeInputStreamWorker<TermExtractionResult, TermExtractionAdditionalWorkData>> workers = Lists.newArrayList();

        ServiceLoader<TermExtractionWorker> services = ServiceLoader.load(TermExtractionWorker.class);
        for (TermExtractionWorker service : services) {
            LOGGER.info("Adding service %s to %s", service.getClass().getName(), getClass().getName());
            InjectHelper.inject(service);
            service.prepare(stormConf, getUser());
            workers.add(service);
        }

        return workers;
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String graphVertexId = json.getString("graphVertexId");
        GraphVertex artifactGraphVertex = graphRepository.findVertex(graphVertexId, getUser());
        runTextExtractions(artifactGraphVertex);
    }

    private void runTextExtractions(GraphVertex artifactGraphVertex) throws Exception {
        checkState(termExtractionStreamProcess != null, "termExtractionStreamProcess was not initialized");

        InputStream textIn = getInputStream(artifactGraphVertex);
        TermExtractionAdditionalWorkData termExtractionAdditionalWorkData = new TermExtractionAdditionalWorkData();
        termExtractionAdditionalWorkData.setGraphVertex(artifactGraphVertex);

        List<ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult>> termExtractionResults = termExtractionStreamProcess.doWork(textIn, termExtractionAdditionalWorkData);
        TermExtractionResult termExtractionResult = new TermExtractionResult();

        mergeTextExtractedInfos(termExtractionResult, termExtractionResults);
        List<TermMentionWithGraphVertex> termMentionsWithGraphVertices = saveTermExtractions(artifactGraphVertex.getId(), termExtractionResult);
        saveRelationships(termExtractionResult.getRelationships(), termMentionsWithGraphVertices);

        workQueueRepository.pushArtifactHighlight(artifactGraphVertex.getId());
    }

    private void mergeTextExtractedInfos(TermExtractionResult termExtractionResult, List<ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult>> results) throws Exception {
        for (ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult> result : results) {
            if (result.getError() != null) {
                throw result.getError();
            }
            termExtractionResult.mergeFrom(result.getResult());
        }
    }

    private List<TermMentionWithGraphVertex> saveTermExtractions(String artifactGraphVertexId, TermExtractionResult termExtractionResult) {
        List<TermExtractionResult.TermMention> termMentions = termExtractionResult.getTermMentions();
        List<TermMentionWithGraphVertex> results = new ArrayList<TermMentionWithGraphVertex>();
        GraphVertex artifactVertex = graphRepository.findVertex(artifactGraphVertexId, getUser());
        for (TermExtractionResult.TermMention termMention : termMentions) {
            LOGGER.debug("Saving term mention '%s':%s (%d:%d)", termMention.getSign(), termMention.getOntologyClassUri(), termMention.getStart(), termMention.getEnd());
            List<String> modifiedProperties = new ArrayList<String>();
            GraphVertex vertex = null;
            boolean newVertex = false;
            TermMention termMentionModel = new TermMention(new TermMentionRowKey(artifactGraphVertexId, termMention.getStart(), termMention.getEnd()));
            termMentionModel.getMetadata().setSign(termMention.getSign());
            termMentionModel.getMetadata().setOntologyClassUri(termMention.getOntologyClassUri());

            Concept concept = ontologyRepository.getConceptByName(termMention.getOntologyClassUri(), getUser());
            if (concept != null) {
                termMentionModel.getMetadata().setConceptGraphVertexId(concept.getId());
            } else {
                LOGGER.error("Could not find ontology graph vertex '%s'", termMention.getOntologyClassUri());
            }

            if (termMention.isResolved()) {
                String title = termMention.getSign();
                vertex = graphRepository.findVertexByExactTitle(title, getUser());
                if (!termMention.getUseExisting() || vertex == null) {
                    vertex = new InMemoryGraphVertex();
                    newVertex = true;
                    title = termMention.getSign();
                    vertex.setProperty(PropertyName.TITLE, title);
                    modifiedProperties.add(PropertyName.TITLE.toString());
                    if (concept != null) {
                        vertex.setProperty(PropertyName.CONCEPT_TYPE.toString(), concept.getId());
                        modifiedProperties.add(PropertyName.CONCEPT_TYPE.toString());
                    }
                }

                if (termMention.getPropertyValue() != null) {
                    Map<String, Object> properties = termMention.getPropertyValue();
                    for (String key : properties.keySet()) {
                        vertex.setProperty(key, properties.get(key));
                        modifiedProperties.add(key);
                    }
                }

                String resolvedEntityGraphVertexId = graphRepository.saveVertex(vertex, getUser());

                if (newVertex) {
                    auditRepository.auditEntity(AuditAction.CREATE.toString(), vertex.getId(), artifactGraphVertexId, title, concept.getId(), termMention.getProcess(), "", getUser());
                }

                for (String property : modifiedProperties) {
                    auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), vertex, property, termMention.getProcess(), "", getUser());
                }

                graphRepository.saveRelationship(artifactGraphVertexId, resolvedEntityGraphVertexId, LabelName.HAS_ENTITY, getUser());

                String labelDisplayName = ontologyRepository.getDisplayNameForLabel(LabelName.HAS_ENTITY.toString(), getUser());
                auditRepository.auditRelationships(AuditAction.CREATE.toString(), artifactVertex, vertex, labelDisplayName, termMention.getProcess(), "", getUser());

                termMentionModel.getMetadata().setGraphVertexId(resolvedEntityGraphVertexId);
            }

            termMentionRepository.save(termMentionModel, getUser().getModelUserContext());
            results.add(new TermMentionWithGraphVertex(termMentionModel, vertex));
        }
        return results;
    }

    private void saveRelationships(List<TermExtractionResult.Relationship> relationships, List<TermMentionWithGraphVertex> termMentionsWithGraphVertices) {
        for (TermExtractionResult.Relationship relationship : relationships) {
            TermMentionWithGraphVertex sourceTermMentionsWithGraphVertex = findTermMentionWithGraphVertex(termMentionsWithGraphVertices, relationship.getSourceTermMention());
            checkNotNull(sourceTermMentionsWithGraphVertex, "source was not found for " + relationship.getSourceTermMention());
            checkNotNull(sourceTermMentionsWithGraphVertex.getGraphVertex(), "source vertex was not found for " + relationship.getSourceTermMention());
            TermMentionWithGraphVertex destTermMentionsWithGraphVertex = findTermMentionWithGraphVertex(termMentionsWithGraphVertices, relationship.getDestTermMention());
            checkNotNull(destTermMentionsWithGraphVertex, "dest was not found for " + relationship.getDestTermMention());
            checkNotNull(destTermMentionsWithGraphVertex.getGraphVertex(), "dest vertex was not found for " + relationship.getDestTermMention());
            String label = relationship.getLabel();
            graphRepository.saveRelationship(
                    sourceTermMentionsWithGraphVertex.getGraphVertex().getId(),
                    destTermMentionsWithGraphVertex.getGraphVertex().getId(),
                    label,
                    getUser()
            );
        }
    }

    private TermMentionWithGraphVertex findTermMentionWithGraphVertex(List<TermMentionWithGraphVertex> termMentionsWithGraphVertices, TermExtractionResult.TermMention termMention) {
        for (TermMentionWithGraphVertex termMentionsWithGraphVertex : termMentionsWithGraphVertices) {
            if (termMentionsWithGraphVertex.getTermMention().getRowKey().getStartOffset() == termMention.getStart()
                    && termMentionsWithGraphVertex.getTermMention().getRowKey().getEndOffset() == termMention.getEnd()
                    && termMentionsWithGraphVertex.getGraphVertex() != null) {
                return termMentionsWithGraphVertex;
            }
        }
        return null;
    }

    @Override
    public void cleanup() {
        termExtractionStreamProcess.stop();
        super.cleanup();
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
    public void setWorkQueueRepository(WorkQueueRepository workQueueRepository) {
        this.workQueueRepository = workQueueRepository;
    }
}
