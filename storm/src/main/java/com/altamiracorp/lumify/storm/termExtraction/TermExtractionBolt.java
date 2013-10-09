package com.altamiracorp.lumify.storm.termExtraction;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.ingest.termExtraction.TextExtractedAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.termExtraction.TextExtractedInfo;
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
import com.google.inject.Inject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import static com.google.common.base.Preconditions.checkNotNull;

public class TermExtractionBolt extends BaseTextProcessingBolt {
    private static final Logger LOGGER = LoggerFactory.getLogger(TermExtractionBolt.class);
    private ThreadedInputStreamProcess<TextExtractedInfo, TextExtractedAdditionalWorkData> textExtractionStreamProcess;
    private TermMentionRepository termMentionRepository;
    private OntologyRepository ontologyRepository;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        try {
            List<ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData>>();

            ServiceLoader<TermExtractionWorker> services = ServiceLoader.load(TermExtractionWorker.class);
            for (TermExtractionWorker service : services) {
                LOGGER.info("adding class " + service.getClass().getName() + " to " + getClass().getName());
                inject(service);
            }
            for (TermExtractionWorker service : services) {
                service.prepare(stormConf, getUser());
            }
            for (TermExtractionWorker service : services) {
                workers.add(service);
            }

            textExtractionStreamProcess = new ThreadedInputStreamProcess<TextExtractedInfo, TextExtractedAdditionalWorkData>("textBoltWorkers", workers);
        } catch (Exception ex) {
            collector.reportError(ex);
        }
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String graphVertexId = json.getString("graphVertexId");

        GraphVertex artifactGraphVertex = graphRepository.findVertex(graphVertexId, getUser());
        runTextExtractions(artifactGraphVertex);

        LOGGER.info("emitting value (" + getClass().getName() + "): " + json.toString());
        getCollector().emit(new Values(json.toString()));
        getCollector().ack(input);
    }

    private void runTextExtractions(GraphVertex artifactGraphVertex) throws Exception {
        checkNotNull(textExtractionStreamProcess, "textExtractionStreamProcess was not initialized");

        InputStream textIn = getInputStream(artifactGraphVertex);
        TextExtractedAdditionalWorkData textExtractedAdditionalWorkData = new TextExtractedAdditionalWorkData();
        textExtractedAdditionalWorkData.setGraphVertex(artifactGraphVertex);
        List<ThreadedTeeInputStreamWorker.WorkResult<TextExtractedInfo>> results = textExtractionStreamProcess.doWork(textIn, textExtractedAdditionalWorkData);
        TextExtractedInfo textExtractedInfo = new TextExtractedInfo();
        mergeTextExtractedInfos(textExtractedInfo, results);
        saveTextExtractedInfos(artifactGraphVertex.getId(), textExtractedInfo);
    }

    private void saveTextExtractedInfos(String artifactGraphVertexId, TextExtractedInfo textExtractedInfo) {
        saveTermMentions(artifactGraphVertexId, textExtractedInfo.getTermMentions());
    }

    private void saveTermMentions(String artifactGraphVertexId, List<TextExtractedInfo.TermMention> termMentions) {
        for (TextExtractedInfo.TermMention termMention : termMentions) {
            LOGGER.info("saving term mention \"" + termMention.getSign() + "\" (" + termMention.getStart() + ":" + termMention.getEnd() + ") " + termMention.getOntologyClassUri());
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
                GraphVertex vertex = graphRepository.findVertexByTitleAndType(termMention.getSign(), VertexType.ENTITY, getUser());
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

            JSONObject termJson = new JSONObject();
            termJson.put("rowKey", termMentionModel.getRowKey().toString());
            pushOnQueue("term", termJson);

            termMentionRepository.save(termMentionModel, getUser());
        }
    }

    private void mergeTextExtractedInfos(TextExtractedInfo textExtractedInfo, List<ThreadedTeeInputStreamWorker.WorkResult<TextExtractedInfo>> results) throws Exception {
        for (ThreadedTeeInputStreamWorker.WorkResult<TextExtractedInfo> result : results) {
            if (result.getError() != null) {
                throw result.getError();
            }
            textExtractedInfo.mergeFrom(result.getResult());
        }
    }

    @Override
    public void cleanup() {
        this.textExtractionStreamProcess.stop();
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
