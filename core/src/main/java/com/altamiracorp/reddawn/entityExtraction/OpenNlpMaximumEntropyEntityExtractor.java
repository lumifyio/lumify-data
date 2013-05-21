package com.altamiracorp.reddawn.entityExtraction;

import com.altamiracorp.reddawn.ucd.models.*;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

public class OpenNlpMaximumEntropyEntityExtractor implements EntityExtractor {
  private static final String MODEL = "OpenNlpMaximumEntropy";
  public static final String PATH_PREFIX = "nlpConfPathPrefix";
  public static final String DEFAULT_PATH_PREFIX = "hdfs://";
  private TokenizerME tokenizer;
  private TokenNameFinder[] finders;
  private String pathPrefix;

  @Override
  public void setup(Mapper.Context context) throws IOException {
    FileSystem fs = FileSystem.get(context.getConfiguration());

    this.pathPrefix = context.getConfiguration().get(PATH_PREFIX, DEFAULT_PATH_PREFIX);

    loadTokenizer(fs);
    loadFinders(fs);
  }

  private void loadFinders(FileSystem fs) throws IOException {
    Path finderHdfsPaths[] = {
        new Path(pathPrefix + "/conf/opennlp/en-ner-date.bin"),
        new Path(pathPrefix + "/conf/opennlp/en-ner-location.bin"),
        new Path(pathPrefix + "/conf/opennlp/en-ner-money.bin"),
        new Path(pathPrefix + "/conf/opennlp/en-ner-organization.bin"),
        new Path(pathPrefix + "/conf/opennlp/en-ner-percentage.bin"),
        new Path(pathPrefix + "/conf/opennlp/en-ner-person.bin"),
        new Path(pathPrefix + "/conf/opennlp/en-ner-time.bin")
    };

    ArrayList<TokenNameFinder> finders = new ArrayList<TokenNameFinder>();
    for (Path finderHdfsPath : finderHdfsPaths) {
      InputStream finderInputStream = fs.open(finderHdfsPath);
      try {
        finders.add(new NameFinderME(new TokenNameFinderModel(finderInputStream)));
      } finally {
        finderInputStream.close();
      }
    }
    this.finders = finders.toArray(new TokenNameFinder[0]);
  }

  private void loadTokenizer(FileSystem fs) throws IOException {
    Path tokenizerHdfsPath = new Path(pathPrefix + "/conf/opennlp/en-token.bin");

    InputStream tokenizerInputStream = fs.open(tokenizerHdfsPath);
    try {
      this.tokenizer = new TokenizerME(new TokenizerModel(tokenizerInputStream));
    } finally {
      tokenizerInputStream.close();
    }
  }

  @Override
  public Collection<Term> extract(ArtifactKey artifactKey, String text) throws Exception {
    ArrayList<Term> terms = new ArrayList<Term>();
    ObjectStream<String> untokenizedLineStream = new PlainTextByLineStream(new StringReader(text));
    String line;
    while ((line = untokenizedLineStream.read()) != null) {
      String list[] = tokenizer.tokenize(line);
      for (TokenNameFinder finder : finders) {
        Span[] foundNames = finder.find(list);
        for (Span foundName : foundNames) {
          String name = Span.spansToStrings(new Span[]{foundName}, list)[0];
          TermKey termKey = TermKey.newBuilder()
              .sign(name)
              .model(MODEL)
              .concept(openNlpTypeToConcept(foundName.getType()))
              .build();
          TermMetadata termMetadata = TermMetadata.newBuilder()
              .artifactKey(artifactKey)
                  // .artifactKeySign("testArtifactKeySign")  TODO what should go here?
                  // .author("testAuthor")  TODO what should go here?
              .mention(new TermMention(foundName.getStart(), foundName.getEnd())) // TODO are these offsets right?
              .build();
          Term term = Term.newBuilder()
              .key(termKey)
              .metadata(termMetadata)
              .build();
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
