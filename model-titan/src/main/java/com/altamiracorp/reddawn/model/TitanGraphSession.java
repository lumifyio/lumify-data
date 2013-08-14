package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphGeoLocation;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.ontology.LabelName;
import com.altamiracorp.reddawn.model.ontology.PropertyName;
import com.altamiracorp.reddawn.model.ontology.VertexType;
import com.altamiracorp.titan.accumulo.AccumuloStorageManager;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.attribute.Geo;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TitanGraphSession extends GraphSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(TitanGraphSession.class.getName());

    public static final String STORAGE_BACKEND_KEY = "graph.storage.backend";
    public static final String STORAGE_TABLE_NAME_KEY = "graph.storage.tablename";
    public static final String STORAGE_INDEX_SEARCH_BACKEND = "graph.storage.index.search.backend";
    public static final String STORAGE_INDEX_SEARCH_HOSTNAME = "graph.storage.index.search.hostname";
    public static final String DEFAULT_STORAGE_TABLE_NAME = "atc_titan";
    public static final String DEFAULT_BACKEND_NAME = AccumuloStorageManager.class.getName();
    public static final String DEFAULT_SEARCH_NAME = "elasticsearch";
    private static final String DEFAULT_STORAGE_INDEX_SEARCH_INDEX_NAME = "titan";
    private static final Integer DEFAULT_STORAGE_INDEX_SEARCH_PORT = 9300;
    private final TitanGraph graph;
    private Properties localConf;

    public TitanGraphSession(Properties props) {
        super();
        localConf = props;
        PropertiesConfiguration conf = new PropertiesConfiguration();
        conf.setProperty("storage.backend", props.getProperty(STORAGE_BACKEND_KEY, DEFAULT_BACKEND_NAME));
        conf.setProperty("storage.tablename", props.getProperty(STORAGE_TABLE_NAME_KEY, DEFAULT_STORAGE_TABLE_NAME));
        conf.setProperty("storage.zookeeperInstanceName", props.getProperty(AccumuloSession.ZOOKEEPER_INSTANCE_NAME));
        conf.setProperty("storage.zookeeperServerName", props.getProperty(AccumuloSession.ZOOKEEPER_SERVER_NAMES));
        conf.setProperty("storage.username", props.getProperty(AccumuloSession.USERNAME));
        conf.setProperty("storage.password", props.getProperty(AccumuloSession.PASSWORD));

        conf.setProperty("storage.index.search.backend", props.getProperty(STORAGE_INDEX_SEARCH_BACKEND, DEFAULT_SEARCH_NAME));
        conf.setProperty("storage.index.search.hostname", props.getProperty(STORAGE_INDEX_SEARCH_HOSTNAME, "localhost"));
        conf.setProperty("storage.index.search.client-only", "true");

        conf.setProperty("autotype", "none");

        LOGGER.info("opening titan:\n" + confToString(conf));
        graph = TitanFactory.open(conf);
    }

    private String confToString(Configuration conf) {
        StringBuilder result = new StringBuilder();
        Iterator keys = conf.getKeys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            result.append(key);
            result.append("=");
            result.append(conf.getString(key));
            result.append("\n");
        }
        return result.toString();
    }

    @Override
    public String save(GraphVertex vertex) {
        Vertex v = null;
        if (vertex.getId() != null) {
            v = this.graph.getVertex(vertex.getId());
        }
        if (v == null) {
            v = this.graph.addVertex(vertex.getId());
        }
        for (String propertyKey : vertex.getPropertyKeys()) {
            Object val = vertex.getProperty(propertyKey);
            if (val instanceof GraphGeoLocation) {
                GraphGeoLocation loc = (GraphGeoLocation) val;
                val = Geoshape.point(loc.getLatitude(), loc.getLongitude());
            }
            v.setProperty(propertyKey, val);
        }
        return "" + v.getId();
    }

    @Override
    public String save(GraphRelationship relationship) {
        Edge edge = null;
        if (relationship.getId() != null) {
            edge = graph.getEdge(relationship.getId());
        }
        if (edge == null) {
            edge = findEdge(relationship.getSourceVertexId(), relationship.getDestVertexId(), relationship.getLabel());
        }
        if (edge == null) {
            Vertex sourceVertex = findVertex(relationship.getSourceVertexId());
            if (sourceVertex == null) {
                throw new RuntimeException("Could not find source vertex: " + relationship.getSourceVertexId());
            }

            Vertex destVertex = findVertex(relationship.getDestVertexId());
            if (destVertex == null) {
                throw new RuntimeException("Could not find destination vertex: " + relationship.getDestVertexId());
            }

            edge = this.graph.addEdge(relationship.getId(), sourceVertex, destVertex, relationship.getLabel());
        }
        for (String propertyKey : relationship.getPropertyKeys()) {
            edge.setProperty(propertyKey, relationship.getProperty(propertyKey));
        }
        return "" + edge.getId();
    }

    private Vertex findVertex(String vertexId) {
        return graph.getVertex(vertexId);
    }

    private List<Edge> findAllEdges(String sourceId, String destId) {
        List<Edge> edgeList = new ArrayList<Edge>();
        Vertex sourceVertex = this.graph.getVertex(sourceId);
        Iterable<Edge> edges = sourceVertex.getEdges(Direction.OUT);
        for (Edge edge : edges) {
            Vertex destVertex = edge.getVertex(Direction.IN);
            String destVertexId = "" + destVertex.getId();
            if (destVertexId.equals(destId)) {
                edgeList.add(edge);
            }
        }
        return edgeList;
    }

    private Edge findEdge(String sourceId, String destId, String label) {
        Vertex sourceVertex = this.graph.getVertex(sourceId);
        Iterable<Edge> edges = sourceVertex.getEdges(Direction.OUT);
        for (Edge edge : edges) {
            Vertex destVertex = edge.getVertex(Direction.IN);
            String destVertexId = "" + destVertex.getId();
            if (destVertexId.equals(destId) && label.equals(edge.getLabel())) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public List<GraphVertex> findBy(String key, String value) {
        Iterable<Vertex> vertices = this.graph.getVertices(key, value);
        return toGraphVertices(vertices);
    }

    private ArrayList<GraphVertex> toGraphVertices(Iterable<Vertex> vertices) {
        ArrayList<GraphVertex> results = new ArrayList<GraphVertex>();
        for (Vertex vertex : vertices) {
            results.add(new TitanGraphVertex(vertex));
        }
        return results;
    }

    @Override
    public List<GraphVertex> getRelatedVertices(String graphVertexId) {
        ArrayList<GraphVertex> results = new ArrayList<GraphVertex>();
        Vertex vertex = this.graph.getVertex(graphVertexId);

        List<Vertex> vertices = new GremlinPipeline(vertex)
                .bothE()
                .bothV()
                .toList();
        vertices.addAll(new GremlinPipeline(vertex)
                .bothE()
                .bothV()
                .in(LabelName.IS_A.toString())
                .toList());
        for (Vertex v : vertices) {
            results.add(new TitanGraphVertex(v));
        }

        return results;
    }

    @Override
    public List<GraphVertex> getResolvedRelatedVertices(String graphVertexId) {
        Vertex vertex = this.graph.getVertex(graphVertexId);

        List<Vertex> resolvedVertices = new GremlinPipeline(vertex)
                .both()
                .hasNot(PropertyName.TYPE.toString(), VertexType.TERM_MENTION.toString())
                .toList();
        resolvedVertices.addAll(new GremlinPipeline(vertex)
                .both()
                .has(PropertyName.TYPE.toString(), VertexType.TERM_MENTION.toString())
                .both()
                .hasNot(PropertyName.TYPE.toString(), VertexType.TERM_MENTION.toString())
                .toList());
        resolvedVertices.addAll(new GremlinPipeline(vertex)
                .both()
                .has(PropertyName.TYPE.toString(), VertexType.TERM_MENTION.toString())
                .as("mentions")
                .both()
                .hasNot(PropertyName.TYPE.toString(), VertexType.TERM_MENTION.toString())
                .hasNot("id", vertex.getId())
                .back("mentions")
                .toList());

        List<GraphVertex> results = new ArrayList<GraphVertex>();
        for (Vertex v : resolvedVertices) {
            results.add(new TitanGraphVertex(v));
        }
        return results;
    }

    @Override
    public List<GraphRelationship> getRelationships(List<String> allIds) {
        List<GraphRelationship> graphRelationships = new ArrayList<GraphRelationship>();
        for (String id : allIds) {
            Vertex vertex = this.graph.getVertex(id);
            List<Vertex> vertexes = new GremlinPipeline(vertex).outE().bothV().toList();
            for (Vertex v : vertexes) {
                if (allIds.contains(v.getId().toString())) {
                    List<Edge> edges = findAllEdges(id, v.getId().toString());
                    for (Edge e : edges) {
                        if (e != null) {
                            graphRelationships.add(new GraphRelationship(e.getId().toString(), id, v.getId().toString(), e.getLabel()));
                        }
                    }
                }
            }
        }

        return graphRelationships;
    }

    @Override
    public HashMap<String, String> getEdgeProperties(String sourceVertex, String destVertex, String label) {
        HashMap<String, String> properties = new HashMap<String, String>();
        Edge e = findEdge(sourceVertex, destVertex, label);
        if (e != null) {
            properties.put("Relationship Type", e.getLabel());
            for (String property : e.getPropertyKeys()) {
                properties.put(property, e.getProperty(property).toString());
            }
            return properties;
        }
        return null;
    }

    @Override
    public List<GraphVertex> findByGeoLocation(double latitude, double longitude, double radius) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.GEO_LOCATION.toString(), Geo.WITHIN, Geoshape.circle(latitude, longitude, radius))
                .vertices();
        return toGraphVertices(r);
    }

    @Override
    public List<GraphVertex> searchVerticesByTitle(String query) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.TITLE.toString(), Text.CONTAINS, query)
                .vertices();
        return toGraphVertices(r);
    }

    @Override
    public List<GraphVertex> searchVerticesByTitleAndType(String query, VertexType type) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.TITLE.toString(), Text.CONTAINS, query)
                .has(PropertyName.TYPE.toString(), type.toString())
                .vertices();
        return toGraphVertices(r);
    }

    @Override
    public GraphVertex findVertexByExactTitleAndType(String graphVertexTitle, VertexType type) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.TITLE.toString(), graphVertexTitle)
                .has(PropertyName.TYPE.toString(), type.toString())
                .vertices();
        ArrayList<GraphVertex> graphVertices = toGraphVertices(r);
        if (graphVertices.size() > 0) {
            return graphVertices.get(0);
        }
        return null;
    }

    @Override
    public GraphVertex findVertexByOntologyTitleAndType(String title, VertexType type) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.ONTOLOGY_TITLE.toString(), title)
                .has(PropertyName.TYPE.toString(), type.toString())
                .vertices();
        ArrayList<GraphVertex> graphVertices = toGraphVertices(r);
        if (graphVertices.size() > 0) {
            return graphVertices.get(0);
        }
        return null;
    }

    @Override
    public GraphVertex findGraphVertex(String graphVertexId) {
        return new TitanGraphVertex(findVertex(graphVertexId));
    }

    @Override
    public void close() {
        this.graph.commit();
        this.graph.shutdown();
    }

    @Override
    public void deleteSearchIndex() {
        LOGGER.info("delete search index: " + DEFAULT_STORAGE_INDEX_SEARCH_INDEX_NAME);
        //TODO: should port be configurable? How about cluster name?
        TransportClient client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(localConf.getProperty(STORAGE_INDEX_SEARCH_HOSTNAME, "localhost"), DEFAULT_STORAGE_INDEX_SEARCH_PORT));
        client.admin().indices().delete(new DeleteIndexRequest(DEFAULT_STORAGE_INDEX_SEARCH_INDEX_NAME)).actionGet();
    }

    @Override
    public Map<String, String> getProperties(String graphVertexId) {
        Vertex vertex = this.graph.getVertex(graphVertexId);
        GremlinPipeline gremlinPipeline = new GremlinPipeline(vertex).map();

        return (Map<String, String>) gremlinPipeline.toList().get(0);
    }

    @Override
    public Map<GraphRelationship, GraphVertex> getRelationships(String graphVertexId) {
        Vertex vertex = this.graph.getVertex(graphVertexId);
        if (vertex == null) {
            throw new RuntimeException("Could not find vertex with id: " + graphVertexId);
        }

        Map<GraphRelationship, GraphVertex> relationships = new HashMap<GraphRelationship, GraphVertex>();
        for (Edge e : vertex.getEdges(Direction.IN)) {
            relationships.put(new TitanGraphRelationship(e), new TitanGraphVertex(e.getVertex(Direction.OUT)));
        }

        for (Edge e : vertex.getEdges(Direction.OUT)) {
            relationships.put(new TitanGraphRelationship(e), new TitanGraphVertex(e.getVertex(Direction.IN)));
        }

        return relationships;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    public void removeRelationship(String source, String target, String label) {
        Edge edge = findEdge(source, target, label);
        if (edge != null) {
            edge.remove();
        }
    }

    @Override
    public void commit() {
        this.graph.commit();
    }
}
