package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.artifact.ArtifactRowKey;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactType;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRowKey;

public class BaseExtractorTest {

    protected Sentence createSentence (String text) {
        ArtifactRowKey artifactRowKey = ArtifactRowKey.build(text.getBytes());
        SentenceRowKey sentenceRowKey = new SentenceRowKey(artifactRowKey.toString(), 100, 200);
        Sentence sentence = new Sentence(sentenceRowKey);
        sentence.getData().setArtifactId(artifactRowKey.toString());
        sentence.getData().setText(text);
        sentence.getData().setStart(100L);
        sentence.getData().setEnd(200L);
        sentence.getMetadata().setArtifactSubject("testSubject");
        sentence.getMetadata().setArtifactType(ArtifactType.DOCUMENT);

        return sentence;
    }
}
