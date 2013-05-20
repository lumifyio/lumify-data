package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.cmdline.UcdCommandLineBase;
import com.altamiracorp.reddawn.ucd.inputFormats.UCDArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.Term;
import com.altamiracorp.reddawn.ucd.outputFormats.UCDOutputFormat;
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

import java.io.IOException;
import java.util.Collection;

public class EntityExtractionMR extends UcdCommandLineBase implements Tool {
  private Class<EntityExtractor> entityExtractorClass;

  public static class EntityExtractorMapper extends Mapper<Text, Artifact, Text, Mutation> {
    public static final String CONF_ENTITY_EXTRACTOR_CLASS = "entityExtractorClass";
    private EntityExtractor entityExtractor;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
      super.setup(context);
      try {
        entityExtractor = (EntityExtractor) context.getConfiguration().getClass(CONF_ENTITY_EXTRACTOR_CLASS, NullEntityExtractor.class).newInstance();
      } catch (InstantiationException e) {
        throw new IOException(e);
      } catch (IllegalAccessException e) {
        throw new IOException(e);
      }
    }

    public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
      try {
        Collection<Term> terms = extractEntities(artifact);
        writeEntities(context, terms);
      } catch (Exception e) {
        throw new IOException(e);
      }
    }

    private void writeEntities(Context context, Collection<Term> terms) throws IOException, InterruptedException {
      for (Term term : terms) {
        context.write(new Text(Term.TABLE_NAME), term.getMutation());
      }
    }

    private Collection<Term> extractEntities(Artifact artifact) throws Exception {
      String artifactKey = artifact.getKey().toString();
      String text = artifact.getContent().getDocExtractedText();
      return entityExtractor.extract(artifactKey, text);
    }

    public static void init(Job job, Class<? extends EntityExtractor> entityExtractor) {
      job.getConfiguration().setClass(CONF_ENTITY_EXTRACTOR_CLASS, entityExtractor, EntityExtractor.class);
    }
  }

  @Override
  protected Options getOptions() {
    Options options = super.getOptions();

    options.addOption(
        OptionBuilder
            .withArgName("c")
            .withLongOpt("classname")
            .withDescription("The class that implements EntityExtractor")
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
    entityExtractorClass = loadClass(textExtractorClassName);
  }

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(CachedConfiguration.getInstance(), new EntityExtractionMR(), args);
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

    job.setMapperClass(EntityExtractorMapper.class);
    job.setMapOutputKeyClass(Key.class);
    job.setMapOutputValueClass(Value.class);
    EntityExtractorMapper.init(job, entityExtractorClass);

    job.setNumReduceTasks(0);

    job.setOutputFormatClass(UCDOutputFormat.class);
    UCDOutputFormat.init(job, getUsername(), getPassword(), getZookeeperInstanceName(), getZookeeperServerNames(), Term.TABLE_NAME);

    job.waitForCompletion(true);
    return job.isSuccessful() ? 0 : 1;
  }
}

