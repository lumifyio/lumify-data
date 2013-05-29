package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.Collection;

public interface SearchProvider {
    void setup(Mapper.Context context) throws Exception;

    void add(Artifact artifact) throws Exception;

    Collection<ArtifactSearchResult> searchArtifacts(String query) throws Exception;
}
