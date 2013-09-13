define([
    'util/retina',
    'service/graph'
], function (retina, GraphService) {

    return FindPath;

    function FindPath() {

        if (!this.graphService) {
            this.graphService = new GraphService();
        }

        this.onContextMenuFindShortestPath = function (hops) {
            var menu = this.select('vertexContextMenuSelector');
            var graphVertexId = menu.data('currentVertexGraphVertexId');
            var sourceVertexPosX = menu.data("currentVertexPositionX");
            var sourceVertexPosY = menu.data("currentVertexPositionY");

            this.findingPath = true;

            this.cy(function (cy) {
                var self = this;
                var sourceVertex = cy.getElementById(graphVertexId);
                var title = sourceVertex.data('originalTitle');
                var beginText = 'Select item to find path to "' + title + '"';
                var instructions = $('<div>')
                    .text(beginText)
                    .addClass('instructions')
                    .appendTo(this.$node);
                var edge = null;
                var targetGraphId = null;

                complete = function (val) {
                    cy.off(tapEvents);
                    cy.off(mouseEvents);
                    cy.panningEnabled(true)
                        .zoomingEnabled(true)
                        .boxSelectionEnabled(true);
                    self.findingPath = false;

                    edge.addClass('label');
                    edge.data('label', 'Finding Path...');

                    var parameters = {
                        sourceGraphVertexId: graphVertexId,
                        destGraphVertexId: targetGraphId,
                        depth: 5,
                        hops: hops
                    };

                    console.log('findPath', parameters);
                    self.graphService.findPath(parameters, function (err, data) {
                        if (edge) {
                            cy.remove(edge);
                            edge = null;
                        }

                        if (err) {
                            self.trigger(document, 'error', err);
                        } else {
                            console.log('findPath results', data);

                            var xOffset = 100, yOffset = 100;
                            var x = sourceVertexPosX || 0;
                            var y = sourceVertexPosY || 0;

                            var vertices = [];
                            data.paths.forEach(function (path) {
                                path.forEach(function (vertex) {
                                    if (vertices.length % 10 === 0) {
                                        y += yOffset;
                                    }
                                    // TODO: refactor this and combine with graph.js/onLoadRelatedSelected
                                    var graphVertexData = $.extend({}, vertex.properties, {
                                        graphVertexId: vertex.id,
                                        graphPosition: {
                                            x: x + xOffset * (vertices.length % 10 + 1),
                                            y: y
                                        },
                                        selected: true
                                    });
                                    vertices.push(graphVertexData);
                                });
                            });
                            if(vertices.length == 0) {
                                // TODO: refactor this to some common function on graph
                                var instructions = $('<div>')
                                    .text(beginText)
                                    .addClass('instructions')
                                    .appendTo(self.$node);
                                instructions.text('Could not find a path.');
                            } else {
                                self.trigger(document, 'addVertices', { vertices: vertices });
                            }
                        }
                    });
                },

                    mouseEvents = {
                        mouseover: function (event) {
                            if (event.cy == event.cyTarget) return;
                            if (event.cyTarget.id() === graphVertexId) return;
                            if (!event.cyTarget.is('node')) return;

                            targetGraphId = event.cyTarget.id();
                            instructions.text('Click to find path');

                            edge = cy.add({
                                group: 'edges',
                                classes: 'temp',
                                data: {
                                    source: graphVertexId,
                                    target: targetGraphId
                                }
                            });
                        },
                        mouseout: function (event) {
                            if (edge && !edge.hasClass('label')) {
                                cy.remove(edge);
                                edge = null;
                            }
                            instructions.text(beginText);
                        }
                    },

                    tapEvents = {
                        tap: function (event) {
                            complete();
                        }
                    };

                cy.on(mouseEvents);
                cy.on(tapEvents);
            });
        };
    }
});
