package com.altamiracorp.reddawn.ucd.model.Sentence;

import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.model.sentence.SentenceData;
import com.altamiracorp.reddawn.ucd.model.sentence.SentenceMetadata;
import com.altamiracorp.reddawn.ucd.model.sentence.SentenceTerm;
import com.altamiracorp.reddawn.ucd.model.sentence.SentenceKey;
import com.altamiracorp.reddawn.ucd.model.sentence.SentenceTermId;
import com.altamiracorp.reddawn.ucd.model.terms.TermKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SentenceTest {
  @Test
  public void createASource() {
    Sentence.Builder sb = Sentence.newBuilder();

    SentenceData.Builder sdb = SentenceData.Builder.newBuilder();

    sdb.artifactId("artifactId");
    sdb.start(5L);
    sdb.end(25L);
    sdb.text("The quick brown fox.");

    SentenceMetadata.Builder smb = SentenceMetadata.newBuilder();

    SentenceTerm.Builder stb = SentenceTerm.newBuilder();

    // todo: add more fields

    TermKey termKey = TermKey.newBuilder().sign("a q khan")
            .model("CTA")
            .concept("PERSON")
            .build();

    stb.termKey(termKey);

    SentenceKey sentenceKey = SentenceKey
            .Builder
            .newBuilder()
            .artifactKey(new ArtifactKey("urn:sha256:007d14d3"))
            .start(271)
            .end(553)
            .build();

    SentenceTermId termId = SentenceTermId.Builder.newBuilder().termId(termKey).termColumnFamilyHash("ColumnFamily").build();
    List<SentenceTermId> termIds = new ArrayList<SentenceTermId>();
    termIds.add(termId);

    Sentence sentence = sb
            .sentenceKey(sentenceKey)
            .sentenceData(sdb.build())
            .sentenceMetadata(smb.build())
            .termIds(termIds)
            .build();

    assertEquals("urn:sha256:007d14d3:271:553", sentence.getSentenceKey().toString());

    SentenceTermId sentenceTermId = sentence.getTermIds().get(0);
    assertEquals("a q khan\u001FCTA\u001FPERSON", sentenceTermId.getTermId().toString());
    assertEquals("ColumnFamily", sentenceTermId.getTermColumnFamilyHash());

    assertEquals("artifactId", sentence.getSentenceData().getArtifactId());
    assertEquals((Object) 5L, sentence.getSentenceData().getStart());
    assertEquals((Object) 25L, sentence.getSentenceData().getEnd());
    assertEquals("The quick brown fox.", sentence.getSentenceData().getText());
  }
}
