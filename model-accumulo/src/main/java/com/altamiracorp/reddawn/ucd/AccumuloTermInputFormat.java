package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.ucd.term.Term;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloTermInputFormat extends AccumuloBaseInputFormat<Term, TermRepository> {
    private TermRepository termRepository = new TermRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, Term.TABLE_NAME, authorizations);
    }

    @Override
    public TermRepository getRepository() {
        return termRepository;
    }
}
