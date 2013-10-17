package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.core.user.ModelAuthorizations;
import com.altamiracorp.lumify.core.model.termMention.TermMention;
import com.altamiracorp.lumify.core.model.termMention.TermMentionBuilder;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloTermMentionInputFormat extends AccumuloBaseInputFormat<TermMention, TermMentionBuilder> {
    private TermMentionBuilder termMentionBuilder = new TermMentionBuilder();

    public static void init(Job job, String username, String password, ModelAuthorizations modelAuthorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        Authorizations authorizations = ((AccumuloModelAuthorizations) modelAuthorizations).getAuthorizations();
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password.getBytes(), TermMention.TABLE_NAME, authorizations);
    }

    @Override
    public TermMentionBuilder getBuilder() {
        return termMentionBuilder;
    }
}
