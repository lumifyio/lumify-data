package com.altamiracorp.reddawn.statementExtraction;

import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRowKey;
import com.altamiracorp.reddawn.ucd.statement.Statement;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class StatementExtractionMRTest {

    StatementExtractionMR.StatementExtractorMapper mapper;

    @Before
    public void setUp () throws Exception{
        mapper = new StatementExtractionMR.StatementExtractorMapper();
    }

    @Test
    public void testStatementExtractorMapperMap () throws Exception{
        StatementExtractor mockExtractor = mock (StatementExtractor.class);

        Sentence mockSentence = mock (Sentence.class);
        SentenceRowKey mockSentenceRowKey = mock (SentenceRowKey.class);
        when (mockSentence.getRowKey()).thenReturn(mockSentenceRowKey);
        when (mockSentenceRowKey.toString()).thenReturn ("Temp Output");

        Statement mockStatement1 = mock(Statement.class);
        Statement mockStatement2 = mock(Statement.class);
        when (mockExtractor.extractStatements(mockSentence)).thenReturn(
                new ArrayList<Statement>((Arrays.asList(new Statement [] {mockStatement1,
                mockStatement2}))));
        Whitebox.setInternalState(mapper, StatementExtractor.class, mockExtractor);

        Mapper.Context mockContext = mock (Mapper.Context.class);
        mapper.map(mock(Text.class), mockSentence, mockContext);
        verify(mockContext, times(2)).write(any(Text.class), any(Sentence.class));
    }

}
