package com.altamiracorp.lumify.core.model.search;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.core.model.graph.GraphVertex;
import com.altamiracorp.lumify.core.model.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

public abstract class SearchProvider {
    //public static final String SEARCH_PROVIDER_PROP_KEY = "search.provider";

    public abstract void add(Artifact artifact, User user) throws Exception;

    public abstract void add(GraphVertex graphVertex, InputStream textIn) throws Exception;

    public abstract Collection<ArtifactSearchResult> searchArtifacts(String query, User user) throws Exception;

    public abstract void deleteIndex(User user);

    public abstract void initializeIndex(User user);

    public abstract void close() throws Exception;
}
