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
import com.altamiracorp.lumify.core.model.termMention.TermMentionModel;
import com.altamiracorp.lumify.core.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.core.security.LumifyVisibility;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.core.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.storm.BaseTextProcessingBolt;
import com.altamiracorp.securegraph.Direction;
import com.altamiracorp.securegraph.Edge;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.mutation.ElementMutation;
import com.altamiracorp.securegraph.mutation.ExistingElementMutation;
import com.google.common.collect.Lists;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;

import static com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties.CONCEPT_TYPE;
import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.TITLE;
import static com.altamiracorp.lumify.core.util.CollectionUtil.trySingle;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class TermExtractionBolt extends BaseTextProcessingBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(TermExtractionBolt.class);
    private TermExtractionProcess termExtractionStreamProcess;
    private List<TermResolutionWorker> termResolvers;
    private LumifyVisibility lumifyVisibility;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        lumifyVisibility = new LumifyVisibility();
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
        Vertex artifactGraphVertex = graph.getVertex(graphVertexId, getAuthorizations());
        runTextExtractions(artifactGraphVertex);
    }

    private void runTextExtractions(Vertex artifactGraphVertex) throws Exception {
        checkState(termExtractionStreamProcess != null, "termExtractionStreamProcess was not initialized");

        InputStream textIn = getInputStream(artifactGraphVertex);
        TermExtractionAdditionalWorkData termExtractionAdditionalWorkData = new TermExtractionAdditionalWorkData();
        termExtractionAdditionalWorkData.setVertex(artifactGraphVertex);

        // run all extraction processes and merge the results
        List<ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult>> termExtractionResults =
                termExtractionStreamProcess.doWork(textIn, termExtractionAdditionalWorkData);
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        mergeTextExtractedInfos(termExtractionResult, termExtractionResults);

        // run all resolution processes on the merged results
        for (TermResolutionWorker resolver : termResolvers) {
            termExtractionResult = resolver.resolveTerms(termExtractionResult);
        }

        List<TermMentionWithGraphVertex> termMentionsWithGraphVertices = saveTermExtractions(artifactGraphVertex, termExtractionResult);
        saveRelationships(termExtractionResult.getRelationships(), termMentionsWithGraphVertices);
    }

    private void mergeTextExtractedInfos(TermExtractionResult termExtractionResult,
                                         List<ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult>> results) throws Exception {
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
            Vertex vertex = null;
            TermMentionModel termMentionModel = new TermMentionModel(new TermMentionRowKey(artifactGraphVertex.getId().toString(), termMention.getStart(), termMention.getEnd()));
            termMentionModel.getMetadata().setSign(termMention.getSign(), lumifyVisibility.getVisibility());
            termMentionModel.getMetadata().setOntologyClassUri(termMention.getOntologyClassUri(), lumifyVisibility.getVisibility());
            if (termMention.getProcess() != null && !termMention.getProcess().equals("")) {
                termMentionModel.getMetadata().setAnalyticProcess(termMention.getProcess(), lumifyVisibility.getVisibility());
            }

            Concept concept = ontologyRepository.getConceptById(termMention.getOntologyClassUri());
            if (concept == null) {
                LOGGER.error("Could not find ontology graph vertex '%s'", termMention.getOntologyClassUri());
                continue;
            }
            termMentionModel.getMetadata().setConceptGraphVertexId(concept.getId(), lumifyVisibility.getVisibility());

            if (termMention.isResolved()) {
                String title = termMention.getSign();
                ElementMutation<Vertex> vertexElementMutation;
                if (termMention.getUseExisting()) {
                    if (termMention.getId() != null) {
                        vertex = graph.getVertex(termMention.getId(), getAuthorizations());
                    } else {
                        vertex = trySingle(graph.query(getAuthorizations())
                                .has(TITLE.getKey(), title)
                                .has(CONCEPT_TYPE.getKey(), concept.getId())
                                .vertices());
                    }
                }
                if (vertex == null) {
                    if (termMention.getId() != null) {
                        vertexElementMutation = graph.prepareVertex(termMention.getId(), lumifyVisibility.getVisibility(), getAuthorizations());
                    } else {
                        vertexElementMutation = graph.prepareVertex(lumifyVisibility.getVisibility(), getAuthorizations());
                    }
                    TITLE.setProperty(vertexElementMutation, title, lumifyVisibility.getVisibility());
                    CONCEPT_TYPE.setProperty(vertexElementMutation, concept.getId(), lumifyVisibility.getVisibility());
                } else {
                    vertexElementMutation = vertex.prepareMutation();
                }

                if (termMention.getPropertyValue() != null) {
                    Map<String, Object> properties = termMention.getPropertyValue();
                    for (String key : properties.keySet()) {
                        // TODO should we wrap these properties in secure graph Text classes?
                        // GS - No.  Leave it up to the property generator to provide Text objects if they
                        // want index control; see CLAVIN for example
                        vertexElementMutation.setProperty(key, properties.get(key), lumifyVisibility.getVisibility());
                    }
                }

                if (!(vertexElementMutation instanceof ExistingElementMutation)) {
                    vertex = vertexElementMutation.save();
                    auditRepository.auditVertexElementMutation(AuditAction.UPDATE, vertexElementMutation, vertex, termMention.getProcess(), getUser(), lumifyVisibility.getVisibility());
                } else {
                    auditRepository.auditVertexElementMutation(AuditAction.UPDATE, vertexElementMutation, vertex, termMention.getProcess(), getUser(), lumifyVisibility.getVisibility());
                    vertex = vertexElementMutation.save();
                }

                // TODO: a better way to check if the same edge exists instead of looking it up every time?
                Edge edge = trySingle(artifactGraphVertex.getEdges(vertex, Direction.OUT, LabelName.RAW_HAS_ENTITY.toString(), getAuthorizations()));
                if (edge == null) {
                    edge = graph.addEdge(artifactGraphVertex, vertex, LabelName.RAW_HAS_ENTITY.toString(), lumifyVisibility.getVisibility(), getAuthorizations());
                    auditRepository.auditRelationship(AuditAction.CREATE, artifactGraphVertex, vertex, edge, termMention.getProcess(), "", getUser(), lumifyVisibility.getVisibility());
                }

                termMentionModel.getMetadata().setVertexId(vertex.getId().toString(), lumifyVisibility.getVisibility());
            }

            termMentionRepository.save(termMentionModel, FlushFlag.NO_FLUSH);
            results.add(new TermMentionWithGraphVertex(termMentionModel, vertex));
            graph.flush();
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

            // TODO: a better way to check if the same edge exists instead of looking it up every time?
            Edge edge = trySingle(sourceTermMentionsWithGraphVertex.getVertex().getEdges(destTermMentionsWithGraphVertex.getVertex(), Direction.OUT, label, getAuthorizations()));
            if (edge == null) {
                graph.addEdge(
                        sourceTermMentionsWithGraphVertex.getVertex(),
                        destTermMentionsWithGraphVertex.getVertex(),
                        label,
                        lumifyVisibility.getVisibility(),
                        getAuthorizations()
                );
            }
        }
        graph.flush();
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
