package com.altamiracorp.reddawn.cmdline;

import com.altamiracorp.reddawn.RedDawnSession;
import org.apache.accumulo.core.util.CachedConfiguration;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.hadoop.util.ToolRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OwlImport extends RedDawnCommandLineBase {
    private String inFileName;

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(CachedConfiguration.getInstance(), new OwlImport(), args);
        if (res != 0) {
            System.exit(res);
        }
    }

    @Override
    protected Options getOptions() {
        Options options = super.getOptions();

        options.addOption(
                OptionBuilder
                        .withLongOpt("in")
                        .withDescription("The input OWL file")
                        .isRequired()
                        .hasArg(true)
                        .withArgName("fileName")
                        .create("i")
        );

        return options;
    }

    @Override
    protected void processOptions(CommandLine cmd) throws Exception {
        super.processOptions(cmd);
        this.inFileName = cmd.getOptionValue("in");
    }

    @Override
    protected int run(CommandLine cmd) throws Exception {
        RedDawnSession session = createRedDawnSession();
        session.getModelSession().initializeTables();

        File inFile = new File(inFileName);

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inFile);

        Element rootElem = doc.getDocumentElement();
        NodeList rootElemChildNodes = rootElem.getChildNodes();
        for (int i = 0; i < rootElemChildNodes.getLength(); i++) {
            Node child = rootElemChildNodes.item(i);
            if (child instanceof Element) {
                Element childElem = (Element) child;
                if (childElem.getNamespaceURI().equals("http://www.w3.org/2002/07/owl#") && childElem.getLocalName().equals("Class")) {
                    importClassElement(session, childElem);
                } else if (childElem.getNamespaceURI().equals("http://www.w3.org/2002/07/owl#") && childElem.getLocalName().equals("DatatypeProperty")) {
                    importDatatypePropertyElement(session, childElem);
                } else if (childElem.getNamespaceURI().equals("http://www.w3.org/2002/07/owl#") && childElem.getLocalName().equals("ObjectProperty")) {
                    importObjectPropertyElement(session, childElem);
                }
            }
        }

        return 0;
    }

    private void importClassElement(RedDawnSession session, Element classElem) {
        String about = classElem.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about");
        Element labelElem = getSingleChildElement(classElem, "http://www.w3.org/2000/01/rdf-schema#", "label");
        String labelText = labelElem.getTextContent().trim();
        Element subClassOf = getSingleChildElement(classElem, "http://www.w3.org/2000/01/rdf-schema#", "subClassOf");
        String subClassOfResource = subClassOf.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");
        List<Element> propertyElems = getChildElements(classElem, "http://altamiracorp.com/ontology#", "property");
        System.out.println("importClassElement: about: " + about + ", labelText: " + labelText + ", subClassOfResource: " + subClassOfResource);
        for (Element propertyElem : propertyElems) {
            String propertyName = propertyElem.getAttributeNS("http://altamiracorp.com/ontology#", "name");
            String propertyValue = propertyElem.getTextContent().trim();
            System.out.println("  " + propertyName + " = " + propertyValue);
        }
    }

    private void importDatatypePropertyElement(RedDawnSession session, Element datatypePropertyElem) {
        String about = datatypePropertyElem.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about");
        Element labelElem = getSingleChildElement(datatypePropertyElem, "http://www.w3.org/2000/01/rdf-schema#", "label");
        String labelText = labelElem.getTextContent().trim();
        Element domainElem = getSingleChildElement(datatypePropertyElem, "http://www.w3.org/2000/01/rdf-schema#", "domain");
        String domainResource = domainElem.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");
        Element rangeElem = getSingleChildElement(datatypePropertyElem, "http://www.w3.org/2000/01/rdf-schema#", "range");
        String rangeResource = rangeElem.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");

        System.out.println("importDatatypePropertyElement: about: " + about + ", labelText: " + labelText + ", domainResource: " + domainResource + ", rangeResource: " + rangeResource);
    }

    private void importObjectPropertyElement(RedDawnSession session, Element objectPropertyElem) {
        String about = objectPropertyElem.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about");
        Element labelElem = getSingleChildElement(objectPropertyElem, "http://www.w3.org/2000/01/rdf-schema#", "label");
        String labelText = labelElem.getTextContent().trim();
        Element domainElem = getSingleChildElement(objectPropertyElem, "http://www.w3.org/2000/01/rdf-schema#", "domain");
        String domainResource = domainElem.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");
        Element rangeElem = getSingleChildElement(objectPropertyElem, "http://www.w3.org/2000/01/rdf-schema#", "range");
        String rangeResource = rangeElem.getAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "resource");

        System.out.println("importObjectPropertyElement: about: " + about + ", labelText: " + labelText + ", domainResource: " + domainResource + ", rangeResource: " + rangeResource);
    }

    private Element getSingleChildElement(Element elem, String ns, String localName) {
        List<Element> childElems = getChildElements(elem, ns, localName);
        if (childElems.size() == 0) {
            return null;
        }
        if (childElems.size() > 1) {
            throw new RuntimeException("More than one child found with " + ns + ":" + localName);
        }
        return childElems.get(0);
    }

    private List<Element> getChildElements(Element elem, String ns, String localName) {
        List<Element> childElems = new ArrayList<Element>();
        NodeList children = elem.getElementsByTagNameNS(ns, localName);
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                if (child.getLocalName().equals(localName)) {
                    childElems.add((Element) child);
                }
            }
        }
        return childElems;
    }
}
