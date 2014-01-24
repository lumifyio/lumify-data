package com.altamiracorp.lumify.wikipedia;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.tuple.Tuple;
import com.altamiracorp.lumify.core.model.ontology.Concept;
import com.altamiracorp.lumify.core.model.ontology.PropertyName;
import com.altamiracorp.lumify.core.util.LumifyLogger;
import com.altamiracorp.lumify.core.util.LumifyLoggerFactory;
import com.altamiracorp.lumify.storm.BaseLumifyBolt;
import com.altamiracorp.securegraph.ElementMutation;
import com.altamiracorp.securegraph.Graph;
import com.altamiracorp.securegraph.Vertex;
import com.altamiracorp.securegraph.Visibility;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import com.google.inject.Inject;
import org.json.JSONObject;
import org.sweble.wikitext.engine.CompiledPage;
import org.sweble.wikitext.engine.Compiler;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.utils.SimpleWikiConfiguration;
import org.sweble.wikitext.lazy.parser.InternalLink;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

public class WikipediaBolt extends BaseLumifyBolt {
    private static final LumifyLogger LOGGER = LumifyLoggerFactory.getLogger(WikipediaBolt.class);
    public static final String WIKIPEDIA_ID_PREFIX = "WIKIPEDIA_";
    public static final String WIKIPEDIA_LINK_ID_PREFIX = "WIKIPEDIA_LINK_";
    private Graph graph;
    private Compiler compiler;
    private SimpleWikiConfiguration config;
    private Visibility visibility;
    private XPathExpression titleXPath;
    private XPathExpression textXPath;
    private Concept wikipediaPageConcept;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        super.prepare(stormConf, context, collector);

        try {
            config = new SimpleWikiConfiguration("classpath:/org/sweble/wikitext/engine/SimpleWikiConfiguration.xml");
            compiler = new Compiler(config);
            visibility = new Visibility("");

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            textXPath = xpath.compile("/page/revision/text/text()");
            titleXPath = xpath.compile("/page/title/text()");

            Concept documentConcept = ontologyRepository.getConceptByName("document");
            wikipediaPageConcept = ontologyRepository.getOrCreateConcept(documentConcept, "wikipediaPage", "Wikipedia Page");
            wikipediaPageConcept.setProperty(PropertyName.DISPLAY_TYPE.toString(), "document", visibility);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize", e);
        }
    }

    @Override
    protected void safeExecute(Tuple input) throws Exception {
        JSONObject json = getJsonFromTuple(input);
        String vertexId = json.getString("vertexId");
        LOGGER.debug("processing: " + vertexId);

        Vertex pageVertex = graph.getVertex(vertexId, getUser().getAuthorizations());
        if (pageVertex == null) {
            throw new RuntimeException("Could not find vertex: " + vertexId);
        }

        StreamingPropertyValue rawValue = (StreamingPropertyValue) pageVertex.getPropertyValue(PropertyName.RAW.toString());
        if (rawValue == null) {
            throw new RuntimeException("Could not get raw value from vertex: " + vertexId);
        }

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        InputStream in = rawValue.getInputStream();
        String wikitext;
        String title;
        try {
            Document doc = dBuilder.parse(in);
            title = (String) titleXPath.evaluate(doc, XPathConstants.STRING);
            wikitext = (String) textXPath.evaluate(doc, XPathConstants.STRING);
        } finally {
            in.close();
        }

        String fileTitle = vertexId;
        PageTitle pageTitle = PageTitle.make(config, fileTitle);
        PageId pageId = new PageId(pageTitle, -1);
        CompiledPage compiledPage = compiler.postprocess(pageId, wikitext, null);
        TextConverter p = new TextConverter(config, 100000);
        String text = (String) p.go(compiledPage.getPage());
        if (text.length() == 0) {
            text = wikitext;
        }

        StreamingPropertyValue textPropertyValue = new StreamingPropertyValue(new ByteArrayInputStream(text.getBytes()), String.class);

        ElementMutation<Vertex> m = pageVertex.prepareMutation();
        if (title != null || title.length() > 0) {
            m.setProperty(PropertyName.TITLE.toString(), title, visibility);
        }
        m.setProperty(PropertyName.TEXT.toString(), textPropertyValue, visibility);
        m.save();

        for (InternalLink link : p.getInternalLinks()) {
            String linkVertexId = getWikipediaPageVertexId(link.getTarget());
            Vertex linkedPageVertex = graph.prepareVertex(getWikipediaPageVertexId(linkVertexId), visibility, getUser().getAuthorizations())
                    .setProperty(PropertyName.CONCEPT_TYPE.toString(), wikipediaPageConcept.getId(), visibility)
                    .setProperty(PropertyName.MIME_TYPE.toString(), "text/plain", visibility)
                    .setProperty(PropertyName.SOURCE.toString(), "Wikipedia", visibility)
                    .save();
            graph.addEdge(getWikipediaPageToPageEdgeId(pageVertex, linkedPageVertex), pageVertex, linkedPageVertex, "link", visibility, getUser().getAuthorizations());
        }

        graph.flush();
    }

    @Inject
    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    private static String getWikipediaPageToPageEdgeId(Vertex pageVertex, Vertex linkedPageVertex) {
        return WIKIPEDIA_LINK_ID_PREFIX + getWikipediaPageTitleFromId(pageVertex.getId()) + "_" + getWikipediaPageTitleFromId(linkedPageVertex.getId());
    }

    private static String getWikipediaPageTitleFromId(Object id) {
        return id.toString().substring(WIKIPEDIA_ID_PREFIX.length());
    }

    public static String getWikipediaPageVertexId(String pageTitle) {
        return WIKIPEDIA_ID_PREFIX + pageTitle.toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
    }
}
