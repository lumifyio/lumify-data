package com.altamiracorp.lumify.core.model.search;

import java.io.InputStream;
import java.util.Collection;

import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.user.User;

public abstract class SearchProvider {
    public abstract void add(GraphVertex graphVertex, InputStream textIn) throws Exception;

    public abstract Collection<ArtifactSearchResult> searchArtifacts(String query, User user) throws Exception;

    public abstract void deleteIndex(User user);

    public abstract void initializeIndex(User user);

    public abstract void close() throws Exception;
}
