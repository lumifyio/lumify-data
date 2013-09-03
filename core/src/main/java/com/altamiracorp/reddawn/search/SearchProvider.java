package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.Collection;
import java.util.Properties;

public interface SearchProvider {
    void setup(Properties props);

    void setup(Mapper.Context context) throws Exception;

    void teardown() throws Exception;

    void add(Artifact artifact) throws Exception;

    Collection<ArtifactSearchResult> searchArtifacts(String query) throws Exception;

    void deleteTables();

    void initializeTables();
}
