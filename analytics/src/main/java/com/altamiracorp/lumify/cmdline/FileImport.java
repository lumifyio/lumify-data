package com.altamiracorp.lumify.cmdline;

import com.altamiracorp.lumify.AppSession;
import com.altamiracorp.lumify.FileImporter;
import com.google.inject.Inject;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FileImport extends CommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImport.class.getName());
    private String directory;
    private String pattern;
    private String source;
    private static String[] arguments;
    private boolean downloadZip;
    private String zipfile;
    private FileImporter fileImporter;

    public static void main(String[] args) throws Exception {
        arguments = args;
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new FileImport(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);
        this.directory = cmd.getOptionValue("directory");
        if (this.directory == null) throw new RuntimeException("No directory provided to FileImport");
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
        if (cmd.hasOption("zipfile")) {
            this.downloadZip = true;
            this.zipfile = (String) cmd.getArgList().get(0);
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

        options.addOption(
                OptionBuilder
                        .withArgName("z")
                        .withLongOpt("zipfile")
                        .withDescription("The zip files to download and extract")
                        .withArgName("zipfile")
                        .create()
        );

        return options;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        File directory = new File(getDirectory());
        if (getZipfile() != null && !getZipfile().contains("import")) {
            String dirZip = getDirectory() + "/" + getZipfile();
            if (getZipfile().contains("/")) {
                dirZip = getZipfile();
                this.directory = getZipfile();
                arguments[0] = getZipfile();
                if (getZipfile().lastIndexOf("/") == getZipfile().length() - 1) {
                    this.zipfile = getZipfile().substring(getZipfile().lastIndexOf("/", (getZipfile().length() - 2)));
                } else {
                    this.zipfile = getZipfile().substring((getZipfile().lastIndexOf("/") + 1));
                }
                arguments[1] = this.zipfile;
                getDataset(arguments);
            }
            if (getDownloadZip() && datasetExists(dirZip)) {
                this.directory = dirZip;
                directory = new File(getDirectory());
            } else if (!datasetExists(dirZip) || getDownloadZip()) {
                getDataset(arguments);
                this.directory = dirZip;
                directory = new File(getDirectory());
            }
        }
        String pattern = getPattern();
        AppSession session = createSession();
        session.getModelSession().initializeTables(getUser());

        fileImporter.writeDirectory(directory, pattern, source, getUser());

        session.close();
        return 0;
    }

    public String getDirectory() {
        return directory;
    }

    public String getPattern() {
        return pattern;
    }

    public String getSource() {
        return source;
    }

    public String getZipfile() {
        return zipfile;
    }

    public Boolean getDownloadZip() {
        return downloadZip;
    }

    private void getDataset(String[] args) {
        try {
            int res = ToolRunner.run(CachedConfiguration.getInstance(), new DownloadAndExtractFile(), args);
            if (res != 0) {
                LOGGER.info("Error pulling dataset from AWS");
                System.exit(res);
            }
        } catch (Exception e) {
            LOGGER.info("Error pulling dataset from AWS");
            e.printStackTrace();
        }
    }

    private Boolean datasetExists(String dir) {
        File file = new File(dir);
        return (file.exists() || (dir.equals("sample/directory")));
    }

    @Inject
    public void setFileImporter(FileImporter fileImporter) {
        this.fileImporter = fileImporter;
    }
}
