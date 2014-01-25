/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import com.altamiracorp.lumify.util.LineReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CsvDocumentMapping.class })
public class CsvDocumentMappingTest {
    private static final String TEST_SUBJECT = "Test Subject";
    private static final int TEST_SKIP_ROWS = 1;
    private static final String TEST_PROCESS_ID = "testProcess";
    private static final String LIVES_AT = "livesAt";
    private static final String KNOWS = "knows";
    private static final String TERM1_ID = "term1";
    private static final String TERM2_ID = "term2";
    private static final String TERM3_ID = "term3";
    private static final int TERM1_IDX = 0;
    private static final int TERM2_IDX = 1;
    private static final int TERM3_IDX = 3;

    @Mock
    private Reader mappingReader;
    @Mock
    private CsvTermColumnMapping term1;
    @Mock
    private CsvTermColumnMapping term2;
    @Mock
    private CsvTermColumnMapping term3;
    @Mock
    private CsvRelationshipMapping rel1;
    @Mock
    private CsvRelationshipMapping rel2;

    private Map<String, CsvTermColumnMapping> termMap;
    private List<CsvRelationshipMapping> relList;

    private CsvDocumentMapping instance;

    @Before
    public void setup() {
        termMap = new HashMap<String, CsvTermColumnMapping>();
        termMap.put(TERM1_ID, term1);
        termMap.put(TERM2_ID, term2);
        termMap.put(TERM3_ID, term3);
        relList = Arrays.asList(rel1, rel2);

        when(term1.getColumnIndex()).thenReturn(TERM1_IDX);
        when(term2.getColumnIndex()).thenReturn(TERM2_IDX);
        when(term3.getColumnIndex()).thenReturn(TERM3_IDX);
        when(term1.compareTo(term1)).thenReturn(0);
        when(term1.compareTo(term2)).thenReturn(-1);
        when(term1.compareTo(term3)).thenReturn(-1);
        when(term2.compareTo(term1)).thenReturn(1);
        when(term2.compareTo(term2)).thenReturn(0);
        when(term2.compareTo(term3)).thenReturn(-1);
        when(term3.compareTo(term1)).thenReturn(1);
        when(term3.compareTo(term2)).thenReturn(1);
        when(term3.compareTo(term3)).thenReturn(0);
        when(rel1.getLabel()).thenReturn(LIVES_AT);
        when(rel1.getSourceTermId()).thenReturn(TERM1_ID);
        when(rel1.getTargetTermId()).thenReturn(TERM2_ID);
        when(rel2.getLabel()).thenReturn(KNOWS);
        when(rel2.getSourceTermId()).thenReturn(TERM3_ID);
        when(rel2.getTargetTermId()).thenReturn(TERM1_ID);

        // instance must be configured AFTER comparisons are set up for term mocks
        instance = new CsvDocumentMapping(TEST_SUBJECT, TEST_SKIP_ROWS, termMap, relList);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIllegalConstruction() {
        doConstructorTest("null subject", (String) null, IllegalArgumentException.class);
        doConstructorTest("empty subject", "", IllegalArgumentException.class);
        doConstructorTest("whitespace subject", "\n \t\t \n", IllegalArgumentException.class);
        doConstructorTest("negative skipRows", -1, IllegalArgumentException.class);
        doConstructorTest("null terms", (Map) null, NullPointerException.class);
        doConstructorTest("empty terms", Collections.EMPTY_MAP, IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLegalConstruction() {
        doConstructorTest("trimmed subject", TEST_SUBJECT);
        doConstructorTest("untrimmed subject", "\n \t" + TEST_SUBJECT + "\t \n", TEST_SUBJECT);
        doConstructorTest("null skipRows", null, 0);
        doConstructorTest("0 skipRows", 0);
        doConstructorTest(">0 skipRows", 10);
        doConstructorTest_Terms("multiple terms", termMap);
        Map<String, CsvTermColumnMapping> singleton = new HashMap<String, CsvTermColumnMapping>();
        singleton.put(TERM1_ID, term1);
        doConstructorTest_Terms("one term", singleton);
        doConstructorTest_Rels("null relationships", null, Collections.EMPTY_LIST);
        doConstructorTest_Rels("empty relationships", new ArrayList<CsvRelationshipMapping>(), Collections.EMPTY_LIST);
        doConstructorTest_Rels("multiple relationships", relList);
        doConstructorTest_Rels("single relationships", Arrays.asList(rel1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ingestDocument() throws Exception {
        InputStream testIn = mock(InputStream.class);
        Writer testOut = mock(Writer.class);
        InputStreamReader inReader = mock(InputStreamReader.class);
        CsvListReader reader = mock(CsvListReader.class);
        CsvListWriter writer = mock(CsvListWriter.class);

        PowerMockito.whenNew(InputStreamReader.class).withArguments(testIn).thenReturn(inReader);
        PowerMockito.whenNew(CsvListReader.class).withArguments(inReader, CsvPreference.EXCEL_PREFERENCE).thenReturn(reader);
        PowerMockito.whenNew(CsvListWriter.class).withArguments(testOut, CsvPreference.EXCEL_PREFERENCE).thenReturn(writer);

        List<String> line1 = mock(List.class, "line1");
        List<String> line2 = mock(List.class, "line2");
        List<String> line3 = mock(List.class, "line3");

        when(reader.read())
                .thenReturn(line1)
                .thenReturn(line2)
                .thenReturn(line3)
                .thenReturn(null);

        instance.ingestDocument(testIn, testOut);


        PowerMockito.verifyNew(InputStreamReader.class).withArguments(testIn);
        PowerMockito.verifyNew(CsvListReader.class).withArguments(inReader, CsvPreference.EXCEL_PREFERENCE);
        PowerMockito.verifyNew(CsvListWriter.class).withArguments(testOut, CsvPreference.EXCEL_PREFERENCE);
        verify(writer).write(line1);
        verify(writer).write(line2);
        verify(writer).write(line3);
        verify(writer).close();
    }

    @Test
    public void testMapDocument_EmptyDocument() throws Exception {
        when(mappingReader.read()).thenReturn(-1);
        TermExtractionResult expected = new TermExtractionResult();
        TermExtractionResult actual = instance.mapDocument(mappingReader, TEST_PROCESS_ID);
        assertEquals(expected, actual);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapDocument_NullColumns() throws Exception {
        LineReader lineReader = mock(LineReader.class);
        StringReader strReader = mock(StringReader.class);
        CsvListReader csvReader = mock(CsvListReader.class);
        TermMention line1term1 = mock(TermMention.class, "l1t1");
        TermMention line1term2 = mock(TermMention.class, "l1t2");
        TermMention line1term3 = mock(TermMention.class, "l1t3");
        String line1 = "line1";
        String line2 = "line2";
        List<String> fields1 = Arrays.asList("foo", "bar", "fizz", "buzz");
        int lineOffset1 = 30;
        int lineOffset2 = 60;
        int line1Term1Offset = lineOffset1;
        int line1Term2Offset = line1Term1Offset + fields1.get(0).length() + 1;
        int line1Term3Offset = line1Term2Offset + fields1.get(1).length() + fields1.get(2).length() + 2;

        PowerMockito.whenNew(LineReader.class).withArguments(mappingReader).thenReturn(lineReader);
        PowerMockito.whenNew(StringReader.class).withArguments(anyString()).thenReturn(strReader);
        PowerMockito.whenNew(CsvListReader.class).withArguments(strReader, CsvPreference.EXCEL_PREFERENCE).thenReturn(csvReader);

        when(lineReader.readLine()).thenReturn(line1, line2);
        when(lineReader.getOffset()).thenReturn(lineOffset1, lineOffset2);
        when(csvReader.read()).thenReturn(fields1, (List<String>) null);
        when(term1.mapTerm(fields1, line1Term1Offset, TEST_PROCESS_ID)).thenReturn(line1term1);
        when(term2.mapTerm(fields1, line1Term2Offset, TEST_PROCESS_ID)).thenReturn(line1term2);
        when(term3.mapTerm(fields1, line1Term3Offset, TEST_PROCESS_ID)).thenReturn(line1term3);

        List<TermMention> expectedMentions = Arrays.asList(line1term1, line1term2, line1term3);
        List<TermRelationship> expectedRelationships = Arrays.asList(
                new TermRelationship(line1term1, line1term2, LIVES_AT),
                new TermRelationship(line1term3, line1term1, KNOWS)
        );
        TermExtractionResult expected = new TermExtractionResult();
        expected.addAllTermMentions(expectedMentions);
        expected.addAllRelationships(expectedRelationships);

        TermExtractionResult result = instance.mapDocument(mappingReader, TEST_PROCESS_ID);

        assertEquals(expected, result);
        verify(lineReader).skipLines(TEST_SKIP_ROWS);
        PowerMockito.verifyNew(LineReader.class).withArguments(mappingReader);
        PowerMockito.verifyNew(StringReader.class).withArguments(line1);
        PowerMockito.verifyNew(StringReader.class).withArguments(line2);
        PowerMockito.verifyNew(CsvListReader.class, times(2)).withArguments(strReader, CsvPreference.EXCEL_PREFERENCE);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMapDocument() throws Exception {
        LineReader lineReader = mock(LineReader.class);
        StringReader strReader = mock(StringReader.class);
        CsvListReader csvReader = mock(CsvListReader.class);
        TermMention line1term1 = mock(TermMention.class, "l1t1");
        TermMention line1term2 = mock(TermMention.class, "l1t2");
        TermMention line1term3 = mock(TermMention.class, "l1t3");
        TermMention line2term1 = mock(TermMention.class, "l2t1");
        TermMention line2term3 = mock(TermMention.class, "l2t3");
        TermMention line3term1 = mock(TermMention.class, "l3t1");
        TermMention line3term2 = mock(TermMention.class, "l3t2");
        TermMention line3term3 = mock(TermMention.class, "l3t3");
        String line1 = "line1";
        String line2 = "line2";
        String line3 = "line3";
        List<String> fields1 = Arrays.asList("foo", "bar", "fizz", "buzz");
        List<String> fields2 = Arrays.asList("this", "is", "a", "test");
        List<String> fields3 = Arrays.asList("the", "pen", "is", "blue");
        int lineOffset1 = 30;
        int lineOffset2 = 60;
        int lineOffset3 = 90;
        int lineOffset4 = 120;
        int line1Term1Offset = lineOffset1;
        int line1Term2Offset = line1Term1Offset + fields1.get(0).length() + 1;
        int line1Term3Offset = line1Term2Offset + fields1.get(1).length() + fields1.get(2).length() + 2;
        int line2Term1Offset = lineOffset2;
        int line2Term2Offset = line2Term1Offset + fields2.get(0).length() + 1;
        int line2Term3Offset = line2Term2Offset + fields2.get(1).length() + fields2.get(2).length() + 2;
        int line3Term1Offset = lineOffset3;
        int line3Term2Offset = line3Term1Offset + fields3.get(0).length() + 1;
        int line3Term3Offset = line3Term2Offset + fields3.get(1).length() + fields3.get(2).length() + 2;

        PowerMockito.whenNew(LineReader.class).withArguments(mappingReader).thenReturn(lineReader);
        PowerMockito.whenNew(StringReader.class).withArguments(anyString()).thenReturn(strReader);
        PowerMockito.whenNew(CsvListReader.class).withArguments(strReader, CsvPreference.EXCEL_PREFERENCE).thenReturn(csvReader);

        when(lineReader.readLine()).thenReturn(line1, line2, line3, null);
        when(lineReader.getOffset()).thenReturn(lineOffset1, lineOffset2, lineOffset3, lineOffset4);
        when(csvReader.read()).thenReturn(fields1, fields2, fields3);
        when(term1.mapTerm(fields1, line1Term1Offset, TEST_PROCESS_ID)).thenReturn(line1term1);
        when(term2.mapTerm(fields1, line1Term2Offset, TEST_PROCESS_ID)).thenReturn(line1term2);
        when(term3.mapTerm(fields1, line1Term3Offset, TEST_PROCESS_ID)).thenReturn(line1term3);
        when(term1.mapTerm(fields2, line2Term1Offset, TEST_PROCESS_ID)).thenReturn(line2term1);
        when(term2.mapTerm(fields2, line2Term2Offset, TEST_PROCESS_ID)).thenReturn(null);
        when(term3.mapTerm(fields2, line2Term3Offset, TEST_PROCESS_ID)).thenReturn(line2term3);
        when(term1.mapTerm(fields3, line3Term1Offset, TEST_PROCESS_ID)).thenReturn(line3term1);
        when(term2.mapTerm(fields3, line3Term2Offset, TEST_PROCESS_ID)).thenReturn(line3term2);
        when(term3.mapTerm(fields3, line3Term3Offset, TEST_PROCESS_ID)).thenReturn(line3term3);

        List<TermMention> expectedMentions = Arrays.asList(
                line1term1, line1term2, line1term3,
                line2term1, line2term3,
                line3term1, line3term2, line3term3
        );
        List<TermRelationship> expectedRelationships = Arrays.asList(
                new TermRelationship(line1term1, line1term2, LIVES_AT),
                new TermRelationship(line1term3, line1term1, KNOWS),
                new TermRelationship(line2term3, line2term1, KNOWS),
                new TermRelationship(line3term1, line3term2, LIVES_AT),
                new TermRelationship(line3term3, line3term1, KNOWS)
        );
        TermExtractionResult expected = new TermExtractionResult();
        expected.addAllTermMentions(expectedMentions);
        expected.addAllRelationships(expectedRelationships);

        TermExtractionResult result = instance.mapDocument(mappingReader, TEST_PROCESS_ID);

        assertEquals(expected, result);
        verify(lineReader).skipLines(TEST_SKIP_ROWS);
        PowerMockito.verifyNew(LineReader.class).withArguments(mappingReader);
        PowerMockito.verifyNew(StringReader.class).withArguments(line1);
        PowerMockito.verifyNew(StringReader.class).withArguments(line2);
        PowerMockito.verifyNew(StringReader.class).withArguments(line3);
        PowerMockito.verifyNew(CsvListReader.class, times(3)).withArguments(strReader, CsvPreference.EXCEL_PREFERENCE);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final String subject, final Class<? extends Throwable> expectedError) {
        doConstructorTest(testName, subject, 0, termMap, Collections.EMPTY_LIST, expectedError);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final Integer skipRows, final Class<? extends Throwable> expectedError) {
        doConstructorTest(testName, TEST_SUBJECT, skipRows, termMap, Collections.EMPTY_LIST, expectedError);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final Map<String, CsvTermColumnMapping> terms,
            final Class<? extends Throwable> expectedError) {
        doConstructorTest(testName, TEST_SUBJECT, 0, terms, Collections.EMPTY_LIST, expectedError);
    }

    private void doConstructorTest(final String testName, final String subject, final Integer skipRows,
            final Map<String, CsvTermColumnMapping> terms, final List<CsvRelationshipMapping> relationships,
            final Class<? extends Throwable> expectedError) {
        try {
            new CsvDocumentMapping(subject, skipRows, terms, relationships);
            fail(String.format("[%s] Expected %s.", testName, expectedError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s] expected %s, got %s.", testName, expectedError.getName(), e.getClass().getName()),
                    expectedError.isAssignableFrom(e.getClass()));
        }
    }

    private void doConstructorTest(final String testName, final String subject) {
        doConstructorTest(testName, subject, subject);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final String subject, final String expSubject) {
        doConstructorTest(testName, subject, 0, termMap, Collections.EMPTY_LIST,
                expSubject, 0, termMap, Collections.EMPTY_LIST);
    }

    private void doConstructorTest(final String testName, final Integer skipRows) {
        doConstructorTest(testName, skipRows, skipRows);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final Integer skipRows, final Integer expSkipRows) {
        doConstructorTest(testName, TEST_SUBJECT, skipRows, termMap, Collections.EMPTY_LIST,
                TEST_SUBJECT, expSkipRows, termMap, Collections.EMPTY_LIST);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest_Terms(final String testName, final Map<String, CsvTermColumnMapping> terms) {
        doConstructorTest(testName, TEST_SUBJECT, 0, terms, Collections.EMPTY_LIST,
                TEST_SUBJECT, 0, terms, Collections.EMPTY_LIST);
    }

    private void doConstructorTest_Rels(final String testName, final List<CsvRelationshipMapping> rels) {
        doConstructorTest_Rels(testName, rels, rels);
    }

    private void doConstructorTest_Rels(final String testName, final List<CsvRelationshipMapping> rels,
            final List<CsvRelationshipMapping> expRels) {
        doConstructorTest(testName, TEST_SUBJECT, 0, termMap, rels, TEST_SUBJECT, 0, termMap, expRels);
    }

    private void doConstructorTest(final String testName, final String subject, final Integer skipRows,
            final Map<String, CsvTermColumnMapping> terms, final List<CsvRelationshipMapping> relationships, final String expSubject,
            final int expSkipRows, final Map<String, CsvTermColumnMapping> expTerms, final List<CsvRelationshipMapping> expRelationships) {
        CsvDocumentMapping mapping = new CsvDocumentMapping(subject, skipRows, terms, relationships);
        assertEquals(testName, expSubject, mapping.getSubject());
        assertEquals(testName, expSkipRows, mapping.getSkipRows());
        assertEquals(testName, expTerms, mapping.getTerms());
        assertEquals(testName, expRelationships, mapping.getRelationships());
    }
}
