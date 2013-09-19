package com.altamiracorp.lumify.search;

import java.util.Collection;
import java.util.Properties;

import org.apache.hadoop.mapreduce.Mapper;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;

public abstract class SearchProvider {
    public static final String SEARCH_PROVIDER_PROP_KEY = "search.provider";

    public abstract void setup(Properties props, User user);

    public abstract void setup(Mapper.Context context, User user) throws Exception;

    public abstract void teardown() throws Exception;

    public abstract void add(Artifact artifact, User user) throws Exception;

    public abstract Collection<ArtifactSearchResult> searchArtifacts(String query, User user) throws Exception;

    public abstract void deleteIndex(User user);

    public abstract void initializeIndex(User user);
}
