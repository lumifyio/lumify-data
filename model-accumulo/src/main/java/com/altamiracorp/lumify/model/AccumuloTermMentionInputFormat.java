package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionRepository;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloTermMentionInputFormat extends AccumuloBaseInputFormat<TermMention, TermMentionRepository> {
    private TermMentionRepository termMentionRepository = new TermMentionRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, TermMention.TABLE_NAME, authorizations);
    }

    @Override
    public TermMentionRepository getRepository() {
        return termMentionRepository;
    }
}
