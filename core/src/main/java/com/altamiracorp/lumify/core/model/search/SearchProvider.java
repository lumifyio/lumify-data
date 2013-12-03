package com.altamiracorp.lumify.core.model.search;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.user.User;

import java.io.InputStream;
import java.util.Collection;

public abstract class SearchProvider {
    public static final String SEARCH_PROVIDER_PROP_KEY = "search.provider";

    public abstract void add(GraphVertex graphVertex, InputStream textIn) throws Exception;

    public abstract Collection<ArtifactSearchResult> searchArtifacts(String query, User user) throws Exception;

    public abstract ArtifactSearchPagedResults searchArtifacts(String query, User user, int from, int size, String subType) throws Exception;

    public abstract void deleteIndex(User user);

    public abstract void initializeIndex(User user);

    public abstract void close() throws Exception;
}
