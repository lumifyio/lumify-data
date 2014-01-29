package com.altamiracorp.lumify.mapping.csv;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import com.altamiracorp.lumify.core.ingest.term.extraction.TermMention;
import com.altamiracorp.lumify.core.ingest.term.extraction.TermRelationship;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractCsvRelationshipMappingTest {
    private static final String TEST_LABEL = "testLabel";
    private static final String TEST_SRC_ID = "testSrcId";
    private static final String TEST_TGT_ID = "testTgtId";

    @Mock
    private TermMention term1;
    @Mock
    private TermMention term2;
    @Mock
    private Delegate delegate;

    private AbstractCsvRelationshipMapping instance;

    @Before
    public void setup() {
        instance = new TestImpl(TEST_SRC_ID, TEST_TGT_ID);
    }

    @Test
    public void testIllegalConstruction() {
        doConstructionTest_SourceId("null srcId", null, NullPointerException.class);
        doConstructionTest_SourceId("empty srcId", "", IllegalArgumentException.class);
        doConstructionTest_SourceId("whitespace srcId", "\n \t\t \n", IllegalArgumentException.class);
        doConstructionTest_TargetId("null tgtId", null, NullPointerException.class);
        doConstructionTest_TargetId("empty tgtId", "", IllegalArgumentException.class);
        doConstructionTest_TargetId("whitespace tgtId", "\n \t\t \n", IllegalArgumentException.class);
    }

    @Test
    public void testLegalConstruction() {
        doConstructionTest_SourceId("trimmed srcId", TEST_SRC_ID, TEST_SRC_ID);
        doConstructionTest_SourceId("untrimmed srcId", "\t  " + TEST_SRC_ID + "  \t\n", TEST_SRC_ID);
        doConstructionTest_TargetId("trimmed tgtId", TEST_TGT_ID, TEST_TGT_ID);
        doConstructionTest_TargetId("untrimmed tgtId", "\t  " + TEST_TGT_ID + "  \t\n", TEST_TGT_ID);
    }

    @Test
    public void testCreateRelationship() {
        doTestCreateRelationship("null source", null, term2, TEST_LABEL, null);
        doTestCreateRelationship("null target", term1, null, TEST_LABEL, null);
        doTestCreateRelationship("null label", term1, term2, null, null);
        doTestCreateRelationship("valid relationship", term1, term2, TEST_LABEL, new TermRelationship(term1, term2, TEST_LABEL));
        doTestCreateRelationship("inverse relationship", term2, term1, TEST_LABEL, new TermRelationship(term2, term1, TEST_LABEL));
        doTestCreateRelationship("circular relationship", term1, term1, TEST_LABEL, new TermRelationship(term1, term1, TEST_LABEL));
    }

    private void doTestCreateRelationship(final String testName, final TermMention source, final TermMention target, final String label,
            final TermRelationship expected) {
        when(delegate.getLabel(source, target)).thenReturn(label);
        TermRelationship result = instance.createRelationship(source, target);
        assertEquals(String.format("[%s]: ", testName), expected, result);
    }

    private void doConstructionTest_SourceId(final String testName, final String srcId, final Class<? extends Throwable> expectedError) {
        doConstructionTest(testName, srcId, TEST_TGT_ID, expectedError);
    }

    private void doConstructionTest_TargetId(final String testName, final String tgtId, final Class<? extends Throwable> expectedError) {
        doConstructionTest(testName, TEST_SRC_ID, tgtId, expectedError);
    }

    private void doConstructionTest(final String testName, final String srcId, final String tgtId,
            final Class<? extends Throwable> expectedError) {
        try {
            new TestImpl(srcId, tgtId);
            fail(String.format("[%s]: Expected %s", testName, expectedError.getName()));
        } catch (Exception e) {
            assertTrue(String.format("[%s]: Expected %s, got %s", testName, expectedError.getName(), e.getClass().getName()),
                    expectedError.isAssignableFrom(e.getClass()));
        }
    }

    private void doConstructionTest_SourceId(final String testName, final String srcId, final String expSrcId) {
        doConstructionTest(testName, srcId, TEST_TGT_ID, expSrcId, TEST_TGT_ID);
    }

    private void doConstructionTest_TargetId(final String testName, final String tgtId, final String expTgtId) {
        doConstructionTest(testName, TEST_SRC_ID, tgtId, TEST_SRC_ID, expTgtId);
    }

    private void doConstructionTest(final String testName, final String srcId, final String tgtId,
            final String expSrcId, final String expTgtId) {
        AbstractCsvRelationshipMapping test = new TestImpl(srcId, tgtId);
        assertEquals(testName, expSrcId, test.getSourceTermId());
        assertEquals(testName, expTgtId, test.getTargetTermId());
    }

    private static interface Delegate {
        String getLabel(final TermMention source, final TermMention target);
    }

    private class TestImpl extends AbstractCsvRelationshipMapping {
        public TestImpl(String srcId, String tgtId) {
            super(srcId, tgtId);
        }

        @Override
        protected String getLabel(TermMention source, TermMention target) {
            return delegate.getLabel(source, target);
        }
    }
}
