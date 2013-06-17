package com.altamiracorp.reddawn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
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

}
