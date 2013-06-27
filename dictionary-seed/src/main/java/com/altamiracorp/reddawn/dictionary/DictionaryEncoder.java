package com.altamiracorp.reddawn.dictionary;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class DictionaryEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryEncoder.class.getName());

    private String filename = "newDictionary.dict";
    private String directoryPath;
    protected Tokenizer tokenizer;
    private StringBuilder currentEntries = new StringBuilder();

    public DictionaryEncoder(String directoryPath) {
        InputStream modelIn = null;
        TokenizerModel model = null;

        try {
            modelIn = new FileInputStream(System.getProperty("user.dir") +
                    "/conf/opennlp/en-token.bin");
            model = new TokenizerModel(modelIn);
        } catch (Exception e) {
            LOGGER.error("Tokenizer model file cannot be found.");
            throw new RuntimeException(e);
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                    LOGGER.warn("The tokenizer model could not be closed");
                }
            }
        }
        tokenizer = new TokenizerME(model);
        setDirectoryPath(directoryPath);
    }

    public DictionaryEncoder(String directoryPath, String initialFilename) {
        this(directoryPath);
        this.filename = initialFilename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setDirectoryPath(String directoryPath) {
        File theDir = new File(directoryPath);
        if (!theDir.exists()) {
            boolean created = theDir.mkdir();
            if (created) {
                LOGGER.info("Directory " + directoryPath + " created");
            } else {
                throw new RuntimeException("Problem creating directory");
            }
        }
        this.directoryPath = directoryPath;
    }


    public void addEntries(String[] entries) {
        currentEntries = new StringBuilder();
        for (String entry : entries) {
            addTaggedTokenizedEntry(entry);
        }
        appendCurrentEntriesToFile();
    }

    private void addTaggedTokenizedEntry(String entry) {
        String[] tokens = tokenizer.tokenize(entry);
        for (int i = 0; i < tokens.length; i++) {
            if(i != 0) {
                currentEntries.append('\t');
            }
            currentEntries.append(tokens[i]);
        }
        currentEntries.append("\n");
    }

    private void appendCurrentEntriesToFile() {
        File file = new File(directoryPath + "/" + filename);
        FileWriter fout = null;
        try {
            fout = new FileWriter(file, true);
            fout.append(currentEntries);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                fout.flush();
                fout.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Wrote entries to dictionary.");

    }
}
