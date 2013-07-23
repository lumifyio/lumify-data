package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.SaveFileResults;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileImport extends RedDawnCommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileImport.class.getName());
    private static final long MAX_SIZE_OF_INLINE_FILE = 1 * 1024 * 1024; // 1MiB
    private static final String MAPPING_JSON_FILE_NAME_SUFFIX = ".mapping.json";
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
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);
        this.directory = cmd.getOptionValue("directory");
        System.out.println("\n\n\n\n Directory: " + this.directory + "\n\n\n\n");
        File file = new File(directory);
        if (!file.exists()) {
            int second= directory.lastIndexOf('/', (directory.length() - 3));
            int last= directory.lastIndexOf('/');
            String set = directory.substring(second);
            if(last == (directory.length()-1))  {
                set = directory.substring(second, last);
            }
            String dataset =  set + ".zip";
            downloadDataset(dataset);
            extractDataset(dataset);
        }

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
        if (!directory.exists()) {
            int second= getDirectory().lastIndexOf('/', (getDirectory().length() - 3));
            String set;
            if(getDirectory().charAt((getDirectory().length()-1)) == '/')  {
                int last= getDirectory().lastIndexOf('/');
                set = getDirectory().substring(second, last);
            } else {
                set = getDirectory().substring(second);
                this.directory += "/";
            }
            String dataset =  set + ".zip";
            downloadDataset(dataset);
            extractDataset(dataset);
            directory = new File(set);
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

    private void downloadDataset(String dataset){
        try{
            String amazon = "https://s3.amazonaws.com/RedDawn/DataSets";
            String aws = amazon + dataset;

            URL awsset = new URL(aws);
            URLConnection connect = awsset.openConnection();
            //perform login information

            InputStream in = connect.getInputStream();

            String repo = this.directory;       //repo for download
            File file = new File(repo);
            file.mkdirs();
            FileOutputStream out = new FileOutputStream(repo + dataset);

            byte[] outStream = new byte[4096];
            int count;
            while((count = in.read(outStream)) >= 0){
                out.write(outStream, 0, count);
            }

            in.close();
            out.close();

        } catch(IOException e) {
            LOGGER.info("Error pulling dataset from AWS");
            e.printStackTrace();
        }
    }

    private void extractDataset(String dataset){
        try{
            String storedRepo;
            String repo;
           if(directory.charAt(directory.length()-1) != '/'){
                storedRepo =this.directory + "/";
                String set = dataset.substring(1, (dataset.length()-4));
                repo = this.directory + "/unzip_" + set + "/";        //repo for extraction from download
            }
            else {
                storedRepo =this.directory;
                String set = dataset.substring(1, (dataset.length()-4));
                repo = this.directory + "unzip_" + set + "/";        //repo for extraction from download
            }

            this.directory = repo;

            String zipString = storedRepo.substring(0, (storedRepo.length()-1)) + dataset;
            byte[] buf = new byte[1024];

            ZipEntry zipentry;
            File file = new File(repo);
            file.mkdirs();
            File file2 = new File(storedRepo);
            file2.mkdirs();
            ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(zipString));

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null) {
                //for each entry to be extracted
                String entryName = repo + zipentry.getName();
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);
                System.out.println("entryname " + entryName);
                int n;
                FileOutputStream fileoutputstream;
                File newFile = new File(entryName);
                if (zipentry.isDirectory()) {
                    if (!newFile.mkdirs()) {
                        break;
                    }
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                fileoutputstream = new FileOutputStream(entryName);

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1) {
                    fileoutputstream.write(buf, 0, n);
                }

                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }
        } catch(Exception e){
            LOGGER.info("Error in extracting zip file");
            e.printStackTrace();
        }
    }
}
