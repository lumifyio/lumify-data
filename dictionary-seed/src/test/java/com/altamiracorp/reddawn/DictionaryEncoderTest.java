package com.altamiracorp.reddawn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class DictionaryEncoderTest {

	DictionaryEncoder encoder = null;

	@Before
	public void setUp() throws Exception{
		encoder = new DictionaryEncoder("blah");
	}

	@Test
	public void testRun() throws Exception {

	}

	@Test
	public void testGetEntries() {
		String sampleInput = "This is a sentence.\nThis is an entry.\nApple Pie\n" +
                "Rainy Day\nHello Goodbye\nSam Woloszynski\nJeff Principe";
		String[] items = null;
		try {
			items = encoder.getEntries(sampleInput);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(7, items.length);
		assertTrue(items[0].equals("This is a sentence."));
		assertTrue(items[1].equals("This is an entry."));
		assertTrue(items[2].equals("Apple Pie"));
		assertTrue(items[3].equals("Rainy Day"));
		assertTrue(items[4].equals("Hello Goodbye"));
		assertTrue(items[5].equals("Sam Woloszynski"));
		assertTrue(items[6].equals("Jeff Principe"));
	}

	@Test
	public void testTokenizeNormal() throws FileNotFoundException {
		String sampleEntry = "Sam Woloszynski";
		String[] results = encoder.tokenize(sampleEntry);
		assertEquals("Sam", results[0]);
		assertEquals("Woloszynski", results[1]);
	}

	@Test
	public void testTokenizerNormal2() throws FileNotFoundException {
		String sampleEntry = "Jeff Principe";
		String[] results = encoder.tokenize(sampleEntry);
		assertEquals("Jeff", results[0]);
		assertEquals("Principe", results[1]);
	}

	@Test
	public void testTokenizeThreeNames() throws FileNotFoundException {
		String sampleEntry = "One Two Three";
		String[] results = encoder.tokenize(sampleEntry);
		assertEquals("One", results[0]);
		assertEquals("Two", results[1]);
		assertEquals("Three", results[2]);
	}

	@Test
	public void testTokenizePunctuation() throws FileNotFoundException {
		String sampleEntry = "Mr. Bob";
		String[] results = encoder.tokenize(sampleEntry);
		assertEquals("Mr.", results[0]);
		assertEquals("Bob", results[1]);
	}

	@Test
	public void testTokenizeLongNameWithPunctuation() throws FileNotFoundException {
		String sampleEntry = "Dr. Patrick L. O'Malley III";
		String[] results = encoder.tokenize(sampleEntry);
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
		String[] results = encoder.tokenize(sampleEntry);
		assertEquals("肥後橋", results[0]);
	}

    @Test
	public void testTokenizeJapaneseNameUnicode() throws FileNotFoundException {
		String sampleEntry = "\\u05E2\\u05D1\\u05D0\\u05E1 \\u05E1\\u05D5\\u05D0\\u05DF";
		String[] results = encoder.tokenize(sampleEntry);
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
        encoder.addEntries("This is a sample entry");

    }

    @Test
    public void testAddEntriesNonexistentDirectory() throws Exception {
        DictionaryEncoder encoder1 = new DictionaryEncoder("dictionaryData", "myDictionary.dict");
        encoder1.addEntries("This is a sample entry");
    }

    @Test
    public void testAddEntriesMultiple() throws Exception {
        encoder.addEntries("First Entry\nSecond Entry\nThird Entry\n");
    }

    @Test
    public void testAddEntriesMultipleTimes() throws Exception {
        encoder.addEntries("First Entry\nSecond Entry\nThird Entry\n");
        encoder.addEntries("Fourth Entry\nFifth Entry\nSixth Entry\n");
    }
}
