package com.altamiracorp.lumify.storm.term.extraction;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.hadoop.thirdparty.guava.common.collect.Lists;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.model.ontology.VertexType;
import com.altamiracorp.lumify.core.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.Concept;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.storm.BaseTextProcessingBolt;
import com.altamiracorp.lumify.storm.term.analysis.LocationTermAnalyzer;
import com.google.inject.Inject;

public class TermExtractionBolt extends BaseTextProcessingBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TermExtractionBolt.class);
    private ThreadedInputStreamProcess<TermExtractionResult, TermExtractionAdditionalWorkData> termExtractionStreamProcess;
    private TermMentionRepository termMentionRepository;
    private OntologyRepository ontologyRepository;

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
            LOGGER.info(String.format("Adding service %s to %s", service.getClass().getName(), getClass().getName()));
            inject(service);
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

        LOGGER.info(String.format("Emitting value (%s): %s", getClass().getSimpleName(), json));
        getCollector().emit(new Values(json.toString()));
        getCollector().ack(input);
    }

    private void runTextExtractions(GraphVertex artifactGraphVertex) throws Exception {
        checkNotNull(termExtractionStreamProcess, "termExtractionStreamProcess was not initialized");

        InputStream textIn = getInputStream(artifactGraphVertex);
        TermExtractionAdditionalWorkData termExtractionAdditionalWorkData = new TermExtractionAdditionalWorkData();
        termExtractionAdditionalWorkData.setGraphVertex(artifactGraphVertex);

        List<ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult>> termExtractionResults = termExtractionStreamProcess.doWork(textIn, termExtractionAdditionalWorkData);
        TermExtractionResult termExtractionResult = new TermExtractionResult();

        mergeTextExtractedInfos(termExtractionResult, termExtractionResults);
        List<TermMentionWithGraphVertex> termMentions = saveTermExtractions(artifactGraphVertex.getId(), termExtractionResult.getTermMentions());
        processTermMentions(termMentions);

        JSONObject artifactHighlightJson = new JSONObject();
        artifactHighlightJson.put("graphVertexId", artifactGraphVertex.getId());
        pushOnQueue("artifactHighlight", artifactHighlightJson);
    }

    private void mergeTextExtractedInfos(TermExtractionResult termExtractionResult, List<ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult>> results) throws Exception {
        for (ThreadedTeeInputStreamWorker.WorkResult<TermExtractionResult> result : results) {
            if (result.getError() != null) {
                throw result.getError();
            }
            termExtractionResult.mergeFrom(result.getResult());
        }
    }

    private List<TermMentionWithGraphVertex> saveTermExtractions(String artifactGraphVertexId, List<TermExtractionResult.TermMention> termMentions) {
        List<TermMentionWithGraphVertex> results = new ArrayList<TermMentionWithGraphVertex>();
        for (TermExtractionResult.TermMention termMention : termMentions) {
            LOGGER.info("saving term mention \"" + termMention.getSign() + "\" (" + termMention.getStart() + ":" + termMention.getEnd() + ") " + termMention.getOntologyClassUri());
            GraphVertex vertex = null;
            TermMention termMentionModel = new TermMention(new TermMentionRowKey(artifactGraphVertexId, termMention.getStart(), termMention.getEnd()));
            termMentionModel.getMetadata().setSign(termMention.getSign());
            termMentionModel.getMetadata().setOntologyClassUri(termMention.getOntologyClassUri());

            Concept concept = ontologyRepository.getConceptByName(termMention.getOntologyClassUri(), getUser());
            if (concept != null) {
                termMentionModel.getMetadata().setConceptGraphVertexId(concept.getId());
            } else {
                LOGGER.error("Could not find ontology graph vertex \"" + termMention.getOntologyClassUri() + "\"");
            }

            if (termMention.isResolved()) {
                vertex = graphRepository.findVertexByTitleAndType(termMention.getSign(), VertexType.ENTITY, getUser());
                if (vertex == null) {
                    vertex = new InMemoryGraphVertex();
                    vertex.setProperty(PropertyName.TITLE, termMention.getSign());
                    vertex.setProperty(PropertyName.SUBTYPE.toString(), concept.getId());
                    vertex.setType(VertexType.ENTITY);
                }

                String resolvedEntityGraphVertexId = graphRepository.saveVertex(vertex, getUser());
                graphRepository.commit();

                graphRepository.saveRelationship(artifactGraphVertexId, resolvedEntityGraphVertexId, LabelName.HAS_ENTITY, getUser());
                graphRepository.commit();

                termMentionModel.getMetadata().setGraphVertexId(resolvedEntityGraphVertexId);
            }

            termMentionRepository.save(termMentionModel, getUser());
            results.add(new TermMentionWithGraphVertex(termMentionModel, vertex));
        }
        return results;
    }

    private void processTermMentions(List<TermMentionWithGraphVertex> termMentions) {
        final LocationTermAnalyzer locationAnalyzer = getInjector().getInstance(LocationTermAnalyzer.class);

        for(final TermMentionWithGraphVertex mention : termMentions) {
            locationAnalyzer.analyzeTermData(mention, getUser());
        }
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

}
