package com.altamiracorp.reddawn.location;

import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SimpleArtifactLocationExtractorTest {
    @Test
    public void testNoLocationReturnsNoArtifacts() throws Exception {
        SimpleArtifactLocationExtractor simpleArtifactLocationExtractor = new SimpleArtifactLocationExtractor();
        Term term = new Term("Bob Smith", "CTA", "Person");
        term.addTermMention(new TermMention()
                .setArtifactKey("artifactKey")
                .setMentionStart(33L)
                .setMentionEnd(44L));
        Collection<Artifact> artifacts = simpleArtifactLocationExtractor.extract(term);
        assertEquals(0, artifacts.size());
    }

    @Test
    public void testOneMentionReturnsAnArtifact() throws Exception {
        SimpleArtifactLocationExtractor simpleArtifactLocationExtractor = new SimpleArtifactLocationExtractor();
        Term term = new Term("Bob Smith", "CTA", "Person");
        term.addTermMention(new TermMention()
                .setArtifactKey("artifactKey")
                .setMentionStart(33L)
                .setMentionEnd(44L)
                .setGeoLocation("POINT(44.45,-77.33)"));
        Collection<Artifact> artifacts = simpleArtifactLocationExtractor.extract(term);
        assertEquals(1, artifacts.size());
        Artifact artifact = artifacts.iterator().next();
        assertEquals("POINT(44.45,-77.33)", artifact.getDynamicMetadata().getGeoLocation());
    }
}
