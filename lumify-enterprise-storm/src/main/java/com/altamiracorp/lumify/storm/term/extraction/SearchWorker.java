package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.search.SearchProvider;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.inject.Inject;

import java.io.InputStream;
import java.util.Map;

public class SearchWorker extends TermExtractionWorker {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(SearchWorker.class);

    private SearchProvider searchProvider;

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData termExtractionAdditionalWorkData) throws Exception {
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        GraphVertex vertex = termExtractionAdditionalWorkData.getGraphVertex();

        LOGGER.debug("Adding graph vertex (id: %s) data to search index", vertex.getId());
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
