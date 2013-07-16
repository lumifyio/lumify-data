package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnSession;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Row;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
import com.altamiracorp.reddawn.ucd.sentence.SentenceRepository;
import com.altamiracorp.reddawn.ucd.statement.StatementRepository;
import com.altamiracorp.reddawn.ucd.term.TermRepository;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

public class StructuredDataExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredDataExtractionMR.class.getName());

    @Override
    protected Class getMapperClass(Job job, Class clazz) {
        return StructuredDataExtractorMapper.class;
    }

    @Override
    protected Class<? extends InputFormat> getInputFormatClassAndInit(Job job) {
        AccumuloArtifactInputFormat.init(job, getUsername(), getPassword(), getAuthorizations(), getZookeeperInstanceName(), getZookeeperServerNames());
        return AccumuloArtifactInputFormat.class;
    }

    @Override
    protected Class<? extends OutputFormat> getOutputFormatClass() {
        return AccumuloModelOutputFormat.class;
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }

    public static class StructuredDataExtractorMapper extends Mapper<Text, Artifact, Text, Row> {
        private RedDawnSession session;
        private ArtifactRepository artifactRepository = new ArtifactRepository();
        private SentenceRepository sentenceRepository = new SentenceRepository();
        private TermRepository termRepository = new TermRepository();
        private StatementRepository statementRepository = new StatementRepository();
        private HashMap<String, StructuredDataExtractorBase> structuredDataExtrators = new HashMap<String, StructuredDataExtractorBase>();

        public StructuredDataExtractorMapper() {
            structuredDataExtrators.put("csv", new CsvStructuredDataExtractor());
        }

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                session = ConfigurableMapJobBase.createRedDawnSession(context);
                for (StructuredDataExtractorBase structuredDataExtractor : structuredDataExtrators.values()) {
                    structuredDataExtractor.setup(context);
                }
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        public void map(Text rowKey, Artifact artifact, Context context) throws IOException, InterruptedException {
            try {
                JSONObject mappingJson = artifact.getGenericMetadata().getMappingJson();
                if (mappingJson == null) {
                    return;
                }
                String text = artifact.getContent().getDocExtractedTextString();
                if (text == null || text.length() == 0) {
                    return;
                }
                String structuredDataType = mappingJson.getString("type");
                LOGGER.info("Extracting structured data from: " + artifact.getRowKey().toString() + ", type: " + structuredDataType);

                if (mappingJson.has("subject")) {
                    artifact.getGenericMetadata().setSubject(mappingJson.getString("subject"));
                }

                StructuredDataExtractorBase structuredDataExtractor = structuredDataExtrators.get(structuredDataType);
                if (structuredDataExtractor != null) {
                    ExtractedData extractedData = structuredDataExtractor.extract(artifact, text, mappingJson);

                    sentenceRepository.saveMany(session.getModelSession(), extractedData.getSentences());
                    termRepository.saveMany(session.getModelSession(), extractedData.getTerms());
                    statementRepository.saveMany(session.getModelSession(), extractedData.getStatements());
                    artifactRepository.save(session.getModelSession(), artifact);
                } else {
                    throw new Exception("Unknown or unhandled structured data type: " + structuredDataType);
                }

            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new StructuredDataExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}

