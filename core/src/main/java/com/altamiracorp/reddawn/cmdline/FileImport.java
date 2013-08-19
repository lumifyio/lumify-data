package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.SaveFileResults;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.accumulo.core.client.MutationsRejectedException;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.hadoop.util.ToolRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

public class FileImport extends RedDawnCommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImport.class.getName());
    private static final long MAX_SIZE_OF_INLINE_FILE = 512 * 1024; // 512kiB
    private static final String MAPPING_JSON_FILE_NAME_SUFFIX = ".mapping.json";
    private ArtifactRepository artifactRepository = new ArtifactRepository();
    private String directory;
    private String pattern;
    private String source;
    private static String[] arguments;
    private Boolean downloadZip;
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
        if(cmd.hasOption("zipfile")){
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
        if (this.downloadZip && datasetExists(getDirectory() + "/" + zipfile)) {
            this.directory = this.directory + "/" + this.zipfile;
            directory = new File(getDirectory());
        } else if (!datasetExists(getDirectory()) || this.downloadZip) {
            getDataset(arguments);
            this.directory = this.directory + "/" + this.zipfile;
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
        Artifact artifact;
        if (file.getName().startsWith(".")) {
            return;
        }
        if (file.length() > MAX_SIZE_OF_INLINE_FILE) {
            FileInputStream fileInputStreamData = new FileInputStream(file);
            try {
                SaveFileResults saveResults = artifactRepository.saveFile(redDawnSession.getModelSession(), fileInputStreamData);
                artifact = new Artifact(saveResults.getRowKey());
                artifact.getGenericMetadata()
                        .setHdfsFilePath(saveResults.getFullPath())
                        .setFileSize(file.length());
            } finally {
                fileInputStreamData.close();
            }
        } else {
            artifact = new Artifact();
            byte[] data = FileUtils.readFileToByteArray(file);
            artifact.getContent().setDocArtifactBytes(data);
            artifact.getGenericMetadata().setFileSize((long) data.length);
        }

        artifact.getContent()
                .setSecurity("U"); // TODO configurable?
        artifact.getGenericMetadata()
                .setFileName(FilenameUtils.getBaseName(file.getName()))
                .setFileExtension(FilenameUtils.getExtension(file.getName()))
                .setFileTimestamp(file.lastModified())
                .setSource(this.source);
        if (mappingJson != null) {
            artifact.getGenericMetadata().setMappingJson(mappingJson);
        }

        LOGGER.info("Writing artifact: " + artifact.getGenericMetadata().getFileName() + "." + artifact.getGenericMetadata().getFileExtension() + " (rowId: " + artifact.getRowKey().toString() + ")");
        artifactRepository.save(redDawnSession.getModelSession(), artifact);
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
