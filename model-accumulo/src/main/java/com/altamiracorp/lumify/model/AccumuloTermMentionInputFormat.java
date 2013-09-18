package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.model.termMention.TermMention;
import com.altamiracorp.lumify.model.termMention.TermMentionBuilder;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloTermMentionInputFormat extends AccumuloBaseInputFormat<TermMention, TermMentionBuilder> {
    private TermMentionBuilder termMentionBuilder = new TermMentionBuilder();

    public static void init(Job job, String username, String password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password.getBytes(), TermMention.TABLE_NAME, authorizations);
    }

    @Override
    public TermMentionBuilder getBuilder() {
        return termMentionBuilder;
    }
}
