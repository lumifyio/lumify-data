package com.altamiracorp.reddawn.dictionary;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;

public class DictionaryEncoder {

    private String filename = "newDictionary.dict";
    private String directoryPath;
    protected Tokenizer tokenizer;
    private StringBuilder currentEntries = new StringBuilder();
    private boolean fileIsOpen = false;

    public DictionaryEncoder(String directoryPath) {
        InputStream modelIn = null;
        TokenizerModel model = null;

        try {
            modelIn = new FileInputStream(System.getProperty("user.dir") + "/../conf/opennlp/en-token.bin");
            model = new TokenizerModel(modelIn);
        } catch (Exception e) {
            throw new RuntimeException("Problem with tokenizer model.");
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
                    System.err.println("The tokenizer model could not be closed");
                }
            }
        }
        tokenizer = new TokenizerME(model);
        setDirectoryPath(directoryPath);
    }

    public DictionaryEncoder(String directoryPath, String initialFilename) {
        this(directoryPath);
        initializeDictionaryFile(initialFilename);
    }

    public void setDirectoryPath(String directoryPath) {
        File theDir = new File(directoryPath);
        if (!theDir.exists()) {
            boolean created = theDir.mkdir();
            if (created) {
                System.out.println("Directory " + directoryPath + " created");
            } else {
                throw new RuntimeException("Problem creating directory");
            }
        }
        this.directoryPath = directoryPath;
    }

    public void initializeDictionaryFile(String filename) {
        if (fileIsOpen) {
            closeFile();
        }
        fileIsOpen = true;
        this.filename = filename;
        System.out.print("Initializing dictionary file " + directoryPath + filename + "... ");
        File file = new File(directoryPath + "/" + filename);
        FileWriter fout = null;
        try {
            fout = new FileWriter(file);
        } catch (IOException e) {
            throw new RuntimeException("Problem writing to file " + file.getAbsolutePath());
        } finally {
            try {
                fout.flush();
                fout.close();
            } catch (IOException e) {
                throw new RuntimeException("Problem closing file " + file.getAbsolutePath());
            }

        }
        System.out.println("DONE");
    }

    public void closeFile() {
        System.out.print("Closing dictionary file " + directoryPath + filename + "... ");
        File file = new File(directoryPath + "/" + filename);
        FileWriter fout = null;
        try {
            fout = new FileWriter(file, true);
        } catch (IOException e) {
            throw new RuntimeException("Problem writing to file " + file.getAbsolutePath());
        } finally {
            try {
                fout.flush();
                fout.close();
            } catch (IOException e) {
                throw new RuntimeException("Problem closing file " + file.getAbsolutePath());
            }
        }
        fileIsOpen = false;
        System.out.println("DONE");
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
            throw new RuntimeException("Problem writing to file " + file.getAbsolutePath());
        } finally {
            try {
                fout.flush();
                fout.close();
            } catch (IOException e) {
                throw new RuntimeException("Problem closing file " + file.getAbsolutePath());
            }
        }
        System.out.println("Wrote entries to dictionary.");

    }
}
