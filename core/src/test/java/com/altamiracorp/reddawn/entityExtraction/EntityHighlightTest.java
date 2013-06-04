package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.model.Term;
import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermMention;
import com.altamiracorp.reddawn.ucd.model.terms.TermMetadata;
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
        ArtifactKey artifactKey = ArtifactKey.newBuilder()
                .docArtifactBytes("artifact1".getBytes())
                .build();
        terms.add(
                Term.newBuilder()
                        .key(
                                TermKey.newBuilder()
                                        .sign("joe ferner")
                                        .concept("person")
                                        .model("ee")
                                        .build())
                        .metadata(
                                TermMetadata.newBuilder()
                                        .artifactKey(artifactKey)
                                        .mention(new TermMention(18, 28))
                                        .build()
                        )
                        .build());
        terms.add(
                Term.newBuilder()
                        .key(
                                TermKey.newBuilder()
                                        .sign("jeff kunkle")
                                        .concept("person")
                                        .model("ee")
                                        .build())
                        .metadata(
                                TermMetadata.newBuilder()
                                        .artifactKey(artifactKey)
                                        .mention(new TermMention(33, 44))
                                        .build()
                        )
                        .build());
        List<EntityHighlightMR.EntityHighlightMapper.TermAndTermMetadata> termAndTermMetadata = EntityHighlightMR.EntityHighlightMapper.getTermAndTermMetadataForArtifact(artifactKey, terms);
        String highlightText = EntityHighlightMR.EntityHighlightMapper.getHighlightedText("Test highlight of Joe Ferner and Jeff Kunkle.", termAndTermMetadata);
        assertEquals("Test highlight of <span class=\"entity person\" term-key=\"joe ferner\\x1Fee\\x1Fperson\">Joe Ferner</span> and <span class=\"entity person\" term-key=\"jeff kunkle\\x1Fee\\x1Fperson\">Jeff Kunkle</span>.", highlightText);
    }

    @Test(expected = AssertionError.class) // TODO handle overlapping entities
    public void testGetHighlightedTextOverlaps() throws Exception {
        ArrayList<Term> terms = new ArrayList<Term>();
        ArtifactKey artifactKey = ArtifactKey.newBuilder()
                .docArtifactBytes("artifact1".getBytes())
                .build();
        terms.add(
                Term.newBuilder()
                        .key(
                                TermKey.newBuilder()
                                        .sign("joe ferner")
                                        .concept("person")
                                        .model("ee")
                                        .build())
                        .metadata(
                                TermMetadata.newBuilder()
                                        .artifactKey(artifactKey)
                                        .mention(new TermMention(18, 28))
                                        .build()
                        )
                        .build());
        terms.add(
                Term.newBuilder()
                        .key(
                                TermKey.newBuilder()
                                        .sign("joe")
                                        .concept("person")
                                        .model("ee")
                                        .build())
                        .metadata(
                                TermMetadata.newBuilder()
                                        .artifactKey(artifactKey)
                                        .mention(new TermMention(18, 21))
                                        .build()
                        )
                        .build());
        List<EntityHighlightMR.EntityHighlightMapper.TermAndTermMetadata> termAndTermMetadata = EntityHighlightMR.EntityHighlightMapper.getTermAndTermMetadataForArtifact(artifactKey, terms);
        String highlightText = EntityHighlightMR.EntityHighlightMapper.getHighlightedText("Test highlight of Joe Ferner.", termAndTermMetadata);
        assertEquals("Test highlight of <span class=\"entity person\" term-key=\"joe ferner\\x1Fee\\x1Fperson\"><span class=\"entity person\" term-key=\"joe\\x1Fee\\x1Fperson\">Joe</span> Ferner</span>.", highlightText);
    }
}