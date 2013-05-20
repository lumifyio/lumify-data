package com.altamiracorp.reddawn.textExtraction;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

import com.altamiracorp.reddawn.textExtraction.util.GenericDateExtractor;
import com.altamiracorp.reddawn.textExtraction.util.TikaMetadataUtils;

public class TikaTextExtractor implements TextExtractor {

	private static final String PROPS_FILE = "tika-extractor.properties";
	private static final String DATE_KEYS = "tika.extraction.datekeys";
	private static final String TITLE_KEYS = "tika.extraction.titlekeys";

	/**
	 * A collection of potential metadata keys for the publish date of a
	 * document
	 */
	private List<String> dateKeys;

	/**
	 * A collection of potential metadata keys for the title/subject of a
	 * document
	 */
	private List<String> titleKeys;

	public TikaTextExtractor() {
		// TODO: Create an actual properties class?
		Properties tikaProperties = new Properties();
		try {
			tikaProperties.load(Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(PROPS_FILE));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dateKeys = Arrays.asList(tikaProperties.getProperty(DATE_KEYS,
				"date,published").split(","));
		titleKeys = Arrays.asList(tikaProperties.getProperty(TITLE_KEYS,
				"title,subject").split(","));
	}

	@Override
	public ExtractedInfo extract(InputStream in) throws Exception {
		ExtractedInfo result = new ExtractedInfo();

		Parser parser = new AutoDetectParser();
		BodyContentHandler handler = new BodyContentHandler(10000000);
		Metadata metadata = new Metadata();
		ParseContext ctx = new ParseContext();
		parser.parse(in, handler, metadata, ctx);

		result.setText(handler.toString());

		// find the date metadata property, if there is one
		String dateKey = TikaMetadataUtils.findKey(dateKeys, metadata);
		Date date = null;
		if (dateKey != null) {
			date = GenericDateExtractor
					.extractSingleDate(metadata.get(dateKey));
		}

		if (date == null) {
			date = new Date();
		}
		result.setDate(date);

		// find the title metadata property, if there is one
		String titleKey = TikaMetadataUtils.findKey(titleKeys, metadata);

		if (titleKey != null) {
			result.setSubject(metadata.get(titleKey));
		}

		return result;
	}
}
