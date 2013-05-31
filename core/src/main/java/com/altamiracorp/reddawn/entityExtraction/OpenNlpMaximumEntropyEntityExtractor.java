package com.altamiracorp.reddawn.entityExtraction;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.model.BaseModel;

import org.apache.hadoop.mapreduce.Mapper.Context;

import com.altamiracorp.reddawn.ucd.model.ArtifactKey;
import com.altamiracorp.reddawn.ucd.model.Term;
import com.altamiracorp.reddawn.ucd.model.TermKey;
import com.altamiracorp.reddawn.ucd.model.TermMention;
import com.altamiracorp.reddawn.ucd.model.TermMetadata;

public class OpenNlpMaximumEntropyEntityExtractor implements EntityExtractor {
	private static final String MODEL = "OpenNlpMaximumEntropy";
	private TokenizerME tokenizer;
	private List<TokenNameFinder> finders;

	@Override
	public void setup(Context context) throws IOException {
		OpenNlpModelRegistry modelRegistry = new OpenNlpModelRegistry();
		modelRegistry.loadRegistry(context);
		buildTokenizer(modelRegistry.getSingleModel("tokenizer"));
		buildFinders(modelRegistry.getModels("finders"));
	}

	protected void buildFinders(List<BaseModel> models) throws IOException {
		this.finders = new ArrayList<TokenNameFinder>();
		for (BaseModel model : models) {
			NameFinderME finder = new NameFinderME((TokenNameFinderModel) model);
			finders.add(finder);
		}
	}

	protected void buildTokenizer(BaseModel tokenizerModel) throws IOException {
		this.tokenizer = new TokenizerME((TokenizerModel) tokenizerModel);
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
							.model(MODEL)
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

	private String openNlpTypeToConcept(String type) {
		return type; // TODO create a mapping for OpenNLP to UCD concepts
	}
}
