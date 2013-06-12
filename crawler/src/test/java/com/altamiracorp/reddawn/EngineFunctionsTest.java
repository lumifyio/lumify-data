package com.altamiracorp.reddawn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(JUnit4.class)

public class EngineFunctionsTest {
	@Test
	public void testConcatenateEmpty() throws Exception {
		ArrayList<String> arrayList = new ArrayList<String>();
		String result = EngineFunctions.concatenate(arrayList, "+");
		assertEquals("", result);
	}

	@Test
	public void testConcatenateSingleItem() throws Exception {
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add("Jeff");
		String result = EngineFunctions.concatenate(arrayList, "+");
		assertEquals("Jeff", result);
	}

	@Test
	public void testConcatenateMultipleItems() throws Exception {
		ArrayList<String> arrayList = new ArrayList<String>();
		arrayList.add("Jeff");
		arrayList.add("Sam");
		String result = EngineFunctions.concatenate(arrayList, "+");
		assertEquals("Jeff+Sam", result);
	}

	@Test
	public void testCreateQueryStringEmpty() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		String result = EngineFunctions.createQueryString(map);
		assertEquals("", result);
	}

	@Test
	public void testCreateQueryStringSingleItem() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put("bestIntern", "jeff");
		String result = EngineFunctions.createQueryString(map);
		assertEquals("&bestIntern=jeff", result);
	}

	@Test
	public void testCreateQueryStringMultipleItems() throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		map.put("bestIntern", "jeff");
		map.put("favoriteIntern", "sam");
		String result = EngineFunctions.createQueryString(map);
		assertEquals("&bestIntern=jeff&favoriteIntern=sam", result);
	}

	@Test
	public void testGetWebpageNormal() throws Exception {
		String result = EngineFunctions.getWebpage("http://www.google.com");
		assertNotNull(result);
	}

	@Test
	public void testGetWebpageInvalidUrl() throws Exception {
		String result = EngineFunctions.getWebpage("http://www.google.com/as;dlfijas%20;ldigjb");
		assertNull(result);
	}

	@Test
	public void testParseRSSNormal() throws Exception {
		ArrayList<String> results =  EngineFunctions.parseRSS(new URL("http://rss.cnn.com/rss/cnn_world.rss"), 10);
		assertEquals(10, results.size());
	}

	@Test
	public void testParseRSSInvalidUrl() throws Exception {
		ArrayList<String> results =  EngineFunctions.parseRSS(new URL("http://www.google.com"), 10);
		assertNull(results);
	}
}
