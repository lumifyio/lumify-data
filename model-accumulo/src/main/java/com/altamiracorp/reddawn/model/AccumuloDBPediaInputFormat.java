package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.dbpedia.DBPedia;
import com.altamiracorp.reddawn.model.dbpedia.DBPediaRepository;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloDBPediaInputFormat extends AccumuloBaseInputFormat<DBPedia, DBPediaRepository> {
    private DBPediaRepository dbPediaRepository = new DBPediaRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, DBPedia.TABLE_NAME, authorizations);
    }

    @Override
    public DBPediaRepository getRepository() {
        return dbPediaRepository;
    }
}
