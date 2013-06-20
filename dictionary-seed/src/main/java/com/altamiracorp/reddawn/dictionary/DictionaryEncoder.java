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
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private static final String DICTIONARY_ROOT_ELEMENT_OPEN = "<dictionary case_sensitive=\"false\">\n";
    private boolean fileIsOpen = false;
    private static final String DICTIONARY_ROOT_ELEMENT_CLOSE = "</dictionary>";

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
            fout.write(XML_HEADER);
            fout.write(DICTIONARY_ROOT_ELEMENT_OPEN);
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
            fout.write(DICTIONARY_ROOT_ELEMENT_CLOSE);
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
        currentEntries.append("<entry>");
        for (String s : tokenizer.tokenize(entry)) {
            currentEntries.append("<token>" + s + "</token>");
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
