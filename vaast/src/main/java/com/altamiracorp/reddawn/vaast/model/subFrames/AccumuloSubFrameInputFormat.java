package com.altamiracorp.reddawn.vaast.model.subFrames;

import com.altamiracorp.reddawn.model.AccumuloBaseInputFormat;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloSubFrameInputFormat extends AccumuloBaseInputFormat<SubFrame, SubFrameRepository> {

    private SubFrameRepository averageFrameRepository = new SubFrameRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, SubFrame.TABLE_NAME, authorizations);
    }

    @Override
    public SubFrameRepository getRepository() {
        return averageFrameRepository;
    }
}
