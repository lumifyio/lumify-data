package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.term.Term;
import org.apache.hadoop.mapreduce.Mapper;

import java.util.ArrayList;
import java.util.Collection;

public class NullEntityExtractor extends EntityExtractor {
    @Override
    public void setup(Mapper.Context context) {
    }

    @Override
    public Collection<Term> extract(Sentence sentence) throws Exception {
        return new ArrayList<Term>();
    }

    @Override
    String getModelName() {
        return "Null";
    }

    @Override
    String getExtractorId() {
        return "NullExtractor";
    }
}
