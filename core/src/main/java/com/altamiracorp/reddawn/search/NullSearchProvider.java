package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.term.Term;
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
    public void add (Term term){
    }

    @Override
    public Collection<ArtifactSearchResult> searchArtifacts(String query) throws Exception {
        return new ArrayList<ArtifactSearchResult>();
    }

    @Override
    public void deleteTables() {
    }

    @Override
    public void initializeTables() {
    }
}
