package com.altamiracorp.lumify.search;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.Collection;
import java.util.Properties;

public interface SearchProvider {
    void setup(Properties props, User user);

    void setup(Mapper.Context context, User user) throws Exception;

    void teardown() throws Exception;

    void add(Artifact artifact, User user) throws Exception;

    Collection<ArtifactSearchResult> searchArtifacts(String query, User user) throws Exception;

    void deleteIndex(User user);

    void initializeIndex(User user);
}
