package com.altamiracorp.reddawn.dictionary;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class DictionaryEncoder {

    private String filename = "newDictionary.dict";
    private String directoryPath;
    protected Tokenizer tokenizer;
    private StringBuilder currentEntries = new StringBuilder();
    private String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private String dictionaryRootElementOpen = "<dictionary case_sensitive=\"false\">\n";
    private boolean fileIsOpen = false;
    private String dictionaryRootElementClose = "</dictionary>";

    public DictionaryEncoder(String directoryPath) {
        InputStream modelIn;

        try {
            modelIn = getClass().getResourceAsStream("en-token.bin");
        } catch (Exception e) {
            throw new RuntimeException("Problem reading tokenizer model.");
        }
        TokenizerModel model = null;
        try {
            model = new TokenizerModel(modelIn);
        } catch (IOException e) {
            throw new RuntimeException("Problem creating tokenizer with model.");
        } finally {
            if (modelIn != null) {
                try {
                    modelIn.close();
                } catch (IOException e) {
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
            fout.write(xmlHeader);
            fout.write(dictionaryRootElementOpen);
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
            fout.write(dictionaryRootElementClose);
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
        currentEntries.append("<entry>\n");
        for (String s : tokenizer.tokenize(entry)) {
            currentEntries.append("<token>" + s + "</token>\n");
        }
        currentEntries.append("</entry>\n");
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
