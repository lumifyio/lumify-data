package com.altamiracorp.reddawn.model;

import com.altamiracorp.reddawn.model.graph.GraphGeoLocation;
import com.altamiracorp.reddawn.model.graph.GraphRelationship;
import com.altamiracorp.reddawn.model.graph.GraphVertex;
import com.altamiracorp.reddawn.model.graph.InMemoryGraphVertex;
import com.altamiracorp.reddawn.model.ontology.*;
import com.altamiracorp.titan.accumulo.AccumuloStorageManager;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanKey;
import com.thinkaurelius.titan.core.TitanType;
import com.thinkaurelius.titan.core.attribute.Geo;
import com.thinkaurelius.titan.core.attribute.Geoshape;
import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class TitanGraphSession extends GraphSession {
    public static final String STORAGE_BACKEND_KEY = "graph.storage.backend";
    public static final String STORAGE_TABLE_NAME_KEY = "graph.storage.tablename";
    public static final String STORAGE_INDEX_SEARCH_BACKEND = "graph.storage.index.search.backend";
    public static final String STORAGE_INDEX_SEARCH_HOSTNAME = "graph.storage.index.search.hostname";
    public static final String DEFAULT_STORAGE_TABLE_NAME = "atc_titan";
    public static final String DEFAULT_BACKEND_NAME = AccumuloStorageManager.class.getName();
    public static final String DEFAULT_SEARCH_NAME = "elasticsearch";
    private static final Logger LOGGER = LoggerFactory.getLogger(TitanGraphSession.class.getName());
    private static final String DEFAULT_STORAGE_INDEX_SEARCH_INDEX_NAME = "titan";
    private static final Integer DEFAULT_STORAGE_INDEX_SEARCH_PORT = 9300;
    private final TitanGraph graph;
    private final TitanQueryFormatter queryFormatter;
    private Properties localConf;

    public TitanGraphSession(Properties props, TitanQueryFormatter queryFormatter) {
        checkNotNull(queryFormatter, "Query formatter cannot be null");
        localConf = props;
        this.queryFormatter = queryFormatter;
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
        if (vertex instanceof TitanGraphVertex) {
            return vertex.getId(); // properties are already set
        }

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
        if (vertex instanceof InMemoryGraphVertex) {
            ((InMemoryGraphVertex) vertex).setId("" + v.getId());
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
        graph.commit();
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

    @Override
    public Edge findEdge(String sourceId, String destId, String label) {
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
    public Property getOrCreatePropertyType(String name, PropertyType dataType) {
        TitanKey typeProperty = (TitanKey) graph.getType(name);
        VertexProperty v;
        if (typeProperty != null) {
            v = new VertexProperty(typeProperty);
        } else {
            Class vertexDataType = String.class;
            switch (dataType) {
                case DATE:
                    vertexDataType = Date.class;
                    break;
                case CURRENCY:
                    vertexDataType = Double.class;
                    break;
                case IMAGE:
                case STRING:
                    vertexDataType = String.class;
                    break;
                case GEO_LOCATION:
                    vertexDataType = Geoshape.class;
                    break;
            }
            v = new VertexProperty(graph.makeType().name(name).dataType(vertexDataType).unique(Direction.OUT).indexed(Vertex.class).makePropertyKey());
        }
        v.setProperty(PropertyName.TYPE.toString(), VertexType.PROPERTY.toString());
        v.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), name);
        v.setProperty(PropertyName.DATA_TYPE.toString(), dataType.toString());
        return v;
    }

    @Override
    public void findOrAddEdge(GraphVertex fromVertex, GraphVertex toVertex, String edgeLabel) {
        Vertex titanFromVertex = getVertex(fromVertex);
        Vertex titanToVertex = getVertex(toVertex);

        Iterator<Edge> possibleEdgeMatches = titanFromVertex.getEdges(Direction.OUT, edgeLabel).iterator();
        while (possibleEdgeMatches.hasNext()) {
            Edge possibleEdgeMatch = possibleEdgeMatches.next();
            TitanGraphVertex possibleMatch = new TitanGraphVertex(possibleEdgeMatch.getVertex(Direction.IN));
            if (possibleMatch.getId().equals(toVertex.getId())) {
                return;
            }
        }
        titanFromVertex.addEdge(edgeLabel, titanToVertex);
    }

    @Override
    public GraphVertex getOrCreateRelationshipType(String relationshipName) {
        TitanType relationshipLabel = graph.getType(relationshipName);
        TitanGraphVertex v;
        if (relationshipLabel != null) {
            v = new TitanGraphVertex(relationshipLabel);
        } else {
            v = new TitanGraphVertex(graph.makeType().name(relationshipName).directed().makeEdgeLabel());
        }
        v.setProperty(PropertyName.TYPE.toString(), VertexType.RELATIONSHIP.toString());
        v.setProperty(PropertyName.ONTOLOGY_TITLE.toString(), relationshipName);
        return v;
    }

    @Override
    public List<Vertex> getRelationships(Concept sourceConcept, final Concept destConcept) {
        List<Vertex> sourceAndParents = getConceptParents(sourceConcept);
        List<Vertex> destAndParents = getConceptParents(destConcept);

        List<Vertex> allRelationshipTypes = new ArrayList<Vertex>();
        for (Vertex s : sourceAndParents) {
            for (Vertex d : destAndParents) {
                allRelationshipTypes.addAll(getRelationshipsShallow(s, d));
            }
        }

        return allRelationshipTypes;
    }

    private List<Vertex> getRelationshipsShallow(Vertex source, final Vertex dest) {
        return new GremlinPipeline(source)
                .outE(LabelName.HAS_EDGE.toString())
                .inV()
                .as("edgeTypes")
                .outE(LabelName.HAS_EDGE.toString())
                .inV()
                .filter(new PipeFunction<Vertex, Boolean>() {
                    @Override
                    public Boolean compute(Vertex vertex) {
                        return vertex.getId().equals(dest.getId());
                    }
                })
                .back("edgeTypes")
                .toList();
    }

    private List<Vertex> getConceptParents(Concept concept) {
        ArrayList<Vertex> results = new ArrayList<Vertex>();
        results.add(concept.getVertex());
        Vertex v = concept.getVertex();
        while ((v = getParentConceptVertex(v)) != null) {
            results.add(v);
        }
        return results;
    }

    @Override
    public Vertex getParentConceptVertex(Vertex conceptVertex) {
        Iterator<Vertex> parents = conceptVertex.getVertices(Direction.OUT, LabelName.IS_A.toString()).iterator();
        if (!parents.hasNext()) {
            return null;
        }
        Vertex v = parents.next();
        if (parents.hasNext()) {
            throw new RuntimeException("Unexpected number of parents for concept: " + conceptVertex.getProperty(PropertyName.TITLE.toString()));
        }
        return v;
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

    private List<List<GraphVertex>> toGraphVerticesPath(Iterable<Iterable<Vertex>> paths) {
        ArrayList<List<GraphVertex>> results = new ArrayList<List<GraphVertex>>();
        for (Iterable<Vertex> path : paths) {
            results.add(toGraphVertices(path));
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
        for (Vertex v : vertices) {
            results.add(new TitanGraphVertex(v));
        }

        return results;
    }

    @Override
    public List<GraphRelationship> getRelationships(List<String> allIds) {
        List<GraphRelationship> graphRelationships = new ArrayList<GraphRelationship>();
        for (String id : allIds) {
            Vertex vertex = this.graph.getVertex(id);
            if (vertex == null) {
                throw new RuntimeException("Could not find vertex with id: " + id);
            }
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
    public Map<String, String> getEdgeProperties(String sourceVertex, String destVertex, String label) {
        Map<String, String> properties = new HashMap<String, String>();
        Edge e = findEdge(sourceVertex, destVertex, label);
        if (e != null) {
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
    public List<GraphVertex> searchVerticesByTitle(String title, JSONArray filterJson) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.TITLE.toString(), Text.CONTAINS, title)
                .vertices();

        GremlinPipeline<Vertex, Vertex> queryPipeline = queryFormatter.createQueryPipeline(r, filterJson);
        return toGraphVertices(queryPipeline.toList());
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
    public GraphVertex findVertexByOntologyTitle(String title) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.ONTOLOGY_TITLE.toString(), title)
                .vertices();
        ArrayList<GraphVertex> graphVertices = toGraphVertices(r);
        if (graphVertices.size() > 0) {
            return graphVertices.get(0);
        }
        return null;
    }

    @Override
    public GraphVertex findVertexByRowKey(String rowKey) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.ROW_KEY.toString(), rowKey)
                .vertices();
        ArrayList<GraphVertex> graphVertices = toGraphVertices(r);
        if (graphVertices.size() > 0) {
            return graphVertices.get(0);
        }
        return null;
    }

    @Override
    public GraphVertex findGraphVertex(String graphVertexId) {
        Vertex vertex = findVertex(graphVertexId);
        if (vertex == null) {
            return null;
        }
        return new TitanGraphVertex(vertex);
    }

    @Override
    public List<GraphVertex> findGraphVertices(String[] vertexIds) {
        ArrayList<GraphVertex> vertices = new ArrayList<GraphVertex>();
        for (String vertexId : vertexIds) {
            vertices.add(findGraphVertex(vertexId));
        }
        return vertices;
    }

    @Override
    public void close() {
        if (this.graph.isOpen()) {
            this.graph.commit();
            this.graph.shutdown();
        }
    }

    @Override
    public void deleteSearchIndex() {
        LOGGER.info("delete search index: " + DEFAULT_STORAGE_INDEX_SEARCH_INDEX_NAME);
        //TODO: should port be configurable? How about cluster name?
        TransportClient client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(localConf.getProperty(STORAGE_INDEX_SEARCH_HOSTNAME, "localhost"), DEFAULT_STORAGE_INDEX_SEARCH_PORT));
        client.admin().indices().delete(new DeleteIndexRequest(DEFAULT_STORAGE_INDEX_SEARCH_INDEX_NAME)).actionGet();
    }

    @Override
    public Map<String, String> getVertexProperties(String graphVertexId) {
        Vertex vertex = this.graph.getVertex(graphVertexId);
        GremlinPipeline gremlinPipeline = new GremlinPipeline(vertex).map();

        return (Map<String, String>) gremlinPipeline.toList().get(0);
    }

    @Override
    public List<List<GraphVertex>> findPath(GraphVertex sourceVertex, GraphVertex destVertex, final int depth) {
        Vertex source = getVertex(sourceVertex);
        final String destVertexId = destVertex.getId();
        GremlinPipeline gremlinPipeline = new GremlinPipeline(source)
                .both()
                .loop(
                        1,
                        new PipeFunction<LoopPipe.LoopBundle, Boolean>() {
                            @Override
                            public Boolean compute(LoopPipe.LoopBundle argument) {
                                return argument.getLoops() <= depth;
                            }
                        },
                        new PipeFunction<LoopPipe.LoopBundle, Boolean>() {
                            @Override
                            public Boolean compute(LoopPipe.LoopBundle argument) {
                                if (argument.getObject() instanceof Vertex) {
                                    Vertex v = (Vertex) argument.getObject();
                                    return ("" + v.getId()).equals(destVertexId);
                                }
                                return false;
                            }
                        }
                )
                .path();

        Iterable<Iterable<Vertex>> paths = (Iterable<Iterable<Vertex>>) gremlinPipeline.toList();
        return toGraphVerticesPath(paths);
    }

    private Vertex getVertex(GraphVertex v) {
        if (v instanceof TitanGraphVertex) {
            return ((TitanGraphVertex) v).getVertex();
        }
        return this.graph.getVertex(v.getId());
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
    public void remove(String graphVertexId) {
        Vertex vertex = this.graph.getVertex(graphVertexId);
        if (vertex == null) {
            throw new RuntimeException("Could not find vertex with id: " + graphVertexId);
        }
        vertex.remove();
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
