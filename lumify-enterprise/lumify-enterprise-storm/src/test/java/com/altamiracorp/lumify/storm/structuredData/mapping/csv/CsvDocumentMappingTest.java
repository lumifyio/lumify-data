/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermExtractionResult;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    @Mock
    private CsvRelationshipMapping rel3;

    private List<CsvTermColumnMapping> termList;
    private List<CsvRelationshipMapping> relList;

    private CsvDocumentMapping instance;

    @Before
    public void setup() {
        termList = Arrays.asList(term1, term2, term3);
        relList = Arrays.asList(rel1, rel2, rel3);

        instance = new CsvDocumentMapping(TEST_SUBJECT, TEST_SKIP_ROWS, termList, relList);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIllegalConstruction() {
        doConstructorTest("null subject", (String) null, IllegalArgumentException.class);
        doConstructorTest("empty subject", "", IllegalArgumentException.class);
        doConstructorTest("whitespace subject", "\n \t\t \n", IllegalArgumentException.class);
        doConstructorTest("negative skipRows", -1, IllegalArgumentException.class);
        doConstructorTest("null terms", (List) null, NullPointerException.class);
        doConstructorTest("empty terms", Collections.EMPTY_LIST, IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLegalConstruction() {
        doConstructorTest("trimmed subject", TEST_SUBJECT);
        doConstructorTest("untrimmed subject", "\n \t" + TEST_SUBJECT + "\t \n", TEST_SUBJECT);
        doConstructorTest("null skipRows", null, 0);
        doConstructorTest("0 skipRows", 0);
        doConstructorTest(">0 skipRows", 10);
        doConstructorTest_Terms("multiple terms", termList);
        doConstructorTest_Terms("one term", Arrays.asList(term1));
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

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final String subject, final Class<? extends Throwable> expectedError) {
        doConstructorTest(testName, subject, 0, termList, Collections.EMPTY_LIST, expectedError);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final Integer skipRows, final Class<? extends Throwable> expectedError) {
        doConstructorTest(testName, TEST_SUBJECT, skipRows, termList, Collections.EMPTY_LIST, expectedError);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final List<CsvTermColumnMapping> terms,
            final Class<? extends Throwable> expectedError) {
        doConstructorTest(testName, TEST_SUBJECT, 0, terms, Collections.EMPTY_LIST, expectedError);
    }

    private void doConstructorTest(final String testName, final String subject, final Integer skipRows,
            final List<CsvTermColumnMapping> terms, final List<CsvRelationshipMapping> relationships,
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
        doConstructorTest(testName, subject, 0, termList, Collections.EMPTY_LIST,
                expSubject, 0, termList, Collections.EMPTY_LIST);
    }

    private void doConstructorTest(final String testName, final Integer skipRows) {
        doConstructorTest(testName, skipRows, skipRows);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest(final String testName, final Integer skipRows, final Integer expSkipRows) {
        doConstructorTest(testName, TEST_SUBJECT, skipRows, termList, Collections.EMPTY_LIST,
                TEST_SUBJECT, expSkipRows, termList, Collections.EMPTY_LIST);
    }

    @SuppressWarnings("unchecked")
    private void doConstructorTest_Terms(final String testName, final List<CsvTermColumnMapping> terms) {
        doConstructorTest(testName, TEST_SUBJECT, 0, terms, Collections.EMPTY_LIST,
                TEST_SUBJECT, 0, terms, Collections.EMPTY_LIST);
    }

    private void doConstructorTest_Rels(final String testName, final List<CsvRelationshipMapping> rels) {
        doConstructorTest_Rels(testName, rels, rels);
    }

    private void doConstructorTest_Rels(final String testName, final List<CsvRelationshipMapping> rels,
            final List<CsvRelationshipMapping> expRels) {
        doConstructorTest(testName, TEST_SUBJECT, 0, termList, rels, TEST_SUBJECT, 0, termList, expRels);
    }

    private void doConstructorTest(final String testName, final String subject, final Integer skipRows,
            final List<CsvTermColumnMapping> terms, final List<CsvRelationshipMapping> relationships, final String expSubject,
            final int expSkipRows, final List<CsvTermColumnMapping> expTerms, final List<CsvRelationshipMapping> expRelationships) {
        CsvDocumentMapping mapping = new CsvDocumentMapping(subject, skipRows, terms, relationships);
        assertEquals(testName, expSubject, mapping.getSubject());
        assertEquals(testName, expSkipRows, mapping.getSkipRows());
        assertEquals(testName, expTerms, mapping.getTerms());
        assertEquals(testName, expRelationships, mapping.getRelationships());
    }
}
