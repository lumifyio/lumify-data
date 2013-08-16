package com.altamiracorp.reddawn.entityExtraction;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.util.StringList;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class OpenNlpDictionaryRegistryTest {
    OpenNlpDictionaryRegistry dictionaryRegistry;

    @Before
    public  void setUp() {
        dictionaryRegistry = new OpenNlpDictionaryRegistry();
    }

    @Test
    public void testBuildDictionaryCaseSensitive() throws Exception {
        boolean caseSensitive = true;
        int expectedSize = 3;
        InputStream inputStream = new ByteArrayInputStream("Awesome Token\nawesome token\nhello world\n".getBytes());

        Dictionary dictionary = dictionaryRegistry.buildDictionary(inputStream, caseSensitive);

        assertTrue(dictionary.contains(new StringList("Awesome", "Token")));
        assertTrue(dictionary.contains(new StringList("awesome", "token")));
        assertTrue(dictionary.contains(new StringList("hello", "world")));
        assertFalse(dictionary.contains(new StringList("Hello", "World")));
        assertEquals(expectedSize, dictionary.asStringSet().size());
    }
}
