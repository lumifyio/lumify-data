package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphNode;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.titan.accumulo.AccumuloStorageManager;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class TitanGraphSession extends GraphSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(TitanGraphSession.class.getName());

    private static final String STORAGE_BACKEND_KEY = "graph.storage.backend";
    private static final String STORAGE_TABLE_NAME_KEY = "graph.storage.tablename";
    private static final String STORAGE_INDEX_SEARCH_BACKEND = "graph.storage.index.search.backend";
    private static final String STORAGE_INDEX_SEARCH_HOSTNAME = "graph.storage.index.search.hostname";
    public static final String DEFAULT_STORAGE_TABLE_NAME = "atc_titan";
    public static final String DEFAULT_BACKEND_NAME = AccumuloStorageManager.class.getName();
    public static final String DEFAULT_SEARCH_NAME = "elasticsearch";
    private final TitanGraph graph;

    public TitanGraphSession(Properties props) {
        super();

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

        LOGGER.info("opening titan:\n" + confToString(conf));
        graph = TitanFactory.open(conf);
        graph.createKeyIndex("rowKey", Vertex.class);
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
    public String save(GraphNode node) {
        Vertex vertex = this.graph.getVertex(node.getId());
        if (vertex == null) {
            vertex = this.graph.addVertex(node.getId());
        }
        for (String propertyKey : node.getPropertyKeys()) {
            vertex.setProperty(propertyKey, node.getProperty(propertyKey));
        }
        this.graph.commit();
        return "" + vertex.getId();
    }

    @Override
    public String save(GraphRelationship relationship) {
        Edge edge = null;
        if (relationship.getId() != null) {
            edge = graph.getEdge(relationship.getId());
        }
        if (edge == null) {
            edge = findEdge(relationship.getSource().getId(), relationship.getDest().getId());
        }
        if (edge == null) {
            Vertex sourceVertex = findVertex(relationship.getSource());
            Vertex destVertex = findVertex(relationship.getDest());
            edge = this.graph.addEdge(relationship.getId(), sourceVertex, destVertex, relationship.getLabel());
        }
        for (String propertyKey : relationship.getPropertyKeys()) {
            edge.setProperty(propertyKey, relationship.getProperty(propertyKey));
        }
        this.graph.commit();
        return "" + edge.getId();
    }

    private Vertex findVertex(GraphNode node) {
        if (node instanceof TitanGraphNode) {
            return ((TitanGraphNode) node).getVertex();
        }
        return graph.getVertex(node.getId());
    }

    private Edge findEdge(String sourceId, String destId) {
        // TODO could there be multiple edge matches for sourceId and destId
        Vertex sourceVertex = this.graph.getVertex(sourceId);
        Iterable<Edge> edges = sourceVertex.getEdges(Direction.OUT);
        for (Edge edge : edges) {
            Vertex destVertex = edge.getVertex(Direction.OUT);
            String destVertexId = "" + destVertex.getId();
            if (destVertexId.equals(destId)) {
                return edge;
            }
        }
        return null;
    }

    @Override
    public List<GraphNode> findBy(String key, String value) {
        Iterable<Vertex> vertices = this.graph.getVertices(key, value);
        ArrayList<GraphNode> results = new ArrayList<GraphNode>();
        for (Vertex vertex : vertices) {
            results.add(new TitanGraphNode(vertex));
        }
        return results;
    }

    @Override
    public List<GraphNode> getRelatedNodes(String graphNodeId) {
        ArrayList<GraphNode> results = new ArrayList<GraphNode>();
        Vertex vertex = this.graph.getVertex(graphNodeId);

        Iterable<Edge> edges = vertex.getEdges(Direction.OUT);
        for (Edge edge : edges) {
            results.add(new TitanGraphNode(edge.getVertex(Direction.IN)));
        }

        edges = vertex.getEdges(Direction.IN);
        for (Edge edge : edges) {
            results.add(new TitanGraphNode(edge.getVertex(Direction.OUT)));
        }

        return results;
    }
}
