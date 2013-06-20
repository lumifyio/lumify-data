package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class FileImport extends RedDawnCommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImport.class.getName());
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private String directory;
    private String pattern;
    private String source;

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
        if (cmd.hasOption("source")) {
            this.source = cmd.getOptionValue("source");
        } else {
            this.source = "File Import";
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

        options.addOption(
                OptionBuilder
                        .withArgName("s")
                        .withLongOpt("source")
                        .withDescription("The name of the source")
                        .withArgName("sourceName")
                        .create()
        );

        return options;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        File directory = new File(getDirectory());
        String pattern = getPattern();

        RedDawnSession redDawnSession = createRedDawnSession();
        redDawnSession.getModelSession().initializeTables();

        IOFileFilter fileFilter = new WildcardFileFilter(pattern);
        IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
        Iterator<File> fileIterator = FileUtils.iterateFiles(directory, fileFilter, directoryFilter);

        while (fileIterator.hasNext()) {
            File f = fileIterator.next();
            if (f.isFile()) {
                writeFile(redDawnSession, f);
            }
        }

        redDawnSession.close();
        return 0;
    }

    private void writeFile(RedDawnSession redDawnSession, File file) throws IOException, MutationsRejectedException {
        byte[] data = FileUtils.readFileToByteArray(file);

        Artifact artifact = new Artifact();
        artifact.getContent()
                .setSecurity("U") // TODO configurable?
                .setDocArtifactBytes(data);
        artifact.getGenericMetadata()
                .setFileName(FilenameUtils.getBaseName(file.getName()))
                .setFileExtension(FilenameUtils.getExtension(file.getName()))
                .setFileSize((long) data.length)
                .setFileTimestamp(file.lastModified())
                .setSource(this.source);

        LOGGER.info("Writing artifact: " + artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension() + " (rowId: " + artifact.getRowKey().toString() + ")");
        artifactRepository.save(redDawnSession.getModelSession(), artifact);
    }

    public String getDirectory() {
        return directory;
    }

    public String getPattern() {
        return pattern;
    }
}
