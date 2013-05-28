package com.altamiracorp.reddawn.entityExtraction;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.model.BaseModel;

import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.ucd.models.Term;

@RunWith(JUnit4.class)
public class OpenNlpMEEntityExtractorTest {
	private OpenNlpMaximumEntropyEntityExtractor extractor;
	private Context context;
	private String tokenizerModelFile = "en-token.bin";
	private String[] finderModelFiles = new String[] { "en-ner-date.bin",
			"en-ner-money.bin", "en-ner-location.bin",
			"en-ner-organization.bin", "en-ner-percentage.bin",
			"en-ner-person.bin", "en-ner-time.bin" };

	private String text = "This is a sentence, written by Bob Robertson, who currently makes 2 million "
			+ "a year. If by 1:30, you don't know what you are doing, you should go watch CNN and see "
			+ "what the latest is on the Benghazi nonsense. I'm 47% sure that this test will pass, but will it?";

	@Before
	public void setUp() throws IOException {
		context = mock(Context.class);
		extractor = new OpenNlpMaximumEntropyEntityExtractor() {
			@Override
			public void setup(Context context) throws IOException {
				buildTokenizer(loadTokenizer());
				buildFinders(loadFinders());
			}

			private List<BaseModel> loadFinders() throws IOException {
				List<BaseModel> finderModels = new ArrayList<BaseModel>();
				for (String finderModelFile : finderModelFiles) {
					InputStream finderModelIn = Thread.currentThread()
							.getContextClassLoader()
							.getResourceAsStream(finderModelFile);
					TokenNameFinderModel finderModel = new TokenNameFinderModel(
							finderModelIn);
					finderModels.add(finderModel);
				}

				return finderModels;
			}

			private BaseModel loadTokenizer() throws IOException {
				InputStream tokenizerModelIn = Thread.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(tokenizerModelFile);
				TokenizerModel tokenizerModel = new TokenizerModel(
						tokenizerModelIn);
				return tokenizerModel;
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
		assertTrue("A date wasn't found", terms.contains("a year-date"));
		assertTrue("Money wasn't found", terms.contains("2 million-money"));
		assertTrue("A location wasn't found",
				terms.contains("benghazi-location"));
		assertTrue("An organization wasn't found",
				terms.contains("cnn-organization"));
		assertTrue("A percentage wasn't found", terms.contains("47-percentage"));
		assertTrue("A time wasn't found", terms.contains("1:30-time"));

	}

}
