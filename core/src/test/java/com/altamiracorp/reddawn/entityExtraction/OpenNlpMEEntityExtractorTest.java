package com.altamiracorp.reddawn.entityExtraction;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.altamiracorp.reddawn.ucd.models.ArtifactKey;
import com.altamiracorp.reddawn.ucd.models.Term;

@RunWith(JUnit4.class)
public class OpenNlpMEEntityExtractorTest {

	private MiniDFSCluster dfsCluster;
	private Context context;
	private String[] modelFiles = new String[] { "en-token.bin",
			"en-ner-date.bin", "en-ner-money.bin", "en-ner-location.bin",
			"en-ner-organization.bin", "en-ner-percentage.bin",
			"en-ner-person.bin", "en-ner-time.bin" };

	private String text = "This is a sentence, written by Bob Robertson, who currently makes 2 million "
			+ "a year. If by 1:30, you don't know what you are doing, you should go watch CNN and see "
			+ "what the latest is on the Benghazi nonsense. I'm 47% sure that this test will pass, but will it?";

	@Before
	public void setUp() throws IOException {
		// let's create a mini dfs cluster (all in-memory)
		Configuration conf = new Configuration();
		dfsCluster = new MiniDFSCluster(conf, 1, true, null);
		System.setProperty("hadoop.log.dir", "./logs");

		// then let's fake out the context object so it returns this new config,
		// based on our fake DFS
		context = mock(Context.class);
		when(context.getConfiguration()).thenReturn(conf);

		createModels();
	}

	private void createModels() throws IOException {
		//get the models from disk and write them to fake DFS
		for (String model : modelFiles) {
			OutputStream modelOut = dfsCluster
					.getFileSystem()
					.create(new Path(
							OpenNlpMaximumEntropyEntityExtractor.DEFAULT_PATH_PREFIX
									+ "/conf/opennlp/" + model));
			InputStream modelIn = Thread.currentThread()
					.getContextClassLoader().getResourceAsStream(model);
			IOUtils.copy(modelIn, modelOut);
			modelOut.close();
			modelIn.close();
		}
	}

	@Test
	public void testEntityExtraction() throws Exception {
		OpenNlpMaximumEntropyEntityExtractor extractor = new OpenNlpMaximumEntropyEntityExtractor();
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
				terms.contains("Bob Robertson-person"));
		assertTrue("A date wasn't found", terms.contains("a year-date"));
		assertTrue("Money wasn't found", terms.contains("2 million-money"));
		assertTrue("A location wasn't found",
				terms.contains("Benghazi-location"));
		assertTrue("An organization wasn't found",
				terms.contains("CNN-organization"));
		assertTrue("A percentage wasn't found", terms.contains("47-percentage"));
		assertTrue("A time wasn't found", terms.contains("1:30-time"));

	}

	@After
	public void tearDown() {
		if (dfsCluster != null) {
			dfsCluster.shutdown();
		}
	}

}
