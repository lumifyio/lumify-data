package com.altamiracorp.lumify.storm.term.extraction;

import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.User;
import com.google.inject.Inject;

public class SearchWorker extends TermExtractionWorker {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchWorker.class);

    private SearchProvider searchProvider;

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData termExtractionAdditionalWorkData) throws Exception {
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        GraphVertex vertex = termExtractionAdditionalWorkData.getGraphVertex();

        LOGGER.debug(String.format("Adding graph vertex (id: %s) data to search index", vertex.getId()));
        searchProvider.add(vertex, work);
        return termExtractionResult;
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @Override
    public void prepare(Map conf, User user) throws Exception {
    }
}
