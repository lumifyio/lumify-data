package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.OpenNlpMaximumEntropyEntityExtractor;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.Concept;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.search.SearchProvider;
import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import com.altamiracorp.lumify.model.termMention.TermMentionRowKey;
import com.altamiracorp.lumify.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.apache.hadoop.conf.Configuration;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TextExtractionBolt extends BaseTextProcessingBolt {
    private ThreadedInputStreamProcess<TextExtractedInfo, TextExtractedAdditionalWorkData> textExtractionStreamProcess;
    private SearchProvider searchProvider;
    private TermMentionRepository termMentionRepository;
    private OntologyRepository ontologyRepository;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        try {
            Configuration configuration = createHadoopConfiguration(stormConf);

            List<ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData>>();
            workers.add(new SearchWorker());
            workers.add(new OpenNlpMeEntityExtractor(configuration, getUser()));
            textExtractionStreamProcess = new ThreadedInputStreamProcess<TextExtractedInfo, TextExtractedAdditionalWorkData>("textBoltWorkers", workers);
        } catch (Exception ex) {
            collector.reportError(ex);
        }
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        String jsonString = input.getString(0);
        JSONObject json = new JSONObject(jsonString);
        String graphVertexId = json.getString("graphVertexId");

        GraphVertex graphVertex = graphRepository.findVertex(graphVertexId, getUser());
        runTextExtractions(graphVertex);

        getCollector().emit(new Values(jsonString));
        getCollector().ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("json"));
    }

    private void runTextExtractions(GraphVertex graphVertex) throws Exception {
        InputStream textIn = getInputStream(graphVertex);
        TextExtractedAdditionalWorkData textExtractedAdditionalWorkData = new TextExtractedAdditionalWorkData();
        textExtractedAdditionalWorkData.setGraphVertex(graphVertex);
        List<ThreadedTeeInputStreamWorker.WorkResult<TextExtractedInfo>> results = textExtractionStreamProcess.doWork(textIn, textExtractedAdditionalWorkData);
        TextExtractedInfo textExtractedInfo = new TextExtractedInfo();
        mergeTextExtractedInfos(textExtractedInfo, results);
        saveTextExtractedInfos(graphVertex.getId(), textExtractedInfo);
    }

    private void saveTextExtractedInfos(String graphVertexId, TextExtractedInfo textExtractedInfo) {
        saveTermMentions(graphVertexId, textExtractedInfo.getTermMentions());
    }

    private void saveTermMentions(String graphVertexId, List<TextExtractedInfo.TermMention> termMentions) {
        for (TextExtractedInfo.TermMention termMention : termMentions) {
            TermMention termMentionModel = new TermMention(new TermMentionRowKey(graphVertexId, termMention.getStart(), termMention.getEnd()));
            termMentionModel.getMetadata().setSign(termMention.getSign());
            termMentionModel.getMetadata().setOntologyClassUri(termMention.getOntologyClassUri());

            Concept concept = ontologyRepository.getConceptByName(termMention.getOntologyClassUri(), getUser());
            if (concept != null) {
                termMentionModel.getMetadata().setConceptGraphVertexId(concept.getId());
            }

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
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @Inject
    public void setTermMentionRepository(TermMentionRepository termMentionRepository) {
        this.termMentionRepository = termMentionRepository;
    }

    @Inject
    public void setOntologyRepository(OntologyRepository ontologyRepository) {
        this.ontologyRepository = ontologyRepository;
    }

    private class TextExtractedAdditionalWorkData {

        private GraphVertex graphVertex;

        public void setGraphVertex(GraphVertex graphVertex) {
            this.graphVertex = graphVertex;
        }

        public GraphVertex getGraphVertex() {
            return graphVertex;
        }
    }

    private class SearchWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
        @Override
        protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
            TextExtractedInfo textExtractedInfo = new TextExtractedInfo();
            searchProvider.add(textExtractedAdditionalWorkData.getGraphVertex(), work);
            return textExtractedInfo;
        }
    }

    private class OpenNlpMeEntityExtractor extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
        private OpenNlpMaximumEntropyEntityExtractor openNlpMaximumEntropyEntityExtractor;

        public OpenNlpMeEntityExtractor(Configuration configuration, User user) throws IOException {
            openNlpMaximumEntropyEntityExtractor = new OpenNlpMaximumEntropyEntityExtractor(configuration, user);
        }

        @Override
        protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
            return openNlpMaximumEntropyEntityExtractor.extract(work);
        }
    }
}
