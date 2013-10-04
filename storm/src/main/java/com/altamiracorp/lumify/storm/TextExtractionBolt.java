package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.altamiracorp.lumify.config.ConfigurationHelper;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.entityExtraction.KnownEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.OpenNlpDictionaryEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.OpenNlpMaximumEntropyEntityExtractor;
import com.altamiracorp.lumify.entityExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.*;
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

import static com.google.common.base.Preconditions.checkNotNull;

public class TextExtractionBolt extends BaseTextProcessingBolt {
    private ThreadedInputStreamProcess<TextExtractedInfo, TextExtractedAdditionalWorkData> textExtractionStreamProcess;
    private SearchProvider searchProvider;
    private TermMentionRepository termMentionRepository;
    private OntologyRepository ontologyRepository;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        try {
            Configuration configuration = ConfigurationHelper.createHadoopConfigurationFromMap(stormConf);

            List<ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData>>();
            workers.add(new SearchWorker());
            workers.add(new OpenNlpMaximumEntropyEntityExtractorWorker(configuration, getUser()));
            workers.add(new OpenNlpDictionaryEntityExtractorWorker(configuration, getUser()));
            workers.add(new KnownEntityExtractorWorker(configuration, getUser()));
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

        GraphVertex artifactGraphVertex = graphRepository.findVertex(graphVertexId, getUser());
        runTextExtractions(artifactGraphVertex);

        getCollector().emit(new Values(jsonString));
        getCollector().ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("json"));
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
            TermMention termMentionModel = new TermMention(new TermMentionRowKey(artifactGraphVertexId, termMention.getStart(), termMention.getEnd()));
            termMentionModel.getMetadata().setSign(termMention.getSign());
            termMentionModel.getMetadata().setOntologyClassUri(termMention.getOntologyClassUri());

            Concept concept = ontologyRepository.getConceptByName(termMention.getOntologyClassUri(), getUser());
            if (concept != null) {
                termMentionModel.getMetadata().setConceptGraphVertexId(concept.getId());
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

    private class OpenNlpMaximumEntropyEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
        private OpenNlpMaximumEntropyEntityExtractor openNlpMaximumEntropyEntityExtractor;

        public OpenNlpMaximumEntropyEntityExtractorWorker(Configuration configuration, User user) throws IOException {
            openNlpMaximumEntropyEntityExtractor = new OpenNlpMaximumEntropyEntityExtractor(configuration, user);
            getInjector().injectMembers(openNlpMaximumEntropyEntityExtractor);
            openNlpMaximumEntropyEntityExtractor.init();
        }

        @Override
        protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
            return openNlpMaximumEntropyEntityExtractor.extract(work);
        }
    }

    private class OpenNlpDictionaryEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
        private OpenNlpDictionaryEntityExtractor openNlpDictionaryEntityExtractor;

        public OpenNlpDictionaryEntityExtractorWorker(Configuration configuration, User user) throws IOException {
            openNlpDictionaryEntityExtractor = new OpenNlpDictionaryEntityExtractor(configuration, user);
            getInjector().injectMembers(openNlpDictionaryEntityExtractor);
            openNlpDictionaryEntityExtractor.init();
        }

        @Override
        protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
            return openNlpDictionaryEntityExtractor.extract(work);
        }
    }

    private class KnownEntityExtractorWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
        private KnownEntityExtractor knownEntityExtractor;

        public KnownEntityExtractorWorker(Configuration configuration, User user) throws IOException {
            knownEntityExtractor = new KnownEntityExtractor(configuration, user);
            getInjector().injectMembers(knownEntityExtractor);
            knownEntityExtractor.init();
        }

        @Override
        protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
            return knownEntityExtractor.extract(work);
        }
    }
}
