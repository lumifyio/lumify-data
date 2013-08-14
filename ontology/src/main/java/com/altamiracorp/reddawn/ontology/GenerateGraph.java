package com.altamiracorp.reddawn.ontology;

import com.altamiracorp.reddawn.cmdline.RedDawnCommandLineBase;
import com.altamiracorp.reddawn.model.ontology.OntologyRepository;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class GenerateGraph extends RedDawnCommandLineBase {
    private PrintStream out = System.out;

    public static void main(String[] args) throws Exception {
        new GenerateGraph().run(args);
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        TitanGraph graph = (TitanGraph) createRedDawnSession().getGraphSession().getGraph();

        out.println("digraph {");
        out.println("\tsplines=curved;");
        out.println("\tsep=\"+50,50\";");
        out.println("\toverlap=scalexy;");
        out.println("\tnodesep=0.6;");
        out.println();

        Vertex entityConcept = graph.getVertices(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME, OntologyRepository.ENTITY_TYPE).iterator().next();
        out.println("\t{ rank=min; \"Entity\";}");
        writeConcept(out, entityConcept);
        writeRelationships(out, graph);

        out.println("}");
        out.close();
        return 0;
    }

    private void writeRelationships(PrintStream out, TitanGraph graph) {
        Iterator<Vertex> relationships = graph.getVertices(OntologyRepository.TYPE_PROPERTY_NAME, OntologyRepository.RELATIONSHIP_TYPE).iterator();
        while (relationships.hasNext()) {
            Vertex relationship = relationships.next();
            String relationshipName = relationship.getProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME);

            Iterator<Vertex> inConcepts = relationship.getVertices(Direction.IN).iterator();
            while (inConcepts.hasNext()) {
                Vertex inConcept = inConcepts.next();
                String inConceptName = inConcept.getProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME);

                Iterator<Vertex> outConcepts = relationship.getVertices(Direction.OUT).iterator();
                while (outConcepts.hasNext()) {
                    Vertex outConcept = outConcepts.next();
                    String outConceptName = outConcept.getProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME);
                    out.println("\t\"" + inConceptName + "\" -> \"" + outConceptName + "\" [ label = \"" + relationshipName + "\"; color=lightgrey; ];");
                }
            }
        }
    }

    private void writeConcept(PrintStream out, Vertex concept) {
        String conceptName = concept.getProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME);
        String propertiesHtml = getConceptPropertiesHtml(concept);
        out.println("\t\"" + conceptName + "\" [");
        out.println("\t\tlabel=<<B>" + conceptName + "</B>" + propertiesHtml + ">");
        out.println("\t];");

        StringBuilder rankList = new StringBuilder();
        Iterator<Vertex> childConcepts = concept.getVertices(Direction.IN, OntologyRepository.IS_A_LABEL_NAME).iterator();
        while (childConcepts.hasNext()) {
            Vertex childConcept = childConcepts.next();
            writeConcept(out, childConcept);

            String childConceptName = childConcept.getProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME);
            out.println("\t\"" + childConceptName + "\" -> \"" + conceptName + "\" [color=black;];");
            rankList.append("\"" + childConceptName + "\";");
        }

        out.println("\t{ rank=same; " + rankList.toString() + "}");
    }

    private String getConceptPropertiesHtml(Vertex concept) {
        StringBuilder result = new StringBuilder();
        Iterator<Vertex> properties = concept.getVertices(Direction.OUT, OntologyRepository.HAS_PROPERTY_LABEL_NAME).iterator();
        while (properties.hasNext()) {
            Vertex childProperty = properties.next();
            String propertyName = childProperty.getProperty(OntologyRepository.ONTOLOGY_TITLE_PROPERTY_NAME);
            String propertyDataType = childProperty.getProperty(OntologyRepository.DATA_TYPE_PROPERTY_NAME);
            result.append("<BR/>" + propertyName + ": " + propertyDataType);
        }
        return result.toString();
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withLongOpt("out")
                        .withDescription("The output filename")
                        .withArgName("filename")
                        .hasArg()
                        .create('o')
        );

        return options;
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);

        if (cmd.hasOption("out")) {
            String outputFileName = cmd.getOptionValue("out");
            out = new PrintStream(new FileOutputStream(outputFileName));
        }
    }
}
