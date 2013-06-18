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
	public void setUp() {
		encoder = new DictionaryEncoder();
	}

	@Test
	public void testRun() throws Exception {

	}

	@Test
	public void testGetEntries() {
		String filename = "/Users/swoloszy/Documents/NIC/red-dawn/dictionary-seed/src/test/testItems.txt";
		ArrayList<String> items = null;
		try {
			items = encoder.getEntities(filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(7, items.size());
		assertTrue(items.get(0).equals("This is a sentence."));
		assertTrue(items.get(1).equals("The tokenizer should break this sentence into an array of words."));
		assertTrue(items.get(2).equals("Apple Pie"));
		assertTrue(items.get(3).equals("Rainy Day"));
		assertTrue(items.get(4).equals("Hello Goodbye"));
		assertTrue(items.get(5).equals("Sam Woloszynski"));
		assertTrue(items.get(6).equals("Jeff Principe"));
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
	public void testGetTokenizedEntryNormal() throws IOException {
        String sampleEntry = "Sam Woloszynski";
        StringBuilder result = encoder.getTokenizedEntry(sampleEntry);
        assertEquals("\t<entry>\n" +
                "\t\t<token>Sam</token>\n" +
                "\t\t<token>Woloszynski</token>\n" +
                "\t</entry>", result.toString());
	}

    @Test
    public void testGetTokenizedEntryLongName() throws IOException {
        String sampleEntry = "Dr. Patrick L. O'Malley III";
        StringBuilder result = encoder.getTokenizedEntry(sampleEntry);
        assertEquals("\t<entry>\n" +
                "\t\t<token>Dr.</token>\n" +
                "\t\t<token>Patrick</token>\n" +
                "\t\t<token>L.</token>\n" +
                "\t\t<token>O'Malley</token>\n" +
                "\t\t<token>III</token>\n" +
                "\t</entry>", result.toString());
    }

}
