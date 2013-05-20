package com.altamiracorp.reddawn.textExtraction;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import com.altamiracorp.reddawn.ucd.inputFormats.UCDArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactContent;
import com.altamiracorp.reddawn.ucd.models.ArtifactGenericMetadata;
import com.altamiracorp.reddawn.ucd.outputFormats.UCDOutputFormat;

public class TextExtractionMR extends UcdCommandLineBase implements Tool {
  private Class<TextExtractor> textExtractorClass;

  public static class TextExtractorMapper extends Mapper<Text, Artifact, Text, Mutation> {
    public static final String CONF_TEXT_EXTRACTOR_CLASS = "textExtractorClass";
    private TextExtractor textExtractor;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      try {
        textExtractor = (TextExtractor) context.getConfiguration().getClass(CONF_TEXT_EXTRACTOR_CLASS, AsciiTextExtractor.class).newInstance();
        textExtractor.setup(context);
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
        mutation.put(ArtifactGenericMetadata.COLUMN_FAMILY_NAME, ArtifactGenericMetadata.COLUMN_MIME_TYPE, extractedInfo.getMediaType());
        context.write(new Text(Artifact.TABLE_NAME), mutation);
      } catch (Exception e) {
        throw new IOException(e);
      }
    }

    public static void init(Job job, Class<? extends TextExtractor> textExtractorClass) {
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
  protected Options getOptions() {
    Options options = super.getOptions();

    options.addOption(
        OptionBuilder
            .withArgName("c")
            .withLongOpt("classname")
            .withDescription("The class that implements TextExtractor")
            .withArgName("name")
            .isRequired()
            .hasArg()
            .create()
    );

    return options;
  }

  @Override
  protected void processOptions(CommandLine cmd) {
    super.processOptions(cmd);

    String textExtractorClassName = cmd.getOptionValue("classname");
    if (textExtractorClassName == null) {
      throw new RuntimeException("'class' parameter is required");
    }
    textExtractorClass = loadClass(textExtractorClassName);
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
    TextExtractorMapper.init(job, textExtractorClass);

    job.setNumReduceTasks(0);

    job.setOutputFormatClass(UCDOutputFormat.class);
    UCDOutputFormat.init(job, getUsername(), getPassword(), getZookeeperInstanceName(), getZookeeperServerNames(), null);

    job.waitForCompletion(true);
    return job.isSuccessful() ? 0 : 1;
  }
}

