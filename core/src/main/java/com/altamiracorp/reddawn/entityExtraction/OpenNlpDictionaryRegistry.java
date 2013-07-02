package com.altamiracorp.reddawn.entityExtraction;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.util.StringList;
import org.apache.commons.io.FilenameUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Keeping this code separate so that we can maybe access the dictionary files
 * from the "update" workflow. A little more reusable than keeping it in the
 * actual Dictionary entity extractor, but could probably use some work.
 *
 * @author rlanman
 */
public class OpenNlpDictionaryRegistry {

    private Map<String, Dictionary> dictionaries = new HashMap<String, Dictionary>();
    private final boolean CASE_SENSITIVE = true;

    public void loadRegistry(String pathPrefix, FileSystem fs)
            throws IOException {
        loadDictionaries(pathPrefix, fs);
    }

    public void loadDictionaries(String pathPrefix, FileSystem fs)
            throws IOException {
        Path hdfsDirectory = new Path(pathPrefix + "/conf/opennlp/dictionaries");
        for (FileStatus dictionaryFileStatus : fs.listStatus(hdfsDirectory)) {
            Path hdfsPath = dictionaryFileStatus.getPath();
            InputStream dictionaryInputStream = fs.open(hdfsPath);
            String type = FilenameUtils.getBaseName(hdfsPath.getName());
            try {
                dictionaries.put(type, buildDictionary(dictionaryInputStream, CASE_SENSITIVE));
            } finally {
                dictionaryInputStream.close();
            }
        }
    }

    protected Dictionary buildDictionary(InputStream dictionaryInputStream, boolean caseSensitive) throws IOException {
        Dictionary dictionary = new Dictionary(caseSensitive);
        BufferedReader dictionaryReader = new BufferedReader(new InputStreamReader(dictionaryInputStream));
        String line = dictionaryReader.readLine();
        while (line != null) {
            String[] tokens = line.split(" ");
            dictionary.put(new StringList(tokens));
            line = dictionaryReader.readLine();
        }
        return dictionary;
    }

    public Dictionary getSingleDictionary(String key) {
        return dictionaries.get(key);
    }

    public Collection<Entry<String, Dictionary>> getAllDictionaries() {
        return dictionaries.entrySet();
    }

}
