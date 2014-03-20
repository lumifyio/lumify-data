package com.altamiracorp.lumify.tools;

import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import com.altamiracorp.lumify.core.model.ontology.OntologyRepository;
import com.altamiracorp.lumify.core.exception.LumifyException;
import com.google.inject.Inject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.coode.owlapi.rdf.rdfxml.RDFXMLRenderer;
import org.jdom.Namespace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

import static com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties.CONCEPT_TYPE;
import static com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties.ONTOLOGY_TITLE;
import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.DISPLAY_NAME;

public class OwlExport extends CommandLineBase {
    private static final Namespace NS_RDF = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    private static final Namespace NS_OWL = Namespace.getNamespace("owl", "http://www.w3.org/2002/07/owl#");
    private static final Namespace NS_RDFS = Namespace.getNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    private static final Namespace NS_LUMIFY = Namespace.getNamespace("lumify", "http://lumify.io#");
    public static final String NS_XML_URI = "http://www.w3.org/XML/1998/namespace";
    private static final Set<String> EXPORT_SKIP_PROPERTIES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            ONTOLOGY_TITLE.getKey(),
            DISPLAY_NAME.getKey(),
            CONCEPT_TYPE.getKey()
    )));

    private OntologyRepository ontologyRepository;
    private ModelSession modelSession;
    private String outFileName;
    private IRI documentIRI;

    public static void main(String[] args) throws Exception {
        int res = new OwlExport().run(args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withLongOpt("out")
                        .withDescription("The output OWL file")
                        .hasArg(true)
                        .withArgName("fileName")
                        .create("o")
        );

        options.addOption(
                OptionBuilder
                        .withLongOpt("iri")
                        .withDescription("The document IRI (URI used for prefixing concepts)")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("uri")
                        .create()
        );

        return options;
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);
        this.outFileName = cmd.getOptionValue("out");
        this.documentIRI = IRI.create(cmd.getOptionValue("iri"));
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
        config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

        List<OWLOntology> loadedOntologies = this.ontologyRepository.loadOntologyFiles(m, config, null);
        OWLOntology o = findOntology(loadedOntologies, documentIRI);
        if (o == null) {
            throw new LumifyException("Could not find ontology with iri " + documentIRI);
        }

        OutputStream out;
        if (outFileName != null) {
            out = new FileOutputStream(outFileName);
        } else {
            out = System.out;
        }
        Writer fileWriter = new OutputStreamWriter(out);

        try {
            new RDFXMLRenderer(o, fileWriter).render();

            return 0;
        } finally {
            fileWriter.close();
        }
    }

    private OWLOntology findOntology(List<OWLOntology> loadedOntologies, IRI documentIRI) {
        for (OWLOntology o : loadedOntologies) {
            if (documentIRI.equals(o.getOntologyID().getOntologyIRI())) {
                return o;
            }
        }
        return null;
    }

    @Inject
    public void setOntologyRepository(OntologyRepository ontologyRepository) {
        this.ontologyRepository = ontologyRepository;
    }

    @Inject
    public void setModelSession(ModelSession modelSession) {
        this.modelSession = modelSession;
    }
}
