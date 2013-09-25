

define([
    'flight/lib/component',
    'service/ontology',
    '3djs',
    'util/previews'
], function(defineComponent, OntologyService, $3djs, previews) {
    'use strict';

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

            if (this.attr.vertices && this.attr.vertices.length) {
                this.addVertices(this.attr.vertices);
            }

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'verticesAdded', this.onVerticesAdded);
            this.on(document, 'verticesDropped', this.onVerticesDropped);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'existingVerticesAdded', this.onExistingVerticesAdded);
            this.on(document, 'relationshipsLoaded', this.onRelationshipsLoaded);
        });

        this.onVerticesDropped = function(event, data) {
            this.addVertices(data.vertices);
            this.trigger(document, 'addVertices', data);
        };

        this.addVertices = function(vertices) {
            var graph = this.graph,
                deferredImages = [];

            vertices.forEach(function(vertex) {
                var node = new $3djs.Graph.Node(vertex.id);

                node.data = vertex;
                node.data.icon = vertex.properties._glyphIcon || this.icons[vertex.properties._subType];

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
                if (self.relationships && self.relationships.length) {
                    self.addEdges(self.relationships);
                }
                graph.needsUpdate = true;
            });
            function addToGraph(width, height, node) {
                node.data.iconWidth = width;
                node.data.iconHeight = height;
                node.data.label = node.data.properties.title;
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
                this.relationships = data.relationships;
                this.addEdges(data.relationships);
                this.graph.needsUpdate = true;
            }
        };

        this.addEdges = function(relationships) {
            var graph = this.graph,
                edges = graph.edges;

            edges.length = 0;
            relationships.forEach(function(r) {
                var source = graph.node(r.from),
                    target = graph.node(r.to);

                if (source && target) {
                    edges.push({
                        source: source,
                        target: target
                    });
                }
            });
        };

        this.load3djs = function() {
            var graph = this.graph,
                self = this,
                graphRenderer = new $3djs.GraphRenderer(this.node);

            this.graphRenderer = graphRenderer;
            graphRenderer.renderGraph(this.graph);
            graphRenderer.addToRenderLoop();
            //graphRenderer.showStats();

            graphRenderer.addEventListener('node_click', function(event) {
                var selected = [];
                if (event.content) {
                    var data = graph.node(event.content).data;
                    selected.push(data);
                }
                self.trigger('verticesSelected', [selected]);
            }, false);
        };
    }
});
