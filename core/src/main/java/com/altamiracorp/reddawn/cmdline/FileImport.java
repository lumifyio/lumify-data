package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.ucd.AuthorizationLabel;
import com.altamiracorp.reddawn.ucd.UcdClient;
import com.altamiracorp.reddawn.ucd.models.Artifact;
import com.altamiracorp.reddawn.ucd.models.ArtifactContent;
import com.altamiracorp.reddawn.ucd.models.ArtifactGenericMetadata;
import org.apache.accumulo.core.client.BatchWriter;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class FileImport extends UcdCommandLineBase {
  private String directory;
  private String pattern;

  public static void main(String[] args) throws Exception {
    int res = ToolRunner.run(CachedConfiguration.getInstance(), new FileImport(), args);
    if (res != 0) {
      System.exit(res);
    }
  }

  @Override
  protected void processOptions(CommandLine cmd) {
    super.processOptions(cmd);
    this.directory = cmd.getOptionValue("directory");
    if (cmd.hasOption("pattern")) {
      this.pattern = cmd.getOptionValue("pattern");
    } else {
      this.pattern = "*";
    }
  }

  @Override
  protected Options getOptions() {
    Options options = super.getOptions();

    options.addOption(
        OptionBuilder
            .withArgName("d")
            .withLongOpt("directory")
            .withDescription("The directory to import")
            .isRequired()
            .hasArg(true)
            .withArgName("path")
            .create()
    );

    options.addOption(
        OptionBuilder
            .withArgName("p")
            .withLongOpt("pattern")
            .withDescription("The pattern to match files against")
            .withArgName("pattern")
            .create()
    );

    return options;
  }

  @Override
  protected int run(CommandLine cmd) throws Exception {
    long memBuf = 1000000L; // bytes to store before sending a batch
    long timeout = 1000L; // milliseconds to wait before sending
    int numThreads = 10;
    File directory = new File(getDirectory());
    String pattern = getPattern();

    UcdClient<AuthorizationLabel> client = createUcdClient();
    client.initializeTables();
    BatchWriter writer = client.getConnection().createBatchWriter(Artifact.TABLE_NAME, memBuf, timeout, numThreads);

    IOFileFilter fileFilter = new WildcardFileFilter(pattern);
    IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
    Iterator<File> fileIterator = FileUtils.iterateFiles(directory, fileFilter, directoryFilter);

    while (fileIterator.hasNext()) {
      File f = fileIterator.next();
      if (f.isFile()) {
        writeFile(writer, f);
      }
    }

    writer.close();
    client.close();
    return 0;
  }

  private void writeFile(BatchWriter writer, File file) throws IOException, MutationsRejectedException {
    byte[] data = FileUtils.readFileToByteArray(file);

    ArtifactContent artifactContent = ArtifactContent.newBuilder()
        .security("U") // TODO configurable?
        .docArtifactBytes(data)
        .build();
    ArtifactGenericMetadata artifactGenericMetadata = ArtifactGenericMetadata.newBuilder()
        .fileName(FilenameUtils.getBaseName(file.getName()))
        .fileExtension(FilenameUtils.getExtension(file.getName()))
        .fileSize(data.length)
        .fileTimestamp(file.lastModified())
        .build();
    Artifact artifact = Artifact.newBuilder()
        .artifactContent(artifactContent)
        .artifactGenericMetadata(artifactGenericMetadata)
        .build();
    System.out.println("Writing artifact: " + artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension() + " (rowId: " + artifact.getKey().toString() + ")");
    writer.addMutation(artifact.getMutation());
  }

  public String getDirectory() {
    return directory;
  }

  public String getPattern() {
    return pattern;
  }
}
