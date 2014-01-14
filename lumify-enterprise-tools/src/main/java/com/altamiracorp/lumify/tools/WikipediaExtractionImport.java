package com.altamiracorp.lumify.tools;

import com.altamiracorp.bigtable.model.FlushFlag;
import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.google.inject.Inject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class WikipediaExtractionImport extends CommandLineBase {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(WikipediaExtractionImport.class);
    private WorkQueueRepository workQueueRepository;

    public WikipediaExtractionImport() {
        initFramework = true;
    }

    private static LineData processLine(int lineNumber, String line) {
        String[] tabSeparatedValues = line.split("\t");
        if (tabSeparatedValues.length < 5) {
            throw new InvalidLineException(lineNumber, line, "line has < 5 fields");
        }

        LineData lineData = new LineData();
        lineData.title = tabSeparatedValues[1];
        lineData.body = tabSeparatedValues[4].replaceAll("\\\\n", "\n");
        lineData.filename = lineData.title.replaceAll("[^A-Za-z0-9]", "_");
        return lineData;
    }

    private static class LineData {
        public String title;
        public String body;
        public String filename;
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withLongOpt("outqueue")
                        .withDescription("The output queue")
                        .hasArg(true)
                        .withArgName("queue")
                        .create("q")
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("infile")
                        .withDescription("The input filename")
                        .hasArg(true)
                        .withArgName("filename")
                        .create("i")
        );

        return options;
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        String outQueue = cmd.getOptionValue("outqueue");
        String infile = cmd.getOptionValue("infile");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(infile)));
        try {
            int lineNumber = 0;
            int successfulLines = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if ((lineNumber % 1000) == 0) {
                    printProgress(lineNumber, successfulLines);
                }
                try {
                    LineData lineData = processLine(lineNumber, line);
                    JSONObject json = new JSONObject();
                    json.put("fileName", lineData.filename);
                    json.put("title", lineData.title);
                    json.put("source", "Wikipedia");
                    json.put("raw", lineData.body);
                    workQueueRepository.pushOnQueue(outQueue, FlushFlag.NO_FLUSH, json);
                    successfulLines++;
                } catch (InvalidLineException ex) {
                    LOGGER.error("Could not process line " + ex.getLineNumber() + ": " + ex.getMessage());
                } catch (Exception ex) {
                    LOGGER.error("Could not process line " + lineNumber + ": " + line, ex);
                }
            }
            printProgress(lineNumber, successfulLines);
            workQueueRepository.flush();
        } finally {
            reader.close();
        }

        return 0;
    }

    private void printProgress(int lineNumber, int successfulLines) {
        LOGGER.info("Sent %d documents to queue of total %d.", successfulLines, lineNumber);
    }

    @Inject
    public void setWorkQueueRepository(WorkQueueRepository workQueueRepository) {
        this.workQueueRepository = workQueueRepository;
    }

    public static void main(String[] args) throws Exception {
        int res = new WikipediaExtractionImport().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    private static class InvalidLineException extends RuntimeException {
        private int lineNumber;
        private String line;

        public InvalidLineException(int lineNumber, String line, String message) {
            super(message);
            this.lineNumber = lineNumber;
            this.line = line;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getLine() {
            return line;
        }
    }
}
