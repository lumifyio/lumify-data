package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
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
    public Collection<ArtifactKey> searchArtifacts(String query) throws Exception {
        return new ArrayList<ArtifactKey>();
    }
}
