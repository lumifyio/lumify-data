package com.altamiracorp.reddawn.textExtraction;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import com.altamiracorp.reddawn.ucd.inputFormats.UCDArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactContent;
import com.altamiracorp.reddawn.ucd.models.ArtifactGenericMetadata;
import com.altamiracorp.reddawn.ucd.outputFormats.UCDOutputFormat;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TextExtractionMR extends UcdCommandLineBase implements Tool {
  public static class TextExtractorMapper extends Mapper<Text, Artifact, Text, Mutation> {
    public static final String CONF_TEXT_EXTRACTOR_CLASS = "textExtractorClass";
    private TextExtractor textExtractor;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      try {
        textExtractor = (TextExtractor) context.getConfiguration().getClass(CONF_TEXT_EXTRACTOR_CLASS, AsciiTextExtractor.class).newInstance();
      } catch (InstantiationException e) {
        throw new IOException(e);
      } catch (IllegalAccessException e) {
        throw new IOException(e);
      }
    }

    public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
      try {
        ExtractedInfo extractedInfo = textExtractor.extract(new ByteArrayInputStream(artifact.getContent().getDocArtifactBytes()));
        Mutation mutation = new Mutation(artifact.getKey().toString());
        mutation.put(ArtifactContent.COLUMN_FAMILY_NAME, ArtifactContent.COLUMN_DOC_EXTRACTED_TEXT, extractedInfo.getText());
        mutation.put(ArtifactGenericMetadata.COLUMN_FAMILY_NAME, ArtifactGenericMetadata.COLUMN_SUBJECT, extractedInfo.getSubject());
        context.write(new Text(Artifact.TABLE_NAME), mutation);
      } catch (Exception e) {
        throw new IOException(e);
      }
    }

    public static void init(Job job, Class<AsciiTextExtractor> textExtractorClass) {
      job.getConfiguration().setClass(CONF_TEXT_EXTRACTOR_CLASS, textExtractorClass, TextExtractor.class);
    }
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(CachedConfiguration.getInstance(), new TextExtractionMR(), args);
    if (res != 0) {
      System.exit(res);
    }
  }

  @Override
  protected int run(CommandLine cmd) throws Exception {
    Job job = new Job(getConf(), this.getClass().getSimpleName());
    job.setJarByClass(this.getClass());

    job.setInputFormatClass(UCDArtifactInputFormat.class);
    UCDArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());

    job.setMapperClass(TextExtractorMapper.class);
    job.setMapOutputKeyClass(Key.class);
    job.setMapOutputValueClass(Value.class);
    TextExtractorMapper.init(job, AsciiTextExtractor.class); // TODO change this to be configurable

    job.setNumReduceTasks(0);

    job.setOutputFormatClass(UCDOutputFormat.class);
    UCDOutputFormat.init(job, getUsername(), getPassword(), getZookeeperInstanceName(), getZookeeperServerNames(), null);

    job.waitForCompletion(true);
    return job.isSuccessful() ? 0 : 1;
  }
}

