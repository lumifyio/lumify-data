package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public interface EntityExtractor {
    void setup(Mapper.Context context) throws IOException;

    Collection<Term> extract(Sentence sentence) throws Exception;
}
