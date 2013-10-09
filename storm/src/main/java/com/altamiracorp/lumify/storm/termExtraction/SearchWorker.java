package com.altamiracorp.lumify.storm.termExtraction;

import com.altamiracorp.lumify.entityExtraction.TextExtractedInfo;
import com.altamiracorp.lumify.model.search.SearchProvider;
import com.altamiracorp.lumify.core.util.ThreadedTeeInputStreamWorker;
import com.google.inject.Inject;

import java.io.InputStream;

class SearchWorker extends ThreadedTeeInputStreamWorker<TextExtractedInfo, TextExtractedAdditionalWorkData> {
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
}
