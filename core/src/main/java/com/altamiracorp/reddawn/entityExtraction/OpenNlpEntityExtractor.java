package com.altamiracorp.reddawn.entityExtraction;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Mapper.Context;

import com.altamiracorp.reddawn.ucd.model.Term;
import com.altamiracorp.reddawn.ucd.model.artifact.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermKey;
import com.altamiracorp.reddawn.ucd.model.terms.TermMention;
import com.altamiracorp.reddawn.ucd.model.terms.TermMetadata;

public abstract class OpenNlpEntityExtractor implements EntityExtractor {

	private FileSystem fs;
	private String pathPrefix;

	private Tokenizer tokenizer;
	private List<TokenNameFinder> finders;

	private static final String PATH_PREFIX_CONFIG = "nlpConfPathPrefix";
	private static final String DEFAULT_PATH_PREFIX = "hdfs://";

	@Override
	public void setup(Context context) throws IOException {
		this.pathPrefix = context.getConfiguration().get(PATH_PREFIX_CONFIG,
				DEFAULT_PATH_PREFIX);
		this.fs = FileSystem.get(context.getConfiguration());

		setTokenizer(loadTokenizer());
		setFinders(loadFinders());
	}

	@Override
	public Collection<Term> extract(ArtifactKey artifactKey, String text)
			throws Exception {
		ArrayList<Term> terms = new ArrayList<Term>();
		ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(
				new StringReader(text));
		String line;
		while ((line = untokenizedLineStream.read()) != null) {
			String list[] = tokenizer.tokenize(line);
			for (TokenNameFinder finder : finders) {
				Span[] foundNames = finder.find(list);
				for (Span foundName : foundNames) {
					String name = Span.spansToStrings(new Span[] { foundName },
							list)[0];
					TermKey termKey = TermKey.newBuilder().sign(name)
							.model(getModelName())
							.concept(openNlpTypeToConcept(foundName.getType()))
							.build();
					TermMetadata termMetadata = TermMetadata.newBuilder()
							.artifactKey(artifactKey)
							// .artifactKeySign("testArtifactKeySign") TODO what
							// should go here?
							// .author("testAuthor") TODO what should go here?
							.mention(
									new TermMention(foundName.getStart(),
											foundName.getEnd())) // TODO are
																	// these
																	// offsets
																	// right?
							.build();
					Term term = Term.newBuilder().key(termKey)
							.metadata(termMetadata).build();
					terms.add(term);
				}
				finder.clearAdaptiveData();
			}
		}
		return terms;
	}

	protected abstract List<TokenNameFinder> loadFinders() throws IOException;

	protected abstract String getModelName();

	protected String getPathPrefix() {
		return this.pathPrefix;
	}

	protected FileSystem getFS() {
		return this.fs;
	}

	protected Tokenizer loadTokenizer() throws IOException {
		Path tokenizerHdfsPath = new Path(pathPrefix
				+ "/conf/opennlp/en-token.bin");

		TokenizerModel tokenizerModel = null;
		InputStream tokenizerModelInputStream = fs.open(tokenizerHdfsPath);
		try {
			tokenizerModel = new TokenizerModel(tokenizerModelInputStream);
		} finally {
			tokenizerModelInputStream.close();
		}

		return new TokenizerME(tokenizerModel);
	}

	protected void setFinders(List<TokenNameFinder> finders) {
		this.finders = finders;

	}

	protected void setTokenizer(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	private String openNlpTypeToConcept(String type) {
		return type; // TODO create a mapping for OpenNLP to UCD concepts
	}

}
