package com.altamiracorp.reddawn.ucd.model.Sentence;

import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermKey;
import com.altamiracorp.reddawn.ucd.model.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.model.sentence.SentenceData;
import com.altamiracorp.reddawn.ucd.model.sentence.SentenceMetadata;
import com.altamiracorp.reddawn.ucd.model.sentence.SentenceTerm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SentenceTest {
  @Test
  public void createASource() {
    Sentence.Builder sb = Sentence.newBuilder();

    SentenceData.Builder sdb = SentenceData.newBuilder();

    SentenceMetadata.Builder smb = SentenceMetadata.newBuilder();

    SentenceTerm.Builder stb = SentenceTerm.newBuilder();

    // todo: add more fields

    TermKey termKey = TermKey.newBuilder().sign("a q khan")
            .model("CTA")
            .concept("PERSON")
            .build();

    stb.termKey(termKey);

    Sentence sentence = sb
            .artifactKey(new ArtifactKey("testArtifactKey"))
            .sentenceData(sdb.build())
            .sentenceMetadata(smb.build())
            .sentenceTerm(stb.build())
            .build();

    SentenceTerm sentenceTerm = new ArrayList<SentenceTerm>(sentence.getSentenceTerm()).get(0);
    assertEquals("a q khan\u001FCTA\u001FPERSON", sentenceTerm.getFamilyName());

    // todo: assert more fields are equal
  }
}
