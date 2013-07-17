package com.altamiracorp.reddawn.ucd;

import com.altamiracorp.reddawn.model.AccumuloBaseInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import org.apache.accumulo.core.client.mapreduce.AccumuloInputFormat;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;

public class AccumuloArtifactInputFormat extends AccumuloBaseInputFormat<Artifact, ArtifactRepository> {
    private ArtifactRepository artifactRepository = new ArtifactRepository();

    public static void init(Job job, String username, byte[] password, Authorizations authorizations, String zookeeperInstanceName, String zookeeperServerNames) {
        AccumuloInputFormat.setZooKeeperInstance(job.getConfiguration(), zookeeperInstanceName, zookeeperServerNames);
        AccumuloInputFormat.setInputInfo(job.getConfiguration(), username, password, Artifact.TABLE_NAME, authorizations);
    }

    @Override
    public ArtifactRepository getRepository() {
        return artifactRepository;
    }
}
