package com.altamiracorp.reddawn.dictionary;

import com.altamiracorp.reddawn.dictionary.DictionaryEncoder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class DictionaryEncoderTest {

    DictionaryEncoder encoder = null;
    String[] sampleEntries1 = {"This is a sample entry"};
    String[] sampleEntries2 = {"first entry", "second entry", "third entry", "fourth entry", "fifth entry", "sixth entry"};
    String[] sampleEntries3 = {"seventh entry", "eighth entry", "ninth entry"};
    String testDirectory = "dictionaryFiles";

    @Before
    public void setUp() throws Exception {
        encoder = new DictionaryEncoder(testDirectory);
    }

    @Test
    public void testTokenizeNormal() throws FileNotFoundException {
        String sampleEntry = "Sam Woloszynski";
        String[] results = encoder.tokenizer.tokenize(sampleEntry);
        assertEquals("Sam", results[0]);
        assertEquals("Woloszynski", results[1]);
    }

    @Test
    public void testTokenizerNormal2() throws FileNotFoundException {
        String sampleEntry = "Jeff Principe";
        String[] results = encoder.tokenizer.tokenize(sampleEntry);
        assertEquals("Jeff", results[0]);
        assertEquals("Principe", results[1]);
    }

    @Test
    public void testTokenizeThreeNames() throws FileNotFoundException {
        String sampleEntry = "One Two Three";
        String[] results = encoder.tokenizer.tokenize(sampleEntry);
        assertEquals("One", results[0]);
        assertEquals("Two", results[1]);
        assertEquals("Three", results[2]);
    }

    @Test
    public void testTokenizePunctuation() throws FileNotFoundException {
        String sampleEntry = "Mr. Bob";
        String[] results = encoder.tokenizer.tokenize(sampleEntry);
        assertEquals("Mr.", results[0]);
        assertEquals("Bob", results[1]);
    }

    @Test
    public void testTokenizeLongNameWithPunctuation() throws FileNotFoundException {
        String sampleEntry = "Dr. Patrick L. O'Malley III";
        String[] results = encoder.tokenizer.tokenize(sampleEntry);
        assertEquals("Dr.", results[0]);
        assertEquals("Patrick", results[1]);
        assertEquals("L.", results[2]);
        //TODO SIMPLE TOKENIZER BREAKS TOKENS ON EVERY PUNCTUATION, INCLUDING INTERNAL
        assertEquals("O'Malley", results[3]);
        assertEquals("III", results[4]);
    }

    @Test
    public void testTokenizeJapaneseName() throws FileNotFoundException {
        String sampleEntry = "肥後橋";
        String[] results = encoder.tokenizer.tokenize(sampleEntry);
        assertEquals("肥後橋", results[0]);
    }

    @Test
    public void testTokenizeJapaneseNameUnicode() throws FileNotFoundException {
        String sampleEntry = "\\u05E2\\u05D1\\u05D0\\u05E1 \\u05E1\\u05D5\\u05D0\\u05DF";
        String[] results = encoder.tokenizer.tokenize(sampleEntry);
        assertEquals("\\u05E2\\u05D1\\u05D0\\u05E1", results[0]);
        assertEquals("\\u05E1\\u05D5\\u05D0\\u05DF", results[1]);
    }

    @Test
    public void testGetCurrentDirectory() {
        String result = encoder.getCurrentDirectory();
        assertEquals("/Users/swoloszy/Documents/NIC/red-dawn", result);
    }

    @Test
    public void testAddEntriesSingleEntry() throws Exception {
        encoder.addEntries(sampleEntries1);
    }

    @Test
    public void testAddEntriesNonexistentDirectory() throws Exception {
        DictionaryEncoder encoder1 = new DictionaryEncoder(testDirectory, "testAddEntriesNonexistentDirectory.dict");
        encoder1.addEntries(sampleEntries1);
    }

    @Test
    public void testAddEntriesMultiple() throws Exception {
        encoder.addEntries(sampleEntries2);
    }

    @Test
    public void testAddEntriesMultipleTimes() throws Exception {
        encoder.addEntries(sampleEntries2);
        encoder.addEntries(sampleEntries3);
    }

    @Test
    public void testInitializeDoc() {
        DictionaryEncoder encoder2 = new DictionaryEncoder(testDirectory);
        String testFilename = "/testDictionaryInit.dict";
        encoder2.initializeDictionaryFile(testFilename);
        File file = new File(testFilename);
        Scanner fin = null;
        try {
            fin = new Scanner(new FileReader(encoder.getCurrentDirectory() + "/testDictionaryDir" + testFilename));
            assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", fin.nextLine());
            assertEquals("<dictionary case_sensitive=\"false\">", fin.nextLine());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Problem reading file");
        } finally {
            fin.close();
        }
    }

    @Test
    public void testCloseFile() {
        String testFilename = "/testDictionaryClose.dict";

        DictionaryEncoder encoder2 = new DictionaryEncoder(testDirectory);
        String testPathName = encoder2.getCurrentDirectory() + "/" + testDirectory + testFilename;

        encoder2.initializeDictionaryFile(testFilename);
        encoder2.closeFile();
        File file = new File(testFilename);
        Scanner fin = null;
        try {
            fin = new Scanner(new FileReader(testPathName));
            fin.nextLine(); //burn xml header
            fin.nextLine(); //burn dictionary open tag
            assertEquals("</dictionary>", fin.nextLine());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Problem reading file");
        } finally {
            fin.close();
        }

    }

    @Test
    public void testAddEntries() {
        String testFilename = "/testDictionaryAddEntries.dict";
        DictionaryEncoder encoder3 = new DictionaryEncoder(testDirectory);

        String testPathName = testDirectory + testFilename;
        System.out.println(testPathName);
        encoder3.initializeDictionaryFile(testFilename);
        encoder3.addEntries(sampleEntries3);
        encoder3.closeFile();
        File file = new File(testFilename);
        Scanner fin = null;
        try {
            fin = new Scanner(new FileReader(testPathName));
            fin.nextLine(); //burn xml header
            fin.nextLine(); //burn dictionary open tag
            assertEquals("<entry>", fin.nextLine());
            assertEquals("<token>seventh</token>", fin.nextLine());
            assertEquals("<token>entry</token>", fin.nextLine());
            assertEquals("</entry>", fin.nextLine());
            assertEquals("<entry>", fin.nextLine());
            assertEquals("<token>eighth</token>", fin.nextLine());
            assertEquals("<token>entry</token>", fin.nextLine());
            assertEquals("</entry>", fin.nextLine());
            assertEquals("<entry>", fin.nextLine());
            assertEquals("<token>ninth</token>", fin.nextLine());
            assertEquals("<token>entry</token>", fin.nextLine());
            assertEquals("</entry>", fin.nextLine());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Problem reading file");
        } finally {
            fin.close();
        }
    }

    @After
    public void tearDown() {
        File thingToDelete = new File(testDirectory);
        thingToDelete.delete();
    }

}