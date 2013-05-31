package com.altamiracorp.reddawn.entityExtraction;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.DictionaryNameFinder;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.ucd.models.Term;

@RunWith(JUnit4.class)
public class OpenNlpDictionaryEntityExtractorTest {

	private OpenNlpDictionaryEntityExtractor extractor;
	private Context context;
	private String tokenizerModelFile = "en-token.bin";
	private Map<String, String> finderDictionaryFiles = new HashMap<String, String>();

	private String text = "This is a sentence that is going to tell you about a guy named "
			+ "Bob Robertson who lives in Boston, MA and works for a company called Altamira Corporation";

	@Before
	public void setUp() throws IOException {
		setUpDictionaryFiles();
		context = mock(Context.class);
		extractor = new OpenNlpDictionaryEntityExtractor() {

			@Override
			public void setup(Context context) throws IOException {
				setFinders(loadFinders());
				setTokenizer(loadTokenizer());
			}

			@Override
			protected List<TokenNameFinder> loadFinders() throws IOException {
				List<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
				for (Entry<String, String> finderDictionaryFileEntry : finderDictionaryFiles
						.entrySet()) {
					InputStream finderDictionaryIn = Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(
									finderDictionaryFileEntry.getValue());
					Dictionary finderDictionary = new Dictionary(
							finderDictionaryIn);
					finders.add(new DictionaryNameFinder(finderDictionary,
							finderDictionaryFileEntry.getKey()));
				}

				return finders;
			}

			@Override
			protected Tokenizer loadTokenizer() throws IOException {
				InputStream tokenizerModelIn = Thread.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(tokenizerModelFile);
				TokenizerModel tokenizerModel = new TokenizerModel(
						tokenizerModelIn);
				return new TokenizerME(tokenizerModel);
			}
		};
	}

	@Test
	public void testEntityExtraction() throws Exception {
		extractor.setup(context);
		ArtifactKey key = ArtifactKey.newBuilder()
				.docArtifactBytes(text.getBytes()).build();
		Collection<Term> terms = extractor.extract(key, text);
		List<String> extractedTerms = new ArrayList<String>();
		for (Term term : terms) {
			extractedTerms.add(term.getKey().getSign() + "-"
					+ term.getKey().getConcept());
		}
		validateOutput(extractedTerms);
	}

	private void validateOutput(List<String> terms) {
		assertTrue("A person wasn't found",
				terms.contains("bob robertson-person"));
		assertTrue("A location wasn't found",
				terms.contains("boston , ma-location"));
		assertTrue("An organization wasn't found",
				terms.contains("altamira corporation-organization"));

	}

	private void setUpDictionaryFiles() {
		finderDictionaryFiles.put("location", "en-ner-location.dict");
		finderDictionaryFiles.put("person", "en-ner-person.dict");
		finderDictionaryFiles.put("organization", "en-ner-organization.dict");
	}

}
