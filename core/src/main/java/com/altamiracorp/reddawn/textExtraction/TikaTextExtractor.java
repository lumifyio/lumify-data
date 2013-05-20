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

	private static final String MIME_TYPE_KEY = "Content-Type";

	private static final String PROPS_FILE = "tika-extractor.properties";
	private static final String DATE_KEYS_PROPERTY = "tika.extraction.datekeys";
	private static final String SUBJECT_KEYS_PROPERTY = "tika.extraction.titlekeys";

	/**
	 * A collection of potential metadata keys for the publish date of a
	 * document
	 */
	private List<String> dateKeys;

	/**
	 * A collection of potential metadata keys for the title/subject of a
	 * document
	 */
	private List<String> subjectKeys;

	public TikaTextExtractor() {
		// TODO: Create an actual properties class?
		Properties tikaProperties = new Properties();
		try {
			// don't require the properties file
			InputStream propsIn = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(PROPS_FILE);
			if (propsIn != null) {
				tikaProperties.load(propsIn);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dateKeys = Arrays.asList(tikaProperties.getProperty(DATE_KEYS_PROPERTY,
				"date,published").split(","));
		subjectKeys = Arrays.asList(tikaProperties.getProperty(
				SUBJECT_KEYS_PROPERTY, "title,subject").split(","));
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

		// since we are using the AutoDetectParser, it is safe to assume that
		// the Content-Type metadata key will always return a value
		result.setMediaType(metadata.get(MIME_TYPE_KEY));
		
		result.setDate(extractDate(metadata));
		result.setSubject(extractSubject(metadata));
		
		return result;
	}

	private Date extractDate(Metadata metadata) {
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

		return date;
	}

	private String extractSubject(Metadata metadata) {
		// find the title metadata property, if there is one
		String title = "";
		String titleKey = TikaMetadataUtils.findKey(subjectKeys, metadata);

		if (titleKey != null) {
			title = metadata.get(titleKey);
		}

		return title;
	}
}
