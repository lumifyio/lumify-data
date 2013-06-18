package com.altamiracorp.reddawn;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.StringList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;

public class DictionaryEncoder {

    private String filename = "newDictionary.dict";
    private String directoryPath = getCurrentDirectory();
    private Dictionary dictionary = new Dictionary();
    private String tokenizerModelLocation = "/Users/swoloszy/Documents/NIC/red-dawn/dictionary-seed/src/test/en-token.bin";
    private String tokenizerModelLocationJeff = "/Users/jprincip/Documents/nic/red-dawn/dictionary-seed/src/test/en-token.bin";

    public DictionaryEncoder() {
    }

    public DictionaryEncoder(String directoryPath) {
        this();
        File theDir = new File(directoryPath);
        if (!theDir.exists()) {
            boolean created = theDir.mkdir();
            if (created) {
                System.out.println("Directory " + directoryPath + " created");
            }
        }
        this.directoryPath = directoryPath;
    }

    public DictionaryEncoder(String directoryPath, String initialFilename) {
        this(directoryPath);
        filename = initialFilename;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
        // TODO check if exists and add if not
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void addEntries(String allEntries) {
        String[] entries = getEntries(allEntries);
        for (String entry : entries) {
            System.out.println("Adding...");
            dictionary.put(new StringList(tokenize(entry)));
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

    protected String[] tokenize(String entry) {
        InputStream modelIn = null;
        try {
            // TODO: not hardcode this
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
        Tokenizer tokenizer = new TokenizerME(model);
        return tokenizer.tokenize(entry);
    }

    protected String[] getEntries(String entries) {
        String[] items = entries.split("\n");
        return items;
    }

}
