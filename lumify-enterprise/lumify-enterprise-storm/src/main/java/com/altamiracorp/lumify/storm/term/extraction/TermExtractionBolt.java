package com.altamiracorp.lumify.storm.term.extraction;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.bigtable.model.FlushFlag;
import com.altamiracorp.lumify.core.bootstrap.InjectHelper;
import com.altamiracorp.lumify.core.ingest.term.extraction.*;
import com.altamiracorp.lumify.core.model.audit.AuditAction;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.LabelName;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.storm.BaseTextProcessingBolt;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.Visibility;
import com.google.common.collect.Lists;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;

import static com.altamiracorp.lumify.core.util.CollectionUtil.single;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class TermExtractionBolt extends BaseTextProcessingBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TermExtractionBolt.class);
    private TermExtractionProcess termExtractionStreamProcess;
    private List<TermResolutionWorker> termResolvers;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        try {
            List<TermExtractionWorker> extractors = loadWorkers(stormConf, TermExtractionWorker.class);
            termExtractionStreamProcess = new TermExtractionProcess("termExtractionBoltWorker", extractors);

            termResolvers = loadWorkers(stormConf, TermResolutionWorker.class);
        } catch (Exception ex) {
            collector.reportError(ex);
        }
    }

    private <T extends TermWorker> List<T> loadWorkers(final Map stormConf, final Class<T> clazz) throws Exception {
        List<T> workers = Lists.newArrayList();

        ServiceLoader<T> services = ServiceLoader.load(clazz);
        for (T service : services) {
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
        Vertex artifactGraphVertex = graph.getVertex(graphVertexId, getUser().getAuthorizations());
        runTextExtractions(artifactGraphVertex);
    }

    private void runTextExtractions(Vertex artifactGraphVertex) throws Exception {
        checkState(termExtractionStreamProcess != null, "termExtractionStreamProcess was not initialized");

        InputStream textIn = getInputStream(artifactGraphVertex);
        TermExtractionAdditionalWorkData termExtractionAdditionalWorkData = new TermExtractionAdditionalWorkData();
        termExtractionAdditionalWorkData.setVertex(artifactGraphVertex);

        // run all extraction processes and merge the results
        List<ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult>> termExtractionResults = termExtractionStreamProcess.doWork(textIn, termExtractionAdditionalWorkData);
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        mergeTextExtractedInfos(termExtractionResult, termExtractionResults);

        // run all resolution processes on the merged results
        for (TermResolutionWorker resolver : termResolvers) {
            termExtractionResult = resolver.resolveTerms(termExtractionResult);
        }

        List<TermMentionWithGraphVertex> termMentionsWithGraphVertices = saveTermExtractions(artifactGraphVertex, termExtractionResult);
        saveRelationships(termExtractionResult.getRelationships(), termMentionsWithGraphVertices);

        workQueueRepository.pushArtifactHighlight(artifactGraphVertex.getId().toString());
    }

    private void mergeTextExtractedInfos(TermExtractionResult termExtractionResult, List<ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult>> results) throws Exception {
        for (ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult> result : results) {
            if (result.getError() != null) {
                throw result.getError();
            }
            termExtractionResult.mergeFrom(result.getResult());
        }
    }

    private List<TermMentionWithGraphVertex> saveTermExtractions(Vertex artifactGraphVertex, TermExtractionResult termExtractionResult) {
        List<TermMention> termMentions = termExtractionResult.getTermMentions();
        List<TermMentionWithGraphVertex> results = new ArrayList<TermMentionWithGraphVertex>();
        for (TermMention termMention : termMentions) {
            LOGGER.debug("Saving term mention '%s':%s (%d:%d)", termMention.getSign(), termMention.getOntologyClassUri(), termMention.getStart(), termMention.getEnd());
            List<String> modifiedProperties = new ArrayList<String>();
            Vertex vertex = null;
            boolean newVertex = false;
            TermMentionModel termMentionModel = new TermMentionModel(new TermMentionRowKey(artifactGraphVertex.getId().toString(), termMention.getStart(), termMention.getEnd()));
            termMentionModel.getMetadata().setSign(termMention.getSign());
            termMentionModel.getMetadata().setOntologyClassUri(termMention.getOntologyClassUri());

            Concept concept = ontologyRepository.getConceptByName(termMention.getOntologyClassUri());
            if (concept != null) {
                termMentionModel.getMetadata().setConceptGraphVertexId(concept.getId().toString());
            } else {
                LOGGER.error("Could not find ontology graph vertex '%s'", termMention.getOntologyClassUri());
                continue;
            }

            if (termMention.isResolved()) {
                String title = termMention.getSign();
                Iterable<Vertex> vertexIterator = graph.query(getUser().getAuthorizations()).has(PropertyName.TITLE.toString(), title).vertices();
                if (!termMention.getUseExisting() || !vertexIterator.iterator().hasNext()) {
                    vertex = graph.addVertex(new Visibility(""), getUser().getAuthorizations());
                    newVertex = true;
                    title = termMention.getSign();
                    vertex.setProperty(PropertyName.TITLE.toString(), title, new Visibility(""));
                    modifiedProperties.add(PropertyName.TITLE.toString());
                    if (concept != null) {
                        vertex.setProperty(PropertyName.CONCEPT_TYPE.toString(), concept.getId(), new Visibility(""));
                        modifiedProperties.add(PropertyName.CONCEPT_TYPE.toString());
                    }
                } else {
                    vertex = single(vertexIterator);
                }

                if (termMention.getPropertyValue() != null) {
                    Map<String, Object> properties = termMention.getPropertyValue();
                    for (String key : properties.keySet()) {
                        vertex.setProperty(key, properties.get(key), new Visibility(""));
                        modifiedProperties.add(key);
                    }
                }

                // TODO get auditing working
//                if (newVertex) {
//                    auditRepository.auditEntity(AuditAction.CREATE.toString(), vertex.getId(), artifactGraphVertexId, title, concept.getId().toString(), termMention.getProcess(), "", getUser());
//                }

                for (String property : modifiedProperties) {
                    auditRepository.auditEntityProperties(AuditAction.UPDATE.toString(), vertex, property, termMention.getProcess(), "", getUser());
                }

                graph.addEdge(artifactGraphVertex, vertex, LabelName.RAW_HAS_ENTITY.toString(), new Visibility(""), getUser().getAuthorizations());

                String labelDisplayName = ontologyRepository.getDisplayNameForLabel(LabelName.RAW_HAS_ENTITY.toString());
                auditRepository.auditRelationships(AuditAction.CREATE.toString(), artifactGraphVertex, vertex, labelDisplayName, termMention.getProcess(), "", getUser());

                termMentionModel.getMetadata().setVertexId(vertex.getId().toString());
            }

            termMentionRepository.save(termMentionModel, FlushFlag.NO_FLUSH, getUser().getModelUserContext());
            results.add(new TermMentionWithGraphVertex(termMentionModel, vertex));
        }
        termMentionRepository.flush();
        return results;
    }

    private void saveRelationships(List<TermRelationship> relationships, List<TermMentionWithGraphVertex> termMentionsWithGraphVertices) {
        for (TermRelationship relationship : relationships) {
            TermMentionWithGraphVertex sourceTermMentionsWithGraphVertex = findTermMentionWithGraphVertex(termMentionsWithGraphVertices, relationship.getSourceTermMention());
            checkNotNull(sourceTermMentionsWithGraphVertex, "source was not found for " + relationship.getSourceTermMention());
            checkNotNull(sourceTermMentionsWithGraphVertex.getVertex(), "source vertex was not found for " + relationship.getSourceTermMention());
            TermMentionWithGraphVertex destTermMentionsWithGraphVertex = findTermMentionWithGraphVertex(termMentionsWithGraphVertices, relationship.getDestTermMention());
            checkNotNull(destTermMentionsWithGraphVertex, "dest was not found for " + relationship.getDestTermMention());
            checkNotNull(destTermMentionsWithGraphVertex.getVertex(), "dest vertex was not found for " + relationship.getDestTermMention());
            String label = relationship.getLabel();
            graph.addEdge(
                    sourceTermMentionsWithGraphVertex.getVertex(),
                    destTermMentionsWithGraphVertex.getVertex(),
                    label,
                    new Visibility(""),
                    getUser().getAuthorizations()
            );
        }
    }

    private TermMentionWithGraphVertex findTermMentionWithGraphVertex(List<TermMentionWithGraphVertex> termMentionsWithGraphVertices, TermMention termMention) {
        for (TermMentionWithGraphVertex termMentionsWithGraphVertex : termMentionsWithGraphVertices) {
            if (termMentionsWithGraphVertex.getTermMention().getRowKey().getStartOffset() == termMention.getStart()
                    && termMentionsWithGraphVertex.getTermMention().getRowKey().getEndOffset() == termMention.getEnd()
                    && termMentionsWithGraphVertex.getVertex() != null) {
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

    private static class TermExtractionProcess extends ThreadedInputStreamProcess<TermExtractionResult, TermExtractionAdditionalWorkData> {
        public TermExtractionProcess(final String threadNamePrefix,
                                     final Collection<? extends ThreadedTeeInputStreamWorker<TermExtractionResult, TermExtractionAdditionalWorkData>>
                                             workersCollection) {
            super(threadNamePrefix, workersCollection);
        }
    }
}
