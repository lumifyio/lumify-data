

define([
    'flight/lib/component',
    'service/ontology',
    '3djs',
    'util/previews'
], function(defineComponent, OntologyService, $3djs, previews) {

    var imageCache = {};

    return defineComponent(Graph3D);

    function loadImage(src) {
        if (imageCache[src]) {
            return imageCache[src];
        }

        var deferred = $.Deferred();
        imageCache[src] = deferred;

        var image = new Image();
        image.onload = function() {
            deferred.resolve(this);
        };
        image.onerror = function() {
            deferred.reject(arguments);
        };
        image.src = src;
        imageCache[src] = deferred.promise();
        return imageCache[src];
    }

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
        

        this.addVertices = function(vertices) {
            var graph = this.graph,
                deferredImages = [];

            vertices.forEach(function(vertex) {
                var node = new $3djs.Graph.Node(vertex.graphVertexId);

                node.data = vertex;
                node.data.icon = vertex._glyphIcon || this.icons[vertex._subType];

                if (node.data.icon) {
                    deferredImages.push(
                        loadImage(node.data.icon)
                            .done(function(image) {
                                var ratio = image.naturalWidth / image.naturalHeight,
                                    height = 150;

                                addToGraph(height * ratio, height, node);
                            })
                    );
                } else {
                    console.warn("No icon set for vertex: ", vertex);
                }
            }.bind(this));

            $.when(deferredImages).done(function() {
                graph.needsUpdate = true;
            });
            function addToGraph(width, height, node) {
                node.data.iconWidth = width;
                node.data.iconHeight = height;
                node.data.label = node.data.title || 'No title Available';
                node.needsUpdate = true;
                graph.addNode(node);
            }
        };

        this.onWorkspaceLoaded = function(evt, workspace) {
            var self = this,
                graph = this.graph;

            if (workspace.data && workspace.data.vertices) {
                this.addVertices(workspace.data.vertices);
            }
        };
        this.onVerticesAdded = function(event, data) {
            if (data.vertices) {
                this.addVertices(data.vertices);
            }
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

                this.graph.needsUpdate = true;

                //this.graphRenderer.updateGraph();
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
