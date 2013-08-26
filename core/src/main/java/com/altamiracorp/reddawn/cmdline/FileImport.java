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
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.hadoop.util.ToolRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class FileImport extends RedDawnCommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImport.class.getName());
    private static final String MAPPING_JSON_FILE_NAME_SUFFIX = ".mapping.json";
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private String directory;
    private String pattern;
    private String source;
    private static String[] arguments;
    private boolean downloadZip;
    private String zipfile;

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
        if (getDownloadZip() && datasetExists(getDirectory() + "/" + getZipfile())) {
            this.directory = getDirectory() + "/" + getZipfile();
            directory = new File(getDirectory());
        } else if ((!datasetExists(getDirectory() + "/" + getZipfile()) || getDownloadZip()) && !getZipfile().contains("import")/*&& !directory.exists()*/) {
            getDataset(arguments);
            this.directory = getDirectory() + "/" + getZipfile();
            directory = new File(getDirectory());
        }
        String pattern = getPattern();
        RedDawnSession redDawnSession = createRedDawnSession();
        redDawnSession.getModelSession().initializeTables();

        IOFileFilter fileFilter = new WildcardFileFilter(pattern);
        IOFileFilter directoryFilter = TrueFileFilter.INSTANCE;
        Iterator<File> fileIterator = FileUtils.iterateFiles(directory, fileFilter, directoryFilter);

        while (fileIterator.hasNext()) {
            File f = fileIterator.next();
            if (f.isFile() && !f.getName().endsWith(MAPPING_JSON_FILE_NAME_SUFFIX)) {
                JSONObject mappingJson = readMappingJsonFile(f);
                writeFile(redDawnSession, f, mappingJson);
            }
        }

        redDawnSession.close();
        return 0;
    }

    private JSONObject readMappingJsonFile(File f) throws JSONException, IOException {
        File mappingJsonFile = new File(f.getAbsolutePath() + MAPPING_JSON_FILE_NAME_SUFFIX);
        JSONObject mappingJson = null;
        if (mappingJsonFile.exists()) {
            FileInputStream mappingJsonFileIn = new FileInputStream(mappingJsonFile);
            try {
                mappingJson = new JSONObject(IOUtils.toString(mappingJsonFileIn));
            } finally {
                mappingJsonFileIn.close();
            }
        }
        return mappingJson;
    }

    private void writeFile(RedDawnSession redDawnSession, File file, JSONObject mappingJson) throws IOException, MutationsRejectedException {
        if (file.getName().startsWith(".")) {
            return;
        }
        Artifact artifact = artifactRepository.createArtifactFromInputStream(
                redDawnSession.getModelSession(),
                file.length(),
                new FileInputStream(file),
                file.getName(),
                file.lastModified());
        artifact.getGenericMetadata().setSource(this.source);
        if (mappingJson != null) {
            artifact.getGenericMetadata().setMappingJson(mappingJson);
        }

        LOGGER.info("Writing artifact: " + artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension() + " (rowId: " + artifact.getRowKey().toString() + ")");
        artifactRepository.save(redDawnSession.getModelSession(), artifact);
        artifactRepository.saveToGraph(redDawnSession.getModelSession(), redDawnSession.getGraphSession(), artifact);
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
}
