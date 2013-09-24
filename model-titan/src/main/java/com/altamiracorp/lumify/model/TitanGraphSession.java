package com.altamiracorp.lumify.model;

import com.altamiracorp.lumify.core.user.User;
import com.altamiracorp.lumify.model.graph.GraphGeoLocation;
import com.altamiracorp.lumify.model.graph.GraphRelationship;
import com.altamiracorp.lumify.model.graph.GraphVertex;
import com.altamiracorp.lumify.model.graph.InMemoryGraphVertex;
import com.altamiracorp.lumify.model.ontology.*;
import com.altamiracorp.titan.accumulo.AccumuloStorageManager;
import com.thinkaurelius.titan.core.*;
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
import org.apache.hadoop.thirdparty.guava.common.collect.Lists;
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

        conf.setProperty("storage.index.search.backend", DEFAULT_SEARCH_NAME);
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
    public String save(GraphVertex vertex, User user) {
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
        commit();
        return "" + v.getId();
    }

    @Override
    public String save(GraphRelationship relationship, User user) {
        Edge edge = null;
        if (relationship.getId() != null) {
            edge = graph.getEdge(relationship.getId());
        }
        if (edge == null) {
            edge = findEdge(relationship.getSourceVertexId(), relationship.getDestVertexId(), relationship.getLabel(), user);
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
        edge.setProperty(PropertyName.TIME_STAMP.toString(), new Date().getTime());
        commit();
        return "" + edge.getId();
    }

    private Vertex findVertex(String vertexId) {
        return graph.getVertex(vertexId);
    }

    private List<Edge> findAllEdges(String sourceId, final String destId) {
        List<Edge> vertices = new GremlinPipeline(this.graph.getVertex(sourceId))
                .outE()
                .toList();
        List<Edge> edgeList = new ArrayList<Edge>();
        for (Edge v : vertices) {
            if (v.getVertex(Direction.IN).getId().toString().equals(destId)) {
                edgeList.add(v);
            }
        }
        return edgeList;
    }

    @Override
    public Edge findEdge(String sourceId, String destId, String label, User user) {
        Vertex sourceVertex = this.graph.getVertex(sourceId);
        if (sourceVertex == null) {
            throw new RuntimeException("Could not find vertex with id: " + sourceId);
        }
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
    public Property getOrCreatePropertyType(String name, PropertyType dataType, User user) {
        TitanKey typeProperty = (TitanKey) graph.getType(name);
        VertexProperty v;
        if (typeProperty != null) {
            v = new VertexProperty(typeProperty);
        } else {
            Class vertexDataType = String.class;
            switch (dataType) {
                case DATE:
                    vertexDataType = Long.class;
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
    public void findOrAddEdge(GraphVertex fromVertex, GraphVertex toVertex, String edgeLabel, User user) {
        checkNotNull(fromVertex, "fromVertex was null");
        checkNotNull(toVertex, "toVertex was null");
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
    public GraphVertex getOrCreateRelationshipType(String relationshipName, User user) {
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
    public List<Vertex> getRelationships(Concept sourceConcept, final Concept destConcept, User user) {
        List<Vertex> sourceAndParents = getConceptParents(sourceConcept, user);
        List<Vertex> destAndParents = getConceptParents(destConcept, user);

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

    private List<Vertex> getConceptParents(Concept concept, User user) {
        ArrayList<Vertex> results = new ArrayList<Vertex>();
        results.add(concept.getVertex());
        Vertex v = concept.getVertex();
        while ((v = getParentConceptVertex(v, user)) != null) {
            results.add(v);
        }
        return results;
    }

    @Override
    public Vertex getParentConceptVertex(Vertex conceptVertex, User user) {
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
    public List<GraphVertex> getRelatedVertices(String graphVertexId, User user) {
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
    public List<GraphRelationship> getRelationships(List<String> allIds, User user) {
        List<GraphRelationship> graphRelationships = new ArrayList<GraphRelationship>();
        for (String id : allIds) {
            Vertex vertex = this.graph.getVertex(id);
            if (vertex == null) {
                throw new RuntimeException("Could not find vertex with id: " + id);
            }
            List<Vertex> vertices = new GremlinPipeline(vertex).outE().inV().toList();
            for (Vertex v : vertices) {
                if (allIds.contains(v.getId().toString())) {
                    List<Edge> edges = findAllEdges(id, v.getId().toString());
                    for (Edge e : edges) {
                        if (e != null) {
                            GraphRelationship relationship = new TitanGraphRelationship(e);
                            relationship.setAllProperties(new TitanGraphRelationship(e).getAllProperty(e));
                            graphRelationships.add(relationship);
                        }
                    }
                }
            }
        }
        commit();
        return graphRelationships;
    }

    @Override
    public Map<String, String> getEdgeProperties(String sourceVertex, String destVertex, String label, User user) {
        Map<String, String> properties = new HashMap<String, String>();
        Edge e = findEdge(sourceVertex, destVertex, label, user);
        if (e != null) {
            for (String property : e.getPropertyKeys()) {
                properties.put(property, e.getProperty(property).toString());
            }
        }
        commit();
        return properties;
    }

    @Override
    public List<GraphVertex> findByGeoLocation(double latitude, double longitude, double radius, User user) {
        Iterable<Vertex> r = graph.query()
                .has(PropertyName.GEO_LOCATION.toString(), Geo.WITHIN, Geoshape.circle(latitude, longitude, radius))
                .vertices();
        return toGraphVertices(r);
    }

    @Override
    public List<GraphVertex> searchVerticesByTitle(String title, JSONArray filterJson, User user) {
        String[] titleParts = title.split(" ");

        TitanGraphQuery query = graph.query();
        for (String titlePart : titleParts) {
            query.has(PropertyName.TITLE.toString(), Text.PREFIX, titlePart);
        }

        Iterable<Vertex> r = query.vertices();
        GremlinPipeline<Vertex, Vertex> queryPipeline = queryFormatter.createQueryPipeline(r, filterJson);
        return toGraphVertices(queryPipeline.toList());
    }

    @Override
    public List<GraphVertex> searchVerticesWithinGraphVertexIds(final List<String> vertexIds, JSONArray filterJson, User user) {
        ArrayList<Vertex> r = new ArrayList<Vertex>();
        for (String vertexId : vertexIds) {
            r.add(findVertex(vertexId));
        }

        GremlinPipeline<Vertex, Vertex> queryPipeline = queryFormatter.createQueryPipeline(r, filterJson);
        ArrayList<Vertex> results = new ArrayList<Vertex>();
        for (Vertex v : queryPipeline.toList()) {
            if (vertexIds.contains(v.getId().toString())) {
                results.add(v);
            }
        }
        return toGraphVertices(results);
    }

    @Override
    public List<GraphVertex> searchVerticesByTitleAndType(String title, VertexType type, User user) {
        String[] titleParts = title.split(" ");

        TitanGraphQuery query = graph.query();

        for (String titlePart : titleParts) {
            query.has(PropertyName.TITLE.toString(), Text.PREFIX, titlePart);
        }

        Iterable<Vertex> r = query
                .has(PropertyName.TYPE.toString(), type.toString())
                .vertices();
        return toGraphVertices(r);
    }

    @Override
    public GraphVertex findVertexByExactTitleAndType(String graphVertexTitle, VertexType type, User user) {
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
    public GraphVertex findVertexByOntologyTitleAndType(String title, VertexType type, User user) {
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
    public GraphVertex findVertexByOntologyTitle(String title, User user) {
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
    public GraphVertex findVertexByRowKey(String rowKey, User user) {
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
    public GraphVertex findGraphVertex(String graphVertexId, User user) {
        Vertex vertex = findVertex(graphVertexId);
        if (vertex == null) {
            return null;
        }
        return new TitanGraphVertex(vertex);
    }

    @Override
    public List<GraphVertex> findGraphVertices(String[] vertexIds, User user) {
        ArrayList<GraphVertex> vertices = new ArrayList<GraphVertex>();
        for (String vertexId : vertexIds) {
            vertices.add(findGraphVertex(vertexId, user));
        }
        return vertices;
    }

    @Override
    public void close() {
        if (this.graph.isOpen()) {
            commit();
            this.graph.shutdown();
        }
    }

    @Override
    public void deleteSearchIndex(User user) {
        LOGGER.info("delete search index: " + DEFAULT_STORAGE_INDEX_SEARCH_INDEX_NAME);
        //TODO: should port be configurable? How about cluster name?
        TransportClient client = new TransportClient().addTransportAddress(new InetSocketTransportAddress(localConf.getProperty(STORAGE_INDEX_SEARCH_HOSTNAME, "localhost"), DEFAULT_STORAGE_INDEX_SEARCH_PORT));
        client.admin().indices().delete(new DeleteIndexRequest(DEFAULT_STORAGE_INDEX_SEARCH_INDEX_NAME)).actionGet();
    }

    @Override
    public Map<String, String> getVertexProperties(String graphVertexId, User user) {
        Vertex vertex = this.graph.getVertex(graphVertexId);
        GremlinPipeline gremlinPipeline = new GremlinPipeline(vertex).map();

        Map<String, String> properties = (Map<String, String>) gremlinPipeline.toList().get(0);
        commit();
        return properties;
    }

    @Override
    public List<List<GraphVertex>> findPath(GraphVertex sourceVertex, GraphVertex destVertex, final int depth, final int hops, User user) {
        Vertex source = getVertex(sourceVertex);
        Collection<Vertex> s = new ArrayList<Vertex>();
        final String destVertexId = destVertex.getId();
        GremlinPipeline gremlinPipeline = new GremlinPipeline(source)
                .both()
                .loop(1,
                        new PipeFunction<LoopPipe.LoopBundle, Boolean>() {
                            @Override
                            public Boolean compute(LoopPipe.LoopBundle loopBundle) {
                                return loopBundle.getLoops() <= depth;

                            }
                        },
                        new PipeFunction<LoopPipe.LoopBundle, Boolean>() {
                            @Override
                            public Boolean compute(LoopPipe.LoopBundle loopBundle) {
                                if (loopBundle.getObject() instanceof Vertex) {
                                    return (((Vertex) loopBundle.getObject()).getId() + "").equals(destVertexId);
                                }
                                return false;
                            }
                        }
                )
                .path()
                .simplePath()
                .groupBy(new PipeFunction() {
                             @Override
                             public Object compute(Object o) {
                                 if (o instanceof List) {
                                     return ((List) o).size();
                                 }
                                 return 0;
                             }
                         },
                        new PipeFunction() {
                            @Override
                            public Object compute(Object o) {
                                if (o instanceof List) {
                                    return o;
                                }
                                return new ArrayList();
                            }
                        }
                ).cap();
        HashMap<Integer, Iterable<Iterable<Vertex>>> pathMap = (HashMap<Integer, Iterable<Iterable<Vertex>>>) gremlinPipeline.toList().get(0);
        return hops == 1 ? toGraphVerticesPath(findShortestPath(pathMap)) : toGraphVerticesPath(findPathsWithHops(pathMap, hops));
    }

    private Iterable<Iterable<Vertex>> findPathsWithHops(HashMap<Integer, Iterable<Iterable<Vertex>>> pathMap, int hops) {
        int targetKey = hops + 2;
        List<Iterable<Vertex>> foundVertices = new ArrayList<Iterable<Vertex>>();

        for (int i = 3; i <= targetKey; i++) {
            if (pathMap.containsKey(i)) {
                foundVertices.addAll(Lists.newArrayList(pathMap.get(i)));
            }
        }

        return foundVertices;
    }

    private Iterable<Iterable<Vertex>> findShortestPath(HashMap<Integer, Iterable<Iterable<Vertex>>> pathMap) {
        int minKey = Integer.MAX_VALUE;
        for (int key : pathMap.keySet()) {
            if (key < minKey) {
                minKey = key;
            }
        }

        return pathMap.containsKey(minKey) ? pathMap.get(minKey) : new ArrayList<Iterable<Vertex>>();
    }

    private Vertex getVertex(GraphVertex v) {
        checkNotNull(v, "GraphVertex cannot be null");
        if (v instanceof TitanGraphVertex) {
            return ((TitanGraphVertex) v).getVertex();
        }
        return this.graph.getVertex(v.getId());
    }

    @Override
    public Map<GraphRelationship, GraphVertex> getRelationships(String graphVertexId, User user) {
        Vertex vertex = this.graph.getVertex(graphVertexId);
        if (vertex == null) {
            throw new RuntimeException("Could not find vertex with id: " + graphVertexId);
        }

        Map<GraphRelationship, GraphVertex> relationships = new TreeMap<GraphRelationship, GraphVertex>(new GraphRelationshipDateComparator());
        for (Edge e : vertex.getEdges(Direction.IN)) {
            GraphRelationship relationship = new TitanGraphRelationship(e);
            relationship.setAllProperties(new TitanGraphRelationship(e).getAllProperty(e));

            relationships.put(relationship, new TitanGraphVertex(e.getVertex(Direction.OUT)));
        }

        for (Edge e : vertex.getEdges(Direction.OUT)) {
            GraphRelationship relationship = new TitanGraphRelationship(e);
            relationship.setAllProperties(new TitanGraphRelationship(e).getAllProperty(e));

            relationships.put(relationship, new TitanGraphVertex(e.getVertex(Direction.IN)));
        }

        return relationships;
    }

    private class GraphRelationshipDateComparator implements Comparator<GraphRelationship> {
        @Override
        public int compare(GraphRelationship rel1, GraphRelationship rel2) {
            Long e1Date = (Long)rel1.getProperty(PropertyName.TIME_STAMP.toString());
            Long e2Date = (Long)rel2.getProperty(PropertyName.TIME_STAMP.toString());
            if (e1Date == null || e2Date == null) {
                return 1;
            }
            return e2Date.compareTo(e1Date);
        }
    }

    @Override
    public void remove(String graphVertexId, User user) {
        Vertex vertex = this.graph.getVertex(graphVertexId);
        if (vertex == null) {
            throw new RuntimeException("Could not find vertex with id: " + graphVertexId);
        }
        vertex.remove();
        commit();
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public void removeRelationship(String source, String target, String label, User user) {
        Edge edge = findEdge(source, target, label, user);
        if (edge != null) {
            edge.remove();
            commit();
        }
    }

    @Override
    public void commit() {
        this.graph.commit();
    }
}
