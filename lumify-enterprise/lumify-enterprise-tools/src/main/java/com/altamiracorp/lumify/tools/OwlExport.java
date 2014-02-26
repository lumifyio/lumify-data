package com.altamiracorp.lumify.tools;

import com.altamiracorp.bigtable.model.ModelSession;
import com.altamiracorp.lumify.core.cmdline.CommandLineBase;
import com.altamiracorp.lumify.core.model.ontology.*;
import com.altamiracorp.lumify.core.util.ModelUtil;
import com.google.inject.Inject;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.jdom.Namespace;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;

import static com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties.CONCEPT_TYPE;
import static com.altamiracorp.lumify.core.model.ontology.OntologyLumifyProperties.ONTOLOGY_TITLE;
import static com.altamiracorp.lumify.core.model.properties.LumifyProperties.DISPLAY_NAME;

public class OwlExport extends CommandLineBase {
    private static final Namespace NS_RDF = Namespace.getNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    private static final Namespace NS_OWL = Namespace.getNamespace("owl", "http://www.w3.org/2002/07/owl#");
    private static final Namespace NS_RDFS = Namespace.getNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    private static final Namespace NS_ATC = Namespace.getNamespace("atc", "http://altamiracorp.com/ontology#");
    public static final String NS_XML_URI = "http://www.w3.org/XML/1998/namespace";
    private static final Set<String> EXPORT_SKIP_PROPERTIES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            ONTOLOGY_TITLE.getKey(),
            DISPLAY_NAME.getKey(),
            CONCEPT_TYPE.getKey()
    )));

    private OntologyRepository ontologyRepository;
    private ModelSession modelSession;
    private String outFileName;

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

        return options;
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);
        this.outFileName = cmd.getOptionValue("out");
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        ModelUtil.initializeTables(modelSession, getUser());

        OutputStream out;
        if (outFileName != null) {
            out = new FileOutputStream(outFileName);
        } else {
            out = System.out;
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder db = dbFactory.newDocumentBuilder();
        Document doc = db.newDocument();

        Element rootElem = doc.createElementNS(NS_RDF.getURI(), "rdf:RDF");
        rootElem.setAttribute("xmlns:rdfs", NS_RDFS.getURI());
        rootElem.setAttribute("xmlns:owl", NS_OWL.getURI());
        rootElem.setAttribute("xmlns:atc", NS_ATC.getURI());
        rootElem.setAttribute("xmlns:rdf", NS_RDF.getURI());

        rootElem.appendChild(createVersionElement(doc));

        Concept rootConcept = ontologyRepository.getRootConcept();
        List<Node> nodes = createConceptElements(doc, rootConcept, null);
        for (Node e : nodes) {
            rootElem.appendChild(e);
        }

        Iterable<Relationship> relationships = ontologyRepository.getRelationshipLabels();
        for (Relationship relationship : relationships) {
            nodes = createRelationshipElements(doc, relationship);
            for (Node e : nodes) {
                rootElem.appendChild(e);
            }
        }

        doc.appendChild(rootElem);

        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        tr.transform(new DOMSource(doc), new StreamResult(out));

        return 0;
    }

    private List<Node> createRelationshipElements(Document doc, Relationship relationship) {
        List<Node> elems = new ArrayList<Node>();
        elems.add(createObjectPropertyElement(doc, relationship));

        List<OntologyProperty> properties = ontologyRepository.getPropertiesByRelationship(relationship.getTitle());
        for (OntologyProperty property : properties) {
            elems.add(createDatatypePropertyElement(doc, property, relationship));
        }

        return elems;
    }

    private Node createObjectPropertyElement(Document doc, Relationship relationship) {
        Element elem = doc.createElementNS(NS_OWL.getURI(), "owl:ObjectProperty");
        elem.setAttributeNS(NS_RDF.getURI(), "rdf:about", relationship.getTitle());
        elem.appendChild(createLabelElement(doc, relationship.getDisplayName()));
        Concept sourceConcept = ontologyRepository.getConceptById(relationship.getSourceConceptId());
        elem.appendChild(createDomainElement(doc, sourceConcept));
        Concept destConcept = ontologyRepository.getConceptById(relationship.getDestConceptId());
        elem.appendChild(createRangeElement(doc, destConcept));
        return elem;
    }

    private List<Node> createConceptElements(Document doc, Concept concept, Concept parentConcept) {
        List<Node> elems = new ArrayList<Node>();

        elems.add(doc.createComment(" Concept: " + concept.getTitle() + " "));

        Element classElem = doc.createElementNS(NS_OWL.getURI(), "owl:Class");
        elems.add(classElem);

        classElem.setAttributeNS(NS_RDF.getURI(), "rdf:about", concept.getTitle());
        classElem.appendChild(createLabelElement(doc, concept.getDisplayName()));

        for (com.altamiracorp.securegraph.Property property : concept.getVertex().getProperties()) {
            if (!EXPORT_SKIP_PROPERTIES.contains(property.getName())) {
                classElem.appendChild(createPropertyElement(doc, property.getName(), property.getValue().toString()));
            }
        }
        if (parentConcept != null) {
            classElem.appendChild(createSubClassOfElement(doc, parentConcept));
        }

        List<OntologyProperty> properties = ontologyRepository.getPropertiesByConceptIdNoRecursion(concept.getId());
        for (OntologyProperty property : properties) {
            elems.add(createDatatypePropertyElement(doc, property, concept));
        }

        List<Concept> childConcepts = ontologyRepository.getChildConcepts(concept);
        for (Concept childConcept : childConcepts) {
            elems.addAll(createConceptElements(doc, childConcept, concept));
        }

        return elems;
    }

    private Element createDatatypePropertyElement(Document doc, OntologyProperty property, Concept concept) {
        Element elem = doc.createElementNS(NS_OWL.getURI(), "owl:DatatypeProperty");
        elem.setAttributeNS(NS_RDF.getURI(), "rdf:about", property.getTitle());
        elem.appendChild(createLabelElement(doc, property.getDisplayName()));
        elem.appendChild(createDomainElement(doc, concept));
        elem.appendChild(createRangeElement(doc, property.getDataType()));
        return elem;
    }

    private Element createDatatypePropertyElement(Document doc, OntologyProperty property, Relationship relationship) {
        Element elem = doc.createElementNS(NS_OWL.getURI(), "owl:DatatypeProperty");
        elem.setAttributeNS(NS_RDF.getURI(), "rdf:about", property.getTitle());
        elem.appendChild(createLabelElement(doc, property.getDisplayName()));
        elem.appendChild(createDomainElement(doc, relationship));
        elem.appendChild(createRangeElement(doc, property.getDataType()));
        return elem;
    }

    private Node createRangeElement(Document doc, PropertyType dataType) {
        Element elem = doc.createElementNS(NS_RDFS.getURI(), "rdfs:range");
        elem.setAttributeNS(NS_RDF.getURI(), "rdf:resource", "http://altamiracorp.com/datatype/" + dataType.toString());
        return elem;
    }

    private Node createRangeElement(Document doc, Concept concept) {
        Element elem = doc.createElementNS(NS_RDFS.getURI(), "rdfs:range");
        elem.setAttributeNS(NS_RDF.getURI(), "rdf:resource", "#" + concept.getTitle());
        return elem;
    }

    private Node createDomainElement(Document doc, Concept concept) {
        Element elem = doc.createElementNS(NS_RDFS.getURI(), "rdfs:domain");
        elem.setAttributeNS(NS_RDF.getURI(), "rdf:resource", "#" + concept.getTitle());
        return elem;
    }

    private Node createDomainElement(Document doc, Relationship relationship) {
        Element elem = doc.createElementNS(NS_RDFS.getURI(), "rdfs:domain");
        elem.setAttributeNS(NS_RDF.getURI(), "rdf:resource", "#" + relationship.getTitle());
        return elem;
    }

    private Node createSubClassOfElement(Document doc, Concept parentConcept) {
        Element elem = doc.createElementNS(NS_RDFS.getURI(), "rdfs:subClassOf");
        elem.setAttributeNS(NS_RDF.getURI(), "rdf:resource", "#" + parentConcept.getTitle());
        return elem;
    }

    private Node createPropertyElement(Document doc, String propertyName, Object propertyValue) {
        Element elem = doc.createElementNS(NS_ATC.getURI(), "atc:property");
        elem.setAttributeNS(NS_ATC.getURI(), "atc:name", propertyName);
        elem.setTextContent(propertyValue.toString());
        return elem;
    }

    private Element createLabelElement(Document doc, String displayName) {
        Element elem = doc.createElementNS(NS_RDFS.getURI(), "rdfs:label");
        elem.setAttributeNS(NS_XML_URI, "lang", "en");
        elem.setTextContent(displayName);
        return elem;
    }

    private Element createVersionElement(Document doc) {
        Element ontologyElem = doc.createElementNS(NS_OWL.getURI(), "owl:Ontology");
        ontologyElem.setAttributeNS(NS_RDF.getURI(), "rdf:about", "");

        Element versionInfoElem = doc.createElementNS(NS_OWL.getURI(), "owl:versionInfo");
        versionInfoElem.setAttributeNS(NS_XML_URI, "lang", "en");
        versionInfoElem.setTextContent("Version 3.8");
        ontologyElem.appendChild(versionInfoElem);

        return ontologyElem;
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
