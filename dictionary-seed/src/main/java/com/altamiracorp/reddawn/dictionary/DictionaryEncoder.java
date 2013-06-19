package com.altamiracorp.reddawn.dictionary;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;
import java.util.Timer;

public class DictionaryEncoder {

    private String filename = "newDictionary.dict";
    private String directoryPath = getCurrentDirectory();
    protected Tokenizer tokenizer;
    private String tokenizerModelLocation = getCurrentDirectory()  + "/dictionary-seed/src/en-token.bin";
    private String tokenizerModelLocationSam = "/Users/swoloszy/Documents/NIC/red-dawn/dictionary-seed/src/en-token.bin";
    private String tokenizerModelLocationJeff = "/Users/jprincip/Documents/nic/red-dawn/dictionary-seed/src/en-token.bin";
    protected StringBuilder currentEntries = new StringBuilder();
    private String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    private String dictionaryRootElementOpen = "<dictionary case_sensitive=\"false\">\n";
    private boolean fileIsOpen = false;
    private String dictionaryRootElementClose = "</dictionary>";
    private long totalTime = 0;

    public DictionaryEncoder() {
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream(tokenizerModelLocationSam);
        } catch (FileNotFoundException e) {
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
    }

    public DictionaryEncoder(String directoryPath) {
        this();
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
        try {
            FileWriter fout = new FileWriter(file);
            fout.write(xmlHeader);
            fout.write(dictionaryRootElementOpen);
        } catch (IOException e) {
            throw new RuntimeException("Problem writing to file " + file.getAbsolutePath());
        }
        System.out.println("DONE");
    }

    public void closeFile() {
        System.out.print("Closing dictionary file " + directoryPath + filename + "... ");
        File file = new File(directoryPath + "/" + filename);
        FileWriter fout = null;
        try {
            fout = new FileWriter(file);
            fout.write(dictionaryRootElementClose);
        } catch (IOException e) {
            throw new RuntimeException("Problem writing to file " + file.getAbsolutePath());
        }
        finally {
            try {
                fout.flush();
                fout.close();
            } catch (IOException e) {
                throw new RuntimeException("Problem closing file " + file.getAbsolutePath());
            }

        }
        System.out.println("DONE");

    }

    public void addEntries(String[] entries) {
        System.out.print("\t\t\t\tBuilding dictionary: ");

        currentEntries = new StringBuilder();
        long start = System.currentTimeMillis();
        for (String entry : entries) {
            addTaggedTokenizedEntry(entry);
        }

        long middle = System.currentTimeMillis();
        System.out.print((middle - start) + "ms\t\t");
        System.out.print("Writing dictionary: ");
        appendCurrentEntriesToFile();
        long end = System.currentTimeMillis();
        System.out.println((end - middle) + "ms");
        totalTime += (end-start);
        System.out.println("Total time: " + totalTime + "ms");
    }

    private void addTaggedTokenizedEntry(String entry) {
        currentEntries.append("<entry>\n");
        for (String s : tokenizer.tokenize(entry)) {
            currentEntries.append("<token>" + s + "</token>\n");
        }
        currentEntries.append("</entry>\n");
    }

    private void appendCurrentEntriesToFile() {
        System.out.print("Appending current batch to file... ");
        File file = new File(directoryPath + "/" + filename);
        try {
            FileWriter fout = new FileWriter(file);
            fout.append(currentEntries);
        } catch (IOException e) {
            throw new RuntimeException("Problem writing to file " + file.getAbsolutePath());
        }
        System.out.println("DONE");
    }

    protected String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }
}
