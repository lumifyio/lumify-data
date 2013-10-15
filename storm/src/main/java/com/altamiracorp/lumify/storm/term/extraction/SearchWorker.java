package com.altamiracorp.lumify.storm.term.extraction;

import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionResult;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.search.SearchProvider;
import com.google.inject.Inject;

import java.io.InputStream;
import java.util.Map;

public class SearchWorker extends TermExtractionWorker {
    private SearchProvider searchProvider;

    @Override
    protected TermExtractionResult doWork(InputStream work, TermExtractionAdditionalWorkData termExtractionAdditionalWorkData) throws Exception {
        TermExtractionResult termExtractionResult = new TermExtractionResult();
        searchProvider.add(termExtractionAdditionalWorkData.getGraphVertex(), work);
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
