package com.altamiracorp.reddawn;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.StringList;

import java.io.*;

public class DictionaryEncoder {

    private String filename = "newDictionary.dict";
    private String directoryPath = getCurrentDirectory();
    private Dictionary dictionary = new Dictionary();
    protected Tokenizer tokenizer;
    private String tokenizerModelLocation = getCurrentDirectory()  + "/dictionary-seed/src/en-token.bin";
    private String tokenizerModelLocationSam = "/Users/swoloszy/Documents/NIC/red-dawn/dictionary-seed/src/en-token.bin";
    private String tokenizerModelLocationJeff = "/Users/jprincip/Documents/nic/red-dawn/dictionary-seed/src/test/en-token.bin";

    public DictionaryEncoder() {
        InputStream modelIn = null;
        try {
            modelIn = new FileInputStream(tokenizerModelLocation);
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
        filename = initialFilename;
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void addEntries(String allEntries) {
        String[] entries = getEntries(allEntries);
        for (String entry : entries) {
            System.out.println("Adding...");
            dictionary.put(new StringList(tokenizer.tokenize(entry)));
        }
        writeToFile();
    }

    private void writeToFile() {
        File file = new File(directoryPath + "/" + filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            dictionary.serialize(out);
        } catch (IOException e) {
            throw new RuntimeException("Problem writing to file " + file.getAbsolutePath());
        }
        System.out.println("Writing to file...");
    }

    protected String getCurrentDirectory() {
        return System.getProperty("user.dir");
    }

    protected String[] getEntries(String entries) {
        String[] items = entries.split("\n");
        return items;
    }

}
