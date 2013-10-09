package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.core.ingest.termExtraction.TermExtractionWorker;
import com.altamiracorp.lumify.core.ingest.termExtraction.TextExtractedAdditionalWorkData;
import com.altamiracorp.lumify.core.ingest.termExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.search.SearchProvider;
import com.google.inject.Inject;

import java.io.InputStream;
import java.util.Map;

public class SearchWorker extends TermExtractionWorker {
    private SearchProvider searchProvider;

    @Override
    protected TextExtractedInfo doWork(InputStream work, TextExtractedAdditionalWorkData textExtractedAdditionalWorkData) throws Exception {
        TextExtractedInfo textExtractedInfo = new TextExtractedInfo();
        searchProvider.add(textExtractedAdditionalWorkData.getGraphVertex(), work);
        return textExtractedInfo;
    }

    @Inject
    public void setSearchProvider(SearchProvider searchProvider) {
        this.searchProvider = searchProvider;
    }

    @Override
    public void prepare(Map conf, User user) throws Exception {
    }
}
