package com.altamiracorp.lumify.cmdline;

import com.altamiracorp.lumify.model.TitanGraphSession;
import com.altamiracorp.lumify.model.ontology.LabelName;
import com.altamiracorp.lumify.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.model.ontology.PropertyName;
import com.altamiracorp.lumify.model.ontology.VertexType;
import com.google.inject.Inject;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Iterator;

public class GenerateGraph extends CommandLineBase {
    private PrintStream out = System.out;
    private TitanGraphSession graphSession;

    public static void main(String[] args) throws Exception {
        new GenerateGraph().run(args);
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        TitanGraph graph = (TitanGraph) graphSession.getGraph();

        out.println("digraph {");
        out.println("\tsplines=curved;");
        out.println("\tsep=\"+50,50\";");
        out.println("\toverlap=scalexy;");
        out.println("\tnodesep=0.6;");
        out.println();

        Vertex entityConcept = graph.getVertices(PropertyName.ONTOLOGY_TITLE.toString(), OntologyRepository.ROOT_CONCEPT_NAME).iterator().next();
        out.println("\t{ rank=min; \"" + OntologyRepository.ROOT_CONCEPT_NAME + "\";}");
        writeConcept(out, entityConcept);
        writeRelationships(out, graph);

        out.println("}");
        out.close();
        return 0;
    }

    private void writeRelationships(PrintStream out, TitanGraph graph) {
        Iterator<Vertex> relationships = graph.getVertices(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString()).iterator();
        while (relationships.hasNext()) {
            Vertex relationship = relationships.next();
            String relationshipName = relationship.getProperty(PropertyName.ONTOLOGY_TITLE.toString());

            Iterator<Vertex> inConcepts = relationship.getVertices(Direction.IN).iterator();
            while (inConcepts.hasNext()) {
                Vertex inConcept = inConcepts.next();
                String inConceptName = inConcept.getProperty(PropertyName.ONTOLOGY_TITLE.toString());

                Iterator<Vertex> outConcepts = relationship.getVertices(Direction.OUT).iterator();
                while (outConcepts.hasNext()) {
                    Vertex outConcept = outConcepts.next();
                    String outConceptName = outConcept.getProperty(PropertyName.ONTOLOGY_TITLE.toString());
                    out.println("\t\"" + inConceptName + "\" -> \"" + outConceptName + "\" [ label = \"" + relationshipName + "\"; color=lightgrey; ];");
                }
            }
        }
    }

    private void writeConcept(PrintStream out, Vertex concept) {
        String conceptName = concept.getProperty(PropertyName.ONTOLOGY_TITLE.toString());
        String propertiesHtml = getConceptPropertiesHtml(concept);
        out.println("\t\"" + conceptName + "\" [");
        out.println("\t\tlabel=<<B>" + conceptName + "</B>" + propertiesHtml + ">");
        out.println("\t];");

        StringBuilder rankList = new StringBuilder();
        Iterator<Vertex> childConcepts = concept.getVertices(Direction.IN, LabelName.IS_A.toString()).iterator();
        while (childConcepts.hasNext()) {
            Vertex childConcept = childConcepts.next();
            writeConcept(out, childConcept);

            String childConceptName = childConcept.getProperty(PropertyName.ONTOLOGY_TITLE.toString());
            out.println("\t\"" + childConceptName + "\" -> \"" + conceptName + "\" [color=black;];");
            rankList.append("\"" + childConceptName + "\";");
        }

        out.println("\t{ rank=same; " + rankList.toString() + "}");
    }

    private String getConceptPropertiesHtml(Vertex concept) {
        StringBuilder result = new StringBuilder();
        Iterator<Vertex> properties = concept.getVertices(Direction.OUT, LabelName.HAS_PROPERTY.toString()).iterator();
        while (properties.hasNext()) {
            Vertex childProperty = properties.next();
            String propertyName = childProperty.getProperty(PropertyName.ONTOLOGY_TITLE.toString());
            String propertyDataType = childProperty.getProperty(PropertyName.DATA_TYPE.toString());
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

    @Inject
    public void setGraphSession(TitanGraphSession graphSession) {
        this.graphSession = graphSession;
    }
}
