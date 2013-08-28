package com.altamiracorp.reddawn.structuredDataExtraction;

import com.altamiracorp.reddawn.ConfigurableMapJobBase;
import com.altamiracorp.reddawn.RedDawnMapper;
import com.altamiracorp.reddawn.model.AccumuloModelOutputFormat;
import com.altamiracorp.reddawn.model.Value;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.graph.GraphRepository;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.ontology.Concept;
import com.altamiracorp.reddawn.model.ontology.LabelName;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.termMention.TermMention;
import com.altamiracorp.reddawn.model.termMention.TermMentionRepository;
import com.altamiracorp.reddawn.textExtraction.StructuredDataTextExtractor;
import com.altamiracorp.reddawn.ucd.AccumuloArtifactInputFormat;
import com.altamiracorp.reddawn.ucd.artifact.Artifact;
import com.altamiracorp.reddawn.ucd.artifact.ArtifactRepository;
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
import java.util.List;

public class StructuredDataExtractionMR extends ConfigurableMapJobBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(StructuredDataExtractionMR.class.getName());

    @Override
    protected Class<? extends Mapper> getMapperClass(Job job, Class clazz) {
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

    public static class StructuredDataExtractorMapper extends RedDawnMapper<Text, Artifact, Text, Artifact> {
        private ArtifactRepository artifactRepository = new ArtifactRepository();
        private OntologyRepository ontologyRepository = new OntologyRepository();
        private GraphRepository graphRepository = new GraphRepository();
        private TermMentionRepository termMentionRepository = new TermMentionRepository();
        private StructuredDataFactory structedDataFactory;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            try {
                structedDataFactory = new StructuredDataFactory(context);
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        @Override
        protected void safeMap(Text key, Artifact artifact, Context context) throws Exception {
            JSONObject mappingJson = artifact.getGenericMetadata().getMappingJson();
            if (mappingJson == null) {
                return;
            }
            Value textValue = artifact.getArtifactExtractedText().get(StructuredDataTextExtractor.NAME);
            if (textValue == null || textValue.toString().length() == 0) {
                return;
            }
            String text = textValue.toString();
            String structuredDataType = mappingJson.getString("type");
            LOGGER.info("Extracting structured data from: " + artifact.getRowKey().toString() + ", type: " + structuredDataType);

            StructuredDataExtractorBase structuredDataExtractor = structedDataFactory.get(structuredDataType);
            if (structuredDataExtractor != null) {
                ExtractedData extractedData = structuredDataExtractor.extract(getSession(), artifact, text, mappingJson);

                saveToUcd(artifact, extractedData);
                GraphVertex artifactVertex = saveArtifactToGraph(artifact);
                saveTermsAndGraphVertices(extractedData.getTermsAndGraphVertices(), artifactVertex);
                saveRelationships(extractedData.getRelationships());
            } else {
                throw new Exception("Unknown or unhandled structured data type: " + structuredDataType);
            }
        }

        private void saveToUcd(Artifact artifact, ExtractedData extractedData) {
            artifactRepository.save(getSession().getModelSession(), artifact);

            for (TermAndGraphVertex termAndGraphVertex : extractedData.getTermsAndGraphVertices()) {
                if (termAndGraphVertex == null) {
                    continue;
                }

                Concept concept = ontologyRepository.getConceptByName(getSession().getGraphSession(), termAndGraphVertex.getTermMention().getMetadata().getConcept());
                if (concept == null) {
                    throw new RuntimeException("Could not find concept: " + termAndGraphVertex.getTermMention().getMetadata().getConcept());
                }
                termAndGraphVertex.getTermMention().getMetadata().setConceptGraphVertexId(concept.getId());

                String termRowKey = termAndGraphVertex.getTermMention().getRowKey().toString();
                TermMention existingTerm = termMentionRepository.findByRowKey(getSession().getModelSession(), termRowKey);
                if (existingTerm != null) {
                    existingTerm.update(termAndGraphVertex.getTermMention());
                    termMentionRepository.save(getSession().getModelSession(), existingTerm);
                    termAndGraphVertex.getTermMention().update(existingTerm);
                } else {
                    termMentionRepository.save(getSession().getModelSession(), termAndGraphVertex.getTermMention());
                }
            }
        }

        private GraphVertex saveArtifactToGraph(Artifact artifact) {
            GraphVertex artifactVertex = artifactRepository.saveToGraph(getSession().getModelSession(), getSession().getGraphSession(), artifact);
            getSession().getGraphSession().commit();
            return artifactVertex;
        }

        private void saveTermsAndGraphVertices(List<TermAndGraphVertex> termAndGraphVertices, GraphVertex artifactVertex) {
            HashMap<String, Concept> conceptMap = new HashMap<String, Concept>();

            for (TermAndGraphVertex termAndGraphVertex : termAndGraphVertices) {
                if (termAndGraphVertex == null) {
                    continue;
                }
                TermMention termMention = termAndGraphVertex.getTermMention();
                if (termMention.getMetadata().getGraphVertexId() == null) {
                    if (termAndGraphVertex.getGraphVertex().getId() == null) {
                        String conceptLabel = termAndGraphVertex.getTermMention().getMetadata().getConcept();
                        Concept concept = conceptMap.get(conceptLabel);
                        if (concept == null) {
                            concept = ontologyRepository.getConceptByName(getSession().getGraphSession(), conceptLabel);
                            if (concept == null) {
                                throw new RuntimeException("Could not find concept: " + conceptLabel);
                            }
                            conceptMap.put(conceptLabel, concept);
                        }

                        termAndGraphVertex.getGraphVertex().setProperty(PropertyName.SUBTYPE.toString(), concept.getId());
                        graphRepository.saveVertex(getSession().getGraphSession(), termAndGraphVertex.getGraphVertex());
                        getSession().getGraphSession().commit();
                    }

                    termMention.getMetadata().setGraphVertexId(termAndGraphVertex.getGraphVertex().getId());
                    termMentionRepository.save(getSession().getModelSession(), termAndGraphVertex.getTermMention());

                    GraphRelationship artifactRelationship = new GraphRelationship(null, artifactVertex.getId(), termAndGraphVertex.getGraphVertex().getId(), LabelName.HAS_ENTITY.toString());
                    getSession().getGraphSession().save(artifactRelationship);
                } else {
                    GraphVertex existingGraphVertex = getSession().getGraphSession().findGraphVertex(termMention.getMetadata().getGraphVertexId());
                    existingGraphVertex.update(termAndGraphVertex.getGraphVertex());
                    getSession().getGraphSession().commit();
                }
            }
            getSession().getGraphSession().commit();
        }

        private void saveRelationships(List<StructuredDataRelationship> relationships) {
            for (StructuredDataRelationship relationship : relationships) {
                String sourceVertexId = relationship.getSource().getGraphVertex().getId();
                String destVertexId = relationship.getDest().getGraphVertex().getId();
                String label = relationship.getLabel();
                if (sourceVertexId == null || destVertexId == null || label == null) {
                    throw new RuntimeException("Invalid relationship: " + sourceVertexId + " -> " + label + " -> " + destVertexId);
                }
                GraphRelationship graphRelationship = new GraphRelationship(null, sourceVertexId, destVertexId, label);
                getSession().getGraphSession().save(graphRelationship);
            }
        }
    }

    @Override
    protected boolean hasConfigurableClassname() {
        return false;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new StructuredDataExtractionMR(), args);
        if (res != 0) {
            System.exit(res);
        }
    }
}
