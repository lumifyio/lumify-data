package com.altamiracorp.reddawn.search;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.Collection;

public interface SearchProvider {
    void setup(Mapper.Context context) throws Exception;

    void add(Artifact artifact) throws Exception;

    void add(Term term) throws Exception;

    Collection<ArtifactSearchResult> searchArtifacts(String query) throws Exception;

    Collection<TermSearchResult> searchTerms (String query) throws Exception;
}
