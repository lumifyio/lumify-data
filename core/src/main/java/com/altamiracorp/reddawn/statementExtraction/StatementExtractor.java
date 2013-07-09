package com.altamiracorp.reddawn.statementExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.Collection;

public interface StatementExtractor {
    public Collection<Statement> extractStatements(Sentence sentence);

    void setup(Mapper<Text,Sentence,Text,Statement>.Context context) throws IOException;
}
