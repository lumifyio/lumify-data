package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.model.AccumuloBaseInputFormat;
import com.altamiracorp.reddawn.ucd.sentence.Sentence;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloSentenceInputFormat extends AccumuloBaseInputFormat<Sentence, SentenceRepository> {
    private SentenceRepository sentenceRepository = new SentenceRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, Sentence.TABLE_NAME, authorizations);
    }

    @Override
    public SentenceRepository getRepository() {
        return sentenceRepository;
    }
}
