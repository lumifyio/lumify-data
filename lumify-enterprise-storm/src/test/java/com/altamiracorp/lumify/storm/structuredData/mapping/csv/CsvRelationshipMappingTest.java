/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.altamiracorp.lumify.storm.structuredData.mapping.csv;

import static org.junit.Assert.*;

import org.junit.Test;

public class CsvRelationshipMappingTest {
    private static final String TEST_LABEL = "testLabel";
    private static final String TEST_SRC_ID = "testSrcId";
    private static final String TEST_TGT_ID = "testTgtId";

    @Test
    public void testIllegalConstruction() {
        doConstructionTest_Label("null label", null, NullPointerException.class);
        doConstructionTest_Label("empty label", "", IllegalArgumentException.class);
        doConstructionTest_Label("whitespace label", "\n \t\t \n", IllegalArgumentException.class);
        doConstructionTest_SourceId("null srcId", null, NullPointerException.class);
        doConstructionTest_SourceId("empty srcId", "", IllegalArgumentException.class);
        doConstructionTest_SourceId("whitespace srcId", "\n \t\t \n", IllegalArgumentException.class);
        doConstructionTest_TargetId("null tgtId", null, NullPointerException.class);
        doConstructionTest_TargetId("empty tgtId", "", IllegalArgumentException.class);
        doConstructionTest_TargetId("whitespace tgtId", "\n \t\t \n", IllegalArgumentException.class);
    }

    @Test
    public void testLegalConstruction() {
        doConstructionTest_Label("trimmed label", TEST_LABEL, TEST_LABEL);
        doConstructionTest_Label("untrimmed label", "\t  " + TEST_LABEL + "  \t\n", TEST_LABEL);
        doConstructionTest_SourceId("trimmed srcId", TEST_SRC_ID, TEST_SRC_ID);
        doConstructionTest_SourceId("untrimmed srcId", "\t  " + TEST_SRC_ID + "  \t\n", TEST_SRC_ID);
        doConstructionTest_TargetId("trimmed tgtId", TEST_TGT_ID, TEST_TGT_ID);
        doConstructionTest_TargetId("untrimmed tgtId", "\t  " + TEST_TGT_ID + "  \t\n", TEST_TGT_ID);
    }

    private void doConstructionTest_Label(final String testName, final String label, final Class<? extends Throwable> expectedError) {
        doConstructionTest(testName, label, TEST_SRC_ID, TEST_TGT_ID, expectedError);
    }

    private void doConstructionTest_SourceId(final String testName, final String srcId, final Class<? extends Throwable> expectedError) {
        doConstructionTest(testName, TEST_LABEL, srcId, TEST_TGT_ID, expectedError);
    }

    private void doConstructionTest_TargetId(final String testName, final String tgtId, final Class<? extends Throwable> expectedError) {
        doConstructionTest(testName, TEST_LABEL, TEST_SRC_ID, tgtId, expectedError);
    }

    private void doConstructionTest(final String testName, final String label, final String srcId, final String tgtId,
            final Class<? extends Throwable> expectedError) {
        try {
            new CsvRelationshipMapping(label, srcId, tgtId);
            fail(String.format("[%s]: Expected %s", testName, expectedError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s", testName, expectedError.getName(), e.getClass().getName()),
                    expectedError.isAssignableFrom(e.getClass()));
        }
    }

    private void doConstructionTest_Label(final String testName, final String label, final String expLabel) {
        doConstructionTest(testName, label, TEST_SRC_ID, TEST_TGT_ID, expLabel, TEST_SRC_ID, TEST_TGT_ID);
    }

    private void doConstructionTest_SourceId(final String testName, final String srcId, final String expSrcId) {
        doConstructionTest(testName, TEST_LABEL, srcId, TEST_TGT_ID, TEST_LABEL, expSrcId, TEST_TGT_ID);
    }

    private void doConstructionTest_TargetId(final String testName, final String tgtId, final String expTgtId) {
        doConstructionTest(testName, TEST_LABEL, TEST_SRC_ID, tgtId, TEST_LABEL, TEST_SRC_ID, expTgtId);
    }

    private void doConstructionTest(final String testName, final String label, final String srcId, final String tgtId,
            final String expLabel, final String expSrcId, final String expTgtId) {
        CsvRelationshipMapping instance = new CsvRelationshipMapping(label, srcId, tgtId);
        assertEquals(testName, expLabel, instance.getLabel());
        assertEquals(testName, expSrcId, instance.getSourceTermId());
        assertEquals(testName, expTgtId, instance.getTargetTermId());
    }
}
