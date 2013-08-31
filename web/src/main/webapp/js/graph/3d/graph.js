

define([
    'flight/lib/component',
    'service/ontology',
    '3djs'
], function(defineComponent, OntologyService, $3djs) {


    return defineComponent(Graph3D);

    function Graph3D() {
        this.ontologyService = new OntologyService();
        this.defaultAttrs({ });

        this.after('teardown', function() {
            this.graphRenderer.teardown();
            this.$node.empty();
        });

        this.after('initialize', function() {
            var self = this;

            this.icons = {
                document: '/img/glyphicons/glyphicons_036_file@2x.png',
                image: '/img/glyphicons/glyphicons_036_file@2x.png',
                video: '/img/glyphicons/glyphicons_036_file@2x.png'
            };
            this.graph = new $3djs.Graph();
            this.ontologyService.concepts(function(err, concepts) {
                function apply(concept) {
                    self.icons[concept.id] = concept.glyphIconHref;
                    if (concept.children) {
                        concept.children.forEach(apply);
                    }
                }
                concepts.entityConcept.children.forEach(apply);

                self.load3djs();
            });
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'verticesAdded', this.onVerticesAdded);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'existingVerticesAdded', this.onExistingVerticesAdded);
            this.on(document, 'relationshipsLoaded', this.onRelationshipsLoaded);

        });

        this.onWorkspaceLoaded = function(evt, workspace) {
            var self = this,
                graph = this.graph;

            if (workspace.data && workspace.data.vertices) {

                workspace.data.vertices.forEach(function(vertex){
                    var node = new $3djs.Graph.Node(vertex.graphVertexId);
                    node.data = vertex;
                    node.data.icon = vertex._glyphIcon || self.icons[vertex._subType];

                    if (node.data.icon) {
                        var image = new Image();
                        image.onload = function() {
                            var ratio = this.naturalWidth / this.naturalHeight;

                            node.data.iconHeight = 150;
                            node.data.iconWidth = node.data.iconHeight * ratio;
                            node.data.label = node.data.title || 'No title';
                            graph.addNode(node);
                        };
                        image.src = node.data.icon;
                    } else {
                        node.data.iconWidth = 100;
                        node.data.iconHeight = 100;
                        graph.addNode(node);
                    }
                });

                this.graphRenderer.updateGraph();
            }
        };
        this.onVerticesAdded = function() {
        };
        this.onVerticesDeleted = function() {
        };
        this.onVerticesUpdated = function() {
        };
        this.onExistingVerticesAdded = function() {
        };
        this.onRelationshipsLoaded = function(event, data) {
            var graph = this.graph;

            if (data.relationships) {
                data.relationships.forEach(function(r) {
                    var src = graph.node(r.from),
                        dest = graph.node(r.to);
                    
                    if (src && dest) {
                        src.connect(dest);
                    }
                });

                this.graphRenderer.updateGraph();
            }
        };

        this.load3djs = function() {
            var graph = this.graph,
                self = this,
                graphRenderer = new $3djs.GraphRenderer(this.node);

            this.graphRenderer = graphRenderer;
            graphRenderer.renderGraph(this.graph);
            graphRenderer.addToRenderLoop();
            graphRenderer.showStats();

            graphRenderer.addEventListener('node_click', function(event) {
                if (event.content) {
                    var data = graph.node(event.content).data;
                    self.trigger('searchResultSelected', [data]);
                } else {
                    self.trigger('searchResultSelected', []);
                }
            }, false);
        };
    }
});
