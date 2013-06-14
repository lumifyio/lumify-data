package com.altamiracorp.reddawn.statementExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public class SentenceBasedStatementExtractor implements StatementExtractor {
    @Override
    public Collection<Sentence> extractStatements(Sentence sentence) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setup(Mapper<Text, Sentence, Text, Statement>.Context context) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
