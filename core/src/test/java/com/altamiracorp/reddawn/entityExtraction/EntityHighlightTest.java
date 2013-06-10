package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermMention;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class EntityHighlightTest {
    @Test
    public void testGetHighlightedText() throws Exception {
        ArrayList<Term> terms = new ArrayList<Term>();
        ArtifactRowKey artifactKey = new ArtifactRowKey("artifact1");
        terms.add(
                new Term("joe ferner", "ee", "person")
                        .addTermMention(new TermMention()
                                .setArtifactKey(artifactKey.toString())
                                .setMentionStart(18L)
                                .setMentionEnd(28L)
                        )
        );
        terms.add(
                new Term("jeff kunkle", "ee", "person")
                        .addTermMention(new TermMention()
                                .setArtifactKey(artifactKey.toString())
                                .setMentionStart(33L)
                                .setMentionEnd(44L)
                        )
        );
        List<EntityHighlightMR.EntityHighlightMapper.TermAndTermMention> termAndTermMetadata = EntityHighlightMR.EntityHighlightMapper.getTermAndTermMetadataForArtifact(artifactKey, terms);
        String highlightText = EntityHighlightMR.EntityHighlightMapper.getHighlightedText("Test highlight of Joe Ferner and Jeff Kunkle.", termAndTermMetadata);
        assertEquals("Test highlight of <span class=\"entity person\" term-key=\"joe ferner\\x1Fee\\x1Fperson\">Joe Ferner</span> and <span class=\"entity person\" term-key=\"jeff kunkle\\x1Fee\\x1Fperson\">Jeff Kunkle</span>.", highlightText);
    }

    @Test(expected = AssertionError.class) // TODO handle overlapping entities
    public void testGetHighlightedTextOverlaps() throws Exception {
        ArrayList<Term> terms = new ArrayList<Term>();
        ArtifactRowKey artifactKey = ArtifactRowKey.build("artifact1".getBytes());
        terms.add(
                new Term("joe ferner", "ee", "person")
                        .addTermMention(new TermMention()
                                .setArtifactKey(artifactKey.toString())
                                .setMentionStart(18L)
                                .setMentionEnd(28L)
                        )
        );
        terms.add(
                new Term("joe", "ee", "person")
                        .addTermMention(new TermMention()
                                .setArtifactKey(artifactKey.toString())
                                .setMentionStart(18L)
                                .setMentionEnd(21L)
                        )
        );
        List<EntityHighlightMR.EntityHighlightMapper.TermAndTermMention> termAndTermMetadata = EntityHighlightMR.EntityHighlightMapper.getTermAndTermMetadataForArtifact(artifactKey, terms);
        String highlightText = EntityHighlightMR.EntityHighlightMapper.getHighlightedText("Test highlight of Joe Ferner.", termAndTermMetadata);
        assertEquals("Test highlight of <span class=\"entity person\" term-key=\"joe ferner\\x1Fee\\x1Fperson\"><span class=\"entity person\" term-key=\"joe\\x1Fee\\x1Fperson\">Joe</span> Ferner</span>.", highlightText);
    }
}