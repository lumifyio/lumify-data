package com.altamiracorp.reddawn.ucd.inputFormats;

import org.apache.accumulo.core.security.Authorizations;
import org.apache.hadoop.mapreduce.Job;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

@RunWith(JUnit4.class)
public class UCDArtifactInputFormatTest {
  @Test
  public void test() throws IOException {
    Job job = new Job();
    UCDArtifactInputFormat.init(job, "username", "password".getBytes(), new Authorizations("U"), "zooInstanceName", "zoo1, zoo2");
    // TODO how do we test input formats. MRUnit doesn't appear to support this
  }
}
