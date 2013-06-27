

define([
    'flight/lib/component',
    'cytoscape',
    './renderer',
    'tpl!./graph',
    'util/throttle',
    'util/previews'
], function(defineComponent, cytoscape, Renderer, template, throttle, previews) {
    'use strict';

    return defineComponent(Graph);

    function Graph() {
        var callbackQueue = [];

        this.defaultAttrs({
            cytoscapeContainerSelector: '.cytoscape-container',
            emptyGraphSelector: '.empty-graph',
            graphToolsSelector: '.ui-cytoscape-panzoom'
        });

        this.cy = function(callback) {
            if ( this.cyLoaded ) {
                callback.call(this, this._cy);
            } else {
                callbackQueue.push( callback );
            }
        };

        this.onNodesAdd = function(evt, data) {
            this.addNodes(data.nodes);
        };

        this.addNodes = function(nodes) {
            this.cy(function(cy) {
                nodes.forEach(function(node) {
                    var title = node.title;
                    var position = node.graphPosition;

                    if (title.length > 10) {
                        title = title.substring(0, 10) + "...";
                    }

                    var cyNodeData = {
                        group: 'nodes',
                        renderedPosition: position,
                        data: {
                            id: node.rowKey,
                            rowKey: node.rowKey,
                            subType: node.subType,
                            type: node.type,
                            title: title,
                            originalTitle: node.title
                        }
                    };

                    var cyNode = cy.add(cyNodeData);
                    cyNode.addClass(node.subType);
                    cyNode.addClass(node.type);

                    if (node.type === 'artifacts') {
                        previews.generatePreview(node.rowKey, { width:178 }, function(dataUri) {
                            if (dataUri) {
                                cyNode.css('background-image', dataUri);
                            }
                        });
                    }
                });

                this.setWorkspaceDirty();
            });
        };

        this.removeSelectedNodes = function() {
            this.cy(function(cy) {
                var nodesToDelete = $.map(cy.nodes().filter(':selected'), function(node) {
                    return {
                        rowKey: node.data('rowKey'),
                        type: node.data('type'),
                        subType: node.data('subType')
                    };
                });

                this.trigger(document, 'nodesDelete', { nodes: nodesToDelete });
            });
        };

        this.onNodesDelete = function(event, data) {
            this.cy(function(cy) {
                var matchingNodes = cy.nodes().filter(function(idx, node) {
                    return data.nodes.filter(function(nodeToDelete) { return node.data('rowKey') == nodeToDelete.rowKey; }).length > 0;
                });
                matchingNodes.remove();
                this.setWorkspaceDirty();

                this.updateNodeSelections(cy);
            });
        };

        this.onNodesUpdate = function(evt, data) {
            this.cy(function(cy) {
                data.nodes
                    .filter(function(updatedNode) { return updatedNode.graphPosition; })
                    .forEach(function(updatedNode) {
                        cy.nodes()
                            .filter(function(idx, node) {
                                return node.data('rowKey') == updatedNode.rowKey;
                            })
                            .each(function(idx, node) {
                                var scale = 'devicePixelRatio' in window ? devicePixelRatio : 1;
                                node.position({
                                    x: updatedNode.graphPosition.x * scale,
                                    y: updatedNode.graphPosition.y * scale
                                });
                            });
                    });
            });
        };

        this.onAddToGraph = function(event, data) {
            var el = $(event.target),
                p = el.offset(),
                c = this.$node.offset(),
                position = {
                    x: p.left - c.left + el.width() / 2.0, 
                    y: p.top - c.top + el.height() / 2.0
                };

            this.trigger(document, 'nodesAdd', {
                nodes: [{
                    title: data.text,
                    rowKey: data.info.rowKey,
                    subType: data.info.subType,
                    type: data.info.type,
                    graphPosition: position
                }]
            });
        };

        this.graphTap = throttle('selection', 100, function(event) {
            if (event.cyTarget === event.cy) {
                this.trigger(document, 'searchResultSelected');
            }
        });

        this.graphSelect = throttle('selection', 100, function(event) {
            this.updateNodeSelections(event.cy);
        });

        this.graphUnselect = throttle('selection', 100, function(event) {
            var self = this,
                selection = event.cy.nodes().filter(':selected');

            if (!selection.length) {
                self.trigger(document, 'searchResultSelected');
            }
        });

        this.updateNodeSelections = function(cy) {
            var selection = cy.nodes().filter(':selected'),
                info = [];

            console.log('selections: ', selection);
            selection.each(function(index, node) {
                info.push(node.data());
            });

            this.trigger(document, 'searchResultSelected', [info]);
        };

        this.onKeyHandler = function(event) {
            var down = event.type === 'keydown',
                up = !down,
                handled = true;

            switch (event.which) {

                case $.ui.keyCode.BACKSPACE:
                case $.ui.keyCode.DELETE:
                    if ( down ) {
                        this.removeSelectedNodes();
                    }
                    break;

                default:
                    handled = false;
            }

            if (handled) {
                event.preventDefault();
                event.stopPropagation();
            }
        };

        this.graphDrag = function(event) {
        };

        this.graphGrab = function(event) {
            this.cy(function(cy) {
                var nodes = event.cyTarget.selected() ? cy.nodes().filter(':selected') : event.cyTarget;
                this.grabbedNodes = nodes.each(function() {
                    var p = this.position();
                    this.data('originalPosition', { x:p.x, y:p.y });
                    this.data('freed', false );
                });
            });
        };

        this.graphFree = function(event) {
            var self = this;

            // CY is sending multiple "free" events, prevent that...
            var dup = true,
                nodes = this.grabbedNodes;

            nodes.each(function(i, e) {
                var p = this.position();
                if ( !e.data('freed') ) {
                    dup = false;
                }
                e.data('targetPosition', {x:p.x, y:p.y});
                e.data('freed', true);

                p = self.pixelsToPoints(p);
            });

            if (dup) {
                return;
            }


            // If the user didn't drag more than a few pixels, select the
            // object, it could be an accidental mouse move
            var target = event.cyTarget, 
                p = target.position(),
                originalPosition = target.data('originalPosition'),
                dx = p.x - originalPosition.x,
                dy = p.y - originalPosition.y,
                distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 5) {
                target.select();
            }


            // Cache these positions since data attr could be overidden
            // then submit to undo manager
            var originalPositions = [], targetPositions = [];
            nodes.each(function(i, e) {
                originalPositions.push( e.data('originalPosition') );
                targetPositions.push( e.data('targetPosition') );
            });

            var graphMovedNodesData = {
                nodes: $.map(nodes, function(node) {
                    return {
                        rowKey: node.data('rowKey'),
                        graphPosition: self.pixelsToPoints({
                            x: node.data('targetPosition').x,
                            y: node.data('targetPosition').y
                        })
                    };
                })
            };
            self.trigger(document, 'nodesUpdate', graphMovedNodesData);

            this.setWorkspaceDirty();
        };

        this.setWorkspaceDirty = function() {
            this.checkEmptyGraph();
        };

        this.checkEmptyGraph = function() {
            this.cy(function(cy) {
                this.select('emptyGraphSelector').toggle(cy.nodes().length === 0);
            });
        };

        this.resetGraph = function() {
            this.cy(function(cy) {
                cy.nodes().remove();
            });
        };

        this.onWorkspaceLoaded = function(evt, workspace) {
            this.resetGraph();
            if (workspace.data && workspace.data.nodes) {
                this.addNodes(workspace.data.nodes);
            }

            this.checkEmptyGraph();
        };

        this.onRelationshipsLoaded = function(evt, relationshipData) {
            this.cy(function(cy) {
                cy.edges().remove();
                relationshipData.relationships.forEach(function(relationship) {
                    cy.add({
                        group: "edges",
                        data: {
                            id: relationship.from + "->" + relationship.to,
                            source: relationship.from,
                            target: relationship.to,
                            type: 'relationship'
                        }
                    });
                });
            });
        };

        this.pixelsToPoints = function(position) {
            if ('devicePixelRatio' in window) {
                return {
                    x: position.x / devicePixelRatio,
                    y: position.y / devicePixelRatio
                };
            } else return position;
        };

        this.after('initialize', function() {
            var self = this;
            this.$node.html(template({}));

            this.$node.droppable({
                drop: function( event, ui ) {
                    var draggable = ui.draggable,
                        droppableOffset = $(event.target).offset();

                    var info = draggable.data('info') || draggable.parents('li').data('info');
                    if ( !info ) {
                        console.warn('No data-info attribute for draggable element found');
                        return;
                    }

                    this.trigger(document, 'nodesAdd', {
                        nodes: [{
                            title: info.title || draggable.text(),
                            rowKey: info.rowKey.replace(/\\[x](1f)/ig, '\u001f'),
                            subType: info.subType,
                            type: info.type,
                            graphPosition: {
                                x: event.clientX - droppableOffset.left,
                                y: event.clientY - droppableOffset.top
                            }
                        }]
                    });
                }.bind(this)
            });

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'nodesAdd', this.onNodesAdd);
            this.on(document, 'nodesDelete', this.onNodesDelete);
            this.on(document, 'nodesUpdate', this.onNodesUpdate);
            this.on(document, 'relationshipsLoaded', this.onRelationshipsLoaded);

            var scale = 'devicePixelRatio' in window ? devicePixelRatio : 1;
            cytoscape("renderer", "red-dawn", Renderer);
            cytoscape({
                showOverlay: false,
                minZoom: 1 / 3,
                maxZoom: 3,
                container: this.select('cytoscapeContainerSelector').css({height:'100%'})[0],
                renderer: {
                    name: 'red-dawn'
                },
                style: cytoscape.stylesheet()
                  .selector('node.person')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_003_user@2x.png'
                    })
                  .selector('node.location')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_242_google_maps@2x.png',
                      'width': 30 * scale,
                      'height': 40 * scale,
                      'border-color': 'white',
                      'border-width': 0
                    })
                  .selector('node.organization')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_263_bank@2x.png',
                      'shape': 'roundrectangle'
                    })
                  .selector('node.document')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
                      'shape': 'rectangle',
                      'width': 60 * scale,
                      'height': 60 * 1.2 * scale,
                      'border-color': '#ccc',
                      'border-width': 1
                    })
                  .selector('node')
                    .css({
                      'width': 30 * scale,
                      'height': 30 * scale,
                      'content': 'data(title)',
                      'font-family': 'helvetica',
                      'font-size': 18 * scale,
                      'text-outline-width': 2,
                      'text-outline-color': 'white',
                      'text-valign': 'bottom',
                      'color': '#999'
                    })
                  .selector(':selected')
                    .css({
                      'background-color': '#0088cc',
                      'border-color': '#0088cc',
                      'line-color': '#000',
                      'color': '#0088cc'
                    })
                  .selector('edge')
                    .css({
                      'width': 2,
                      'target-arrow-shape': 'triangle'
                    }),

                ready: function(){
                    var cy = this;

                    self._cy = cy;

                    self.drainCallbackQueue = function() {
                        callbackQueue.forEach(function( callback ) {
                            callback.call(self, cy); 
                        });
                        callbackQueue.length = 0;
                    };

                    var container = cy.container(),
                        options = cy.options();

                    $(container).cytoscapePanzoom({
                        minZoom: options.minZoom,
                        maxZoom: options.maxZoom
                    }).focus().on({
                        click: function() { this.focus(); },
                        keydown: self.onKeyHandler.bind(self),
                        keyup: self.onKeyHandler.bind(self)
                    });

                    var panZoom = self.select('graphToolsSelector');
                    self.on(document, 'detailPaneResize', function(e, data) {
                        panZoom.css({
                            right: data.width + 'px'
                        });
                    });
                    self.on(document, 'addToGraph', self.onAddToGraph);

                    cy.on({
                        tap: self.graphTap.bind(self),
                        select: self.graphSelect.bind(self),
                        unselect: self.graphUnselect.bind(self),
                        grab: self.graphGrab.bind(self),
                        free: self.graphFree.bind(self),
                        drag: self.graphDrag.bind(self)
                    });

                    self.cyLoaded = true;
                    self.drainCallbackQueue();
                }
            });
        });
    }

});

