package com.altamiracorp.reddawn.mrIntegrationTests;

import com.altamiracorp.reddawn.model.AccumuloQueryUser;
import com.altamiracorp.reddawn.model.AccumuloSession;
import com.altamiracorp.reddawn.model.ColumnFamily;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import com.altamiracorp.reddawn.ucd.sentence.SentenceTerm;
import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.accumulo.core.client.Connector;
import org.apache.accumulo.core.client.ZooKeeperInstance;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
@Ignore
public class MrIntegrationTests {
    @Test
    public void testArtifactCounts() throws AccumuloSecurityException, AccumuloException {
        Session session = createModelSession();
        ArtifactRepository artifactRepository = new ArtifactRepository();
        List<Artifact> rows = artifactRepository.findAll(session);
        assertEquals(233, rows.size());
    }

    @Test
    public void testSentenceCounts() throws AccumuloSecurityException, AccumuloException {
        Session session = createModelSession();
        SentenceRepository sentenceRepository = new SentenceRepository();
        List<Sentence> rows = sentenceRepository.findAll(session);
        assertEquals(13096, rows.size());
        Sentence firstSentence = rows.get(0);
        assertEquals("By Julie Christie, Parenting.com", firstSentence.getData().getText());
        assertEquals((Long) 0L, firstSentence.getData().getStart());
        assertEquals((Long) 32L, firstSentence.getData().getEnd());
        assertEquals("urn\u001Fsha256\u001F0014f02ae81e32c72318b785c4f8cd3993f97d94492e30934bdc9c29f5a7a1d8:0000000000000032:0000000000000000", firstSentence.getRowKey().toString());
        assertTrue(containsTerm(firstSentence, "julie christie\u001FOpenNlpMaximumEntropy\u001Fperson"));
    }

    private Boolean containsTerm(Sentence sentence, String term) {
        for (ColumnFamily columnFamily : sentence.getColumnFamilies()) {
            if (columnFamily.getColumnFamilyName().toString().startsWith("urn")) {
                String termId = columnFamily.get(SentenceTerm.TERM_ID).toString();
                if (term.equals(termId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void testTermCounts() throws AccumuloSecurityException, AccumuloException {
        Session session = createModelSession();
        TermRepository termRepository = new TermRepository();
        List<Term> rows = termRepository.findAll(session);
        assertEquals(0, rows.size());
    }

    private Session createModelSession() throws AccumuloException, AccumuloSecurityException {
        String zookeeperInstanceName = "reddawn";
        String zookeeperServerName = "192.168.33.10";
        String username = "root";
        String password = "reddawn";
        ZooKeeperInstance zooKeeperInstance = new ZooKeeperInstance(zookeeperInstanceName, zookeeperServerName);
        Connector connector = zooKeeperInstance.getConnector(username, password);

        AccumuloQueryUser queryUser = new AccumuloQueryUser();
        return new AccumuloSession(connector, queryUser);
    }
}
