package io.lumify.dictionary;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DictionaryEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryEncoder.class.getName());
    private final int COMMON_WORD_LIMIT = 1000; // Sets how many of the most common words in the english language to filter out

    private String filename = "newDictionary.dict";
    private String directoryPath;
    protected Tokenizer tokenizer;
    private StringBuilder currentEntries = new StringBuilder();
    private List<String> commonWords = new ArrayList<String>();

    public DictionaryEncoder(String directoryPath) {
        InputStream modelIn = null;
        TokenizerModel model = null;

        try {
            modelIn = getClass().getResourceAsStream("/en-token.bin");
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

        buildCommonWordList();
    }

    private void buildCommonWordList() {
        BufferedReader commonWordReader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/most-common-words.txt")));
        try {
            String nextWord = commonWordReader.readLine();
            for(int i = 0; i < COMMON_WORD_LIMIT && nextWord != null; i++) {
                commonWords.add(nextWord.toLowerCase());
                nextWord = commonWordReader.readLine();
            }
        } catch (IOException e) {
            LOGGER.error("Could not read in common english words dictionary. Skipping...");
        }
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
        String trimmedEntry = trimEntry(entry);
        if(isCommonWord(trimmedEntry) || !hasAlphabetCharacters(trimmedEntry)) {
            return;
        }

        String[] tokens = tokenizer.tokenize(trimmedEntry);
        for (int i = 0; i < tokens.length; i++) {
            if(i != 0) {
                currentEntries.append(' ');
            }
            currentEntries.append(tokens[i]);
        }
        currentEntries.append("\n");
    }

    private String trimEntry(String entry) {
        if(entry.charAt(entry.length() - 1) == ')') {
            for(int i = entry.length() - 1; i >= 0; i--) {
                if(entry.charAt(i) == '(') {
                    return entry.substring(0, i).trim();
                }
            }
        }

        return entry.trim();
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

    private boolean isCommonWord(String dictionaryEntry) {
        return commonWords.contains(dictionaryEntry.toLowerCase());

    }

    private boolean hasAlphabetCharacters(String dictionaryEntry) {
        return !dictionaryEntry.matches("[^a-zA-Z]+");
    }
}
