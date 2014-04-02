package com.altamiracorp.lumify.tikaTextExtractor;

import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkData;
import com.altamiracorp.lumify.core.ingest.graphProperty.GraphPropertyWorkerPrepareData;
import com.altamiracorp.lumify.core.model.properties.LumifyProperties;
import com.altamiracorp.lumify.core.model.properties.RawLumifyProperties;
import com.altamiracorp.lumify.core.model.workQueue.WorkQueueRepository;
import com.altamiracorp.securegraph.*;
import com.altamiracorp.securegraph.inmemory.InMemoryAuthorizations;
import com.altamiracorp.securegraph.inmemory.InMemoryGraph;
import com.altamiracorp.securegraph.property.StreamingPropertyValue;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class TikaTextExtractorGraphPropertyWorkerTest {
    private Graph graph;
    private Visibility visibility;
    private Authorizations authorizations;
    private TikaTextExtractorGraphPropertyWorker textExtractor;

    @Mock
    private WorkQueueRepository workQueueRepository;

    @Before
    public void before() throws Exception {
        graph = new InMemoryGraph();
        visibility = new Visibility("");
        authorizations = new InMemoryAuthorizations();
        textExtractor = new TikaTextExtractorGraphPropertyWorker();

        GraphPropertyWorkerPrepareData prepareData = new GraphPropertyWorkerPrepareData(null, null, null, null, null, null);
        textExtractor.prepare(prepareData);
        textExtractor.setGraph(graph);
        textExtractor.setWorkQueueRepository(workQueueRepository);
    }

    @Test
    public void testExtractWithHtml() throws Exception {
        String data = "<html>";
        data += "<head>";
        data += "<title>Test Title</title>";
        data += "<meta content=\"2013-01-01T18:09:20Z\" itemprop=\"datePublished\" name=\"pubdate\"/>";
        data += "</head>";
        data += "<body>";
        data += "<div><table><tr><td>Menu1</td><td>Menu2</td><td>Menu3</td></tr></table></div>\n";
        data += "\n";
        data += "<h1>Five reasons why Windows 8 has failed</h1>\n";
        data += "<p>The numbers speak for themselves. Vista, universally acknowledged as a failure, actually had significantly better adoption numbers than Windows 8. At similar points in their roll-outs, Vista had a desktop market share of 4.52% compared to Windows 8's share of 2.67%. Underlining just how poorly Windows 8's adoption has gone, Vista didn't even have the advantage of holiday season sales to boost its numbers. Tablets--and not Surface RT tablets--were what people bought last December, not Windows 8 PCs.</p>\n";
        data += "</body>";
        data += "</html>";
        createVertex(data);

        InputStream in = new ByteArrayInputStream(data.getBytes());
        Vertex vertex = graph.getVertex("v1", authorizations);
        Property property = vertex.getProperty(RawLumifyProperties.RAW.getKey());
        GraphPropertyWorkData workData = new GraphPropertyWorkData(vertex, property);
        textExtractor.execute(in, workData);

        vertex = graph.getVertex("v1", authorizations);
        assertEquals("Test Title", LumifyProperties.TITLE.getPropertyValue(vertex));

        assertEquals(
                "Five reasons why Windows 8 has failed\n" +
                        "The numbers speak for themselves. Vista, universally acknowledged as a failure, actually had significantly better adoption numbers than Windows 8. At similar points in their roll-outs, Vista had a desktop market share of 4.52% compared to Windows 8's share of 2.67%. Underlining just how poorly Windows 8's adoption has gone, Vista didn't even have the advantage of holiday season sales to boost its numbers. Tablets--and not Surface RT tablets--were what people bought last December, not Windows 8 PCs.\n",
                IOUtils.toString(RawLumifyProperties.TEXT.getPropertyValue(vertex).getInputStream())
        );
        assertEquals(new Date(1357063760000L), RawLumifyProperties.CREATE_DATE.getPropertyValue(vertex));
    }

    private void createVertex(String data) {
        VertexBuilder v = graph.prepareVertex("v1", visibility, authorizations);
        StreamingPropertyValue textValue = new StreamingPropertyValue(new ByteArrayInputStream(data.getBytes()), byte[].class);
        textValue.searchIndex(false);
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(RawLumifyProperties.METADATA_MIME_TYPE, "text/html");
        RawLumifyProperties.RAW.setProperty(v, textValue, metadata, visibility);
        v.save();
    }

    @Test
    public void testExtractWithEmptyHtml() throws Exception {
        String data = "<html>";
        data += "<head>";
        data += "<title>Test Title</title>";
        data += "<meta content=\"2013-01-01T18:09:20Z\" itemprop=\"datePublished\" name=\"pubdate\"/>";
        data += "</head>";
        data += "<body>";
        data += "</body>";
        data += "</html>";
        createVertex(data);

        InputStream in = new ByteArrayInputStream(data.getBytes());
        Vertex vertex = graph.getVertex("v1", authorizations);
        Property property = vertex.getProperty(RawLumifyProperties.RAW.getKey());
        GraphPropertyWorkData workData = new GraphPropertyWorkData(vertex, property);
        textExtractor.execute(in, workData);

        vertex = graph.getVertex("v1", authorizations);
        assertEquals("Test Title", LumifyProperties.TITLE.getPropertyValue(vertex));
        assertEquals("", IOUtils.toString(RawLumifyProperties.TEXT.getPropertyValue(vertex).getInputStream()));
        assertEquals(new Date(1357063760000L), RawLumifyProperties.CREATE_DATE.getPropertyValue(vertex));
    }

    @Test
    public void testExtractWithNotHtml() throws Exception {
        String data = "<title>Test Title</title>";
        data += "<meta content=\"2013-01-01T18:09:20Z\" itemprop=\"datePublished\" name=\"pubdate\"/>";
        data += "<h1>Five reasons why Windows 8 has failed</h1>";
        data += "<p>The numbers speak for themselves. Vista, universally acknowledged as a failure, actually had significantly better adoption numbers than Windows 8. At similar points in their roll-outs, Vista had a desktop market share of 4.52% compared to Windows 8's share of 2.67%. Underlining just how poorly Windows 8's adoption has gone, Vista didn't even have the advantage of holiday season sales to boost its numbers. Tablets--and not Surface RT tablets--were what people bought last December, not Windows 8 PCs.</p>";
        data += "</body>";
        data += "</html>";
        createVertex(data);

        InputStream in = new ByteArrayInputStream(data.getBytes());
        Vertex vertex = graph.getVertex("v1", authorizations);
        Property property = vertex.getProperty(RawLumifyProperties.RAW.getKey());
        GraphPropertyWorkData workData = new GraphPropertyWorkData(vertex, property);
        textExtractor.execute(in, workData);

        vertex = graph.getVertex("v1", authorizations);
        assertEquals("Test Title", LumifyProperties.TITLE.getPropertyValue(vertex));
        assertEquals(
                "Five reasons why Windows 8 has failed\n" +
                        "The numbers speak for themselves. Vista, universally acknowledged as a failure, actually had significantly better adoption numbers than Windows 8. At similar points in their roll-outs, Vista had a desktop market share of 4.52% compared to Windows 8's share of 2.67%. Underlining just how poorly Windows 8's adoption has gone, Vista didn't even have the advantage of holiday season sales to boost its numbers. Tablets--and not Surface RT tablets--were what people bought last December, not Windows 8 PCs.\n",
                IOUtils.toString(RawLumifyProperties.TEXT.getPropertyValue(vertex).getInputStream())
        );
        assertEquals(new Date(1357063760000L), RawLumifyProperties.CREATE_DATE.getPropertyValue(vertex));
    }

    //todo : add test with image metadata
}
