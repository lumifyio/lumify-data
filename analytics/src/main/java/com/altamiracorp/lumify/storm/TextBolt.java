package com.altamiracorp.lumify.storm;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.search.SearchProvider;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import com.altamiracorp.lumify.util.ThreadedInputStreamProcess;
import com.altamiracorp.lumify.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TextBolt extends BaseLumifyBolt {
    private ThreadedInputStreamProcess<TextExtractedInfo, AdditionalWorkData> threadedInputStreamProcess;
    private SearchProvider searchProvider;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);
        List<ThreadedTeeInputStreamWorker<TextExtractedInfo, AdditionalWorkData>> workers = new ArrayList<ThreadedTeeInputStreamWorker<TextExtractedInfo, AdditionalWorkData>>();
        workers.add(new SearchWorker());
        threadedInputStreamProcess = new ThreadedInputStreamProcess<TextExtractedInfo, AdditionalWorkData>("textBoltWorkers", workers);
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = new JSONObject(input.getString(0));
        String graphVertexId = json.getString("graphVertexId");

        GraphVertex graphVertex = graphRepository.findVertex(graphVertexId, getUser());
        InputStream textIn = getInputStream(graphVertex);

        AdditionalWorkData additionalWorkData = new AdditionalWorkData();
        additionalWorkData.setGraphVertex(graphVertex);
        threadedInputStreamProcess.doWork(textIn, additionalWorkData);
        getCollector().ack(input);
    }

    private InputStream getInputStream(GraphVertex graphVertex) throws Exception {
        InputStream textIn;
        String textHdfsPath = (String) graphVertex.getProperty(PropertyName.TEXT_HDFS_PATH);
        if (textHdfsPath != null) {
            textIn = openFile(textHdfsPath);
        } else {
            String artifactRowKey = (String) graphVertex.getProperty(PropertyName.ROW_KEY);
            Artifact artifact = artifactRepository.findByRowKey(artifactRowKey, getUser());
            textIn = new ByteArrayInputStream(artifact.getMetadata().getText().getBytes());
        }
        return textIn;
    }

    @Override
    public void cleanup() {
        this.threadedInputStreamProcess.stop();
        super.cleanup();
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    private class AdditionalWorkData {

        private GraphVertex graphVertex;

        public void setGraphVertex(GraphVertex graphVertex) {
            this.graphVertex = graphVertex;
        }

        public GraphVertex getGraphVertex() {
            return graphVertex;
        }
    }

    private class SearchWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, AdditionalWorkData> {
        @Override
        protected TextExtractedInfo doWork(InputStream work, AdditionalWorkData additionalWorkData) throws Exception {
            TextExtractedInfo textExtractedInfo = new TextExtractedInfo();
            searchProvider.add(additionalWorkData.getGraphVertex(), work);
            return textExtractedInfo;
        }
    }
}
