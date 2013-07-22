package com.altamiracorp.reddawn.dbpedia;

import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.cmdline.RedDawnCommandLineBase;
import com.altamiracorp.reddawn.model.RowKeyHelper;
import com.altamiracorp.reddawn.model.Session;
import com.altamiracorp.reddawn.model.dbpedia.DBPedia;
import com.altamiracorp.reddawn.model.dbpedia.DBPediaRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.util.ToolRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBPediaImport extends RedDawnCommandLineBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBPediaImport.class.getName());
    private List<File> inFiles;
    private List<String> limitTerms = null;
    private DBPediaRepository dbPediaRepository = new DBPediaRepository();
    private int flushCount = 1000;
    private Object outputLock = new Object();

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new DBPediaImport(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);
        if (!cmd.hasOption("in")) {
            throw new RuntimeException("in is required");
        }
        File inFile = new File(cmd.getOptionValue("in"));
        if (!inFile.exists()) {
            throw new RuntimeException("in file does not exist");
        }
        if (inFile.isDirectory()) {
            inFiles = getDbpediaFiles(inFile);
        } else {
            inFiles = new ArrayList<File>();
            inFiles.add(inFile);
        }

        if (cmd.hasOption("wikidir")) {
            String wikidir = cmd.getOptionValue("wikidir");
            limitTerms = getLimitTerms(new File(wikidir));
        }

        if (cmd.hasOption("flushCount")) {
            flushCount = Integer.parseInt(cmd.getOptionValue("flushCount"));
        }
    }

    private List<File> getDbpediaFiles(File inFile) {
        ArrayList<File> result = new ArrayList<File>();
        for (File f : inFile.listFiles()) {
            if (f.getName().endsWith(".nt")) {
                result.add(f);
            }
        }
        return result;
    }

    private List<String> getLimitTerms(File wikidir) {
        ArrayList<String> result = new ArrayList<String>();
        for (File f : wikidir.listFiles()) {
            if (f.getName().endsWith(".xml")) {
                String fname = FilenameUtils.removeExtension(f.getName());
                result.add(fname);
            }
        }
        return result;
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withLongOpt("in")
                        .withDescription("The DBPedia input file")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("filename")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("wikidir")
                        .withDescription("A directory containing wikipedia page files to limit import")
                        .hasArg(true)
                        .withArgName("directory")
                        .create()
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("flushCount")
                        .withDescription("Number of entries to buffer before flushing")
                        .hasArg(true)
                        .withArgName("count")
                        .create()
        );

        return options;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        RedDawnSession session = createRedDawnSession();
        session.getModelSession().initializeTables();

        for (File inFile : inFiles) {
            run(session, inFile);
        }

        return 0;
    }

    private void run(RedDawnSession session, File inFile) throws IOException {
        String inFileName = inFile.getName();
        DBPediaReader dbpediaReader = null;
        if (inFileName.contains("geo_coordinates")) {
            dbpediaReader = new GeoCoordinatesReader();
        } else if (inFileName.contains("images")) {
            dbpediaReader = new ImagesReader();
        } else if (inFileName.contains("instance_types")) {
            dbpediaReader = new InstanceTypesReader();
        } else if (inFileName.contains("labels")) {
            dbpediaReader = new LabelsReader();
        } else if (inFileName.contains("specific_mappingbased_properties")) {
            dbpediaReader = new SpecificMappingBasedPropertiesReader();
        } else if (inFileName.contains("mappingbased_properties")) {
            dbpediaReader = new MappingBasedPropertiesReader();
        } else if (inFileName.contains("wikipedia_links")) {
            dbpediaReader = new WikipediaLinksReader();
        }

        if (dbpediaReader == null) {
            System.err.println("Unhandled DBPedia file: " + inFileName);
        } else {
            dbpediaReader.read(session.getModelSession(), inFile, this.limitTerms);
        }
    }

    private abstract class DBPediaReader {
        private Date startTime;

        public void read(Session session, File inFile, List<String> limitTerms) throws IOException {
            FileInputStream in = new FileInputStream(inFile);
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                int lineCount = 0;
                int writtenLineCount = 0;
                String line;
                ArrayList<DBPedia> dbpedias = new ArrayList<DBPedia>();
                startTime = new Date();
                while ((line = reader.readLine()) != null) {
                    try {
                        lineCount++;
                        if ((lineCount % 100) == 0) {
                            printProgressBar(lineCount, writtenLineCount);
                        }
                        if (line.startsWith("#")) {
                            continue;
                        }
                        if (limitTerms != null && !hasLimitTerm(limitTerms, line)) {
                            continue;
                        }
                        String[] lineParts = splitLine(lineCount, line);
                        DBPedia dbpedia = readLine(lineParts);
                        if (dbpedia != null) {
                            dbpedias.add(dbpedia);
                            writtenLineCount++;
                            if (dbpedias.size() > flushCount) {
                                dbPediaRepository.saveMany(session, dbpedias);
                                dbpedias.clear();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                printProgressBar(lineCount, writtenLineCount);
                System.out.println();

                try {
                    if (dbpedias.size() > 0) {
                        dbPediaRepository.saveMany(session, dbpedias);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            } finally {
                in.close();
            }
        }

        private void printProgressBar(int lineCount, int writtenLineCount) {
            long time = new Date().getTime() - startTime.getTime();
            double percentComplete = ((double) lineCount) / ((double) getTotalLineCount());
            int totalBarLength = 50;
            int completeBarLength = (int) (percentComplete * (double) totalBarLength);
            int toWorkOnBarLength = totalBarLength - completeBarLength;
            double rate = (double) lineCount / (double) time * 1000.0;
            synchronized (outputLock) {
                System.out.print('\r');
                for (int i = 0; i < completeBarLength; i++) {
                    System.out.print('=');
                }
                if (completeBarLength != totalBarLength) {
                    System.out.print('>');
                }
                for (int i = 0; i < toWorkOnBarLength; i++) {
                    System.out.print(' ');
                }
                String name = getClass().getSimpleName();
                System.out.printf("%s: %.1f%%  %d/%d %.1f pages/s (found: %d)", name, percentComplete * 100.0, lineCount, getTotalLineCount(), rate, writtenLineCount);
            }
        }

        protected abstract int getTotalLineCount();

        private String[] splitLine(int lineNumber, String line) {
            ArrayList<String> parts = new ArrayList<String>();
            char[] chars = line.toCharArray();
            for (int i = 0; i < chars.length; ) {
                if (Character.isWhitespace(chars[i]) || chars[i] == '.') {
                    i++;
                } else if (chars[i] == '<') {
                    i++;
                    StringBuilder part = new StringBuilder();
                    for (; i < chars.length; i++) {
                        if (chars[i] == '>') {
                            i++;
                            break;
                        }
                        part.append(chars[i]);
                    }
                    parts.add(part.toString());
                } else if (chars[i] == '"') {
                    i++;
                    StringBuilder part = new StringBuilder();
                    for (; i < chars.length; i++) {
                        if (chars[i] == '\\') {
                            i++;
                        } else if (chars[i] == '"') {
                            i++;
                            break;
                        }
                        part.append(chars[i]);
                    }
                    parts.add(part.toString());

                    // skip this possible "@en" part
                    while (!Character.isWhitespace(chars[i])) {
                        i++;
                    }
                } else {
                    System.out.println("Unexpected character (line " + lineNumber + "): " + chars[i]);
                    i++;
                }
            }
            return parts.toArray(new String[0]);
        }

        private boolean hasLimitTerm(List<String> limitTerms, String line) {
            for (String limitTerm : limitTerms) {
                if (line.startsWith("<http://dbpedia.org/resource/" + limitTerm + ">")) {
                    return true;
                }
            }
            return false;
        }

        protected abstract DBPedia readLine(String[] lineParts);
    }

    private class GeoCoordinatesReader extends DBPediaReader {
        @Override
        protected int getTotalLineCount() {
            return 1900006;
        }

        @Override
        protected DBPedia readLine(String[] lineParts) {
            DBPedia dbpedia = new DBPedia(lineParts[0]);
            dbpedia.getGeoCoordinates().set(lineParts[1], lineParts[2]);
            return dbpedia;
        }
    }

    private class ImagesReader extends DBPediaReader {
        @Override
        protected int getTotalLineCount() {
            return 7370587;
        }

        @Override
        protected DBPedia readLine(String[] lineParts) {
            DBPedia dbpedia = new DBPedia(lineParts[0]);
            dbpedia.getImage().set(lineParts[1], lineParts[2]);
            return dbpedia;
        }
    }

    private class InstanceTypesReader extends DBPediaReader {
        @Override
        protected int getTotalLineCount() {
            return 13225167;
        }

        @Override
        protected DBPedia readLine(String[] lineParts) {
            DBPedia dbpedia = new DBPedia(lineParts[0]);
            dbpedia.getInstanceTypes().set(RowKeyHelper.buildMinor(lineParts[1], lineParts[2]), lineParts[2]);
            return dbpedia;
        }
    }

    private class LabelsReader extends DBPediaReader {
        @Override
        protected int getTotalLineCount() {
            return 9442540;
        }

        @Override
        protected DBPedia readLine(String[] lineParts) {
            DBPedia dbpedia = new DBPedia(lineParts[0]);
            dbpedia.getLabel().setLabel(lineParts[2]);
            return dbpedia;
        }
    }

    private class SpecificMappingBasedPropertiesReader extends DBPediaReader {
        @Override
        protected int getTotalLineCount() {
            return 635835;
        }

        @Override
        protected DBPedia readLine(String[] lineParts) {
            DBPedia dbpedia = new DBPedia(lineParts[0]);
            dbpedia.getSpecificMappingBasedProperties().set(lineParts[1], lineParts[2]);
            return dbpedia;
        }
    }

    private class MappingBasedPropertiesReader extends DBPediaReader {
        @Override
        protected int getTotalLineCount() {
            return 20516861;
        }

        @Override
        protected DBPedia readLine(String[] lineParts) {
            DBPedia dbpedia = new DBPedia(lineParts[0]);
            dbpedia.getMappingBasedProperties().set(lineParts[1], lineParts[2]);
            return dbpedia;
        }
    }

    private class WikipediaLinksReader extends DBPediaReader {
        @Override
        protected int getTotalLineCount() {
            return 28327616;
        }

        @Override
        protected DBPedia readLine(String[] lineParts) {
            if (!lineParts[0].startsWith("http://dbpedia.org")) {
                return null;
            }
            DBPedia dbpedia = new DBPedia(lineParts[0]);
            if (lineParts[1].equals("http://xmlns.com/foaf/0.1/isPrimaryTopicOf")) {
                dbpedia.getWikipediaLinks().setUrl(lineParts[2]);
            }
            dbpedia.getWikipediaLinks().set(lineParts[1], lineParts[2]);
            return dbpedia;
        }
    }
}
