package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.model.Artifact;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.ArrayList;
import java.util.Collection;

public class NullSearchProvider implements SearchProvider {
    @Override
    public void setup(Mapper.Context context) {
    }

    @Override
    public void add(Artifact artifact) {
    }

    @Override
    public Collection<ArtifactSearchResult> searchArtifacts(String query) throws Exception {
        return new ArrayList<ArtifactSearchResult>();
    }
}
