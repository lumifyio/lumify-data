package com.altamiracorp.reddawn.vaast.model.averageFrames;

import com.altamiracorp.reddawn.model.AccumuloBaseInputFormat;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloAverageFrameInputFormat extends AccumuloBaseInputFormat<AverageFrame, AverageFrameRepository> {
    private AverageFrameRepository averageFrameRepository = new AverageFrameRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, AverageFrame.TABLE_NAME, authorizations);
    }

    @Override
    public AverageFrameRepository getRepository() {
        return averageFrameRepository;
    }
}
