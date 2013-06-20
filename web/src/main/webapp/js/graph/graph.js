

define([
    'flight/lib/component',
    'cytoscape',
    './renderer',
    'tpl!./graph'
], function(defineComponent, cytoscape, Renderer, template) {
    'use strict';

    return defineComponent(Graph);

    function Graph() {
        var cy = null;

        this.defaultAttrs({
            cytoscapeContainerSelector: '.cytoscape-container',
            emptyGraphSelector: '.empty-graph',
            graphToolsSelector: '.ui-cytoscape-panzoom'
        });

        this.onNodesAdd = function(evt, data) {
            this.addNodes(data.nodes);
        };

        this.addNodes = function(nodes) {
            var cyNodes = $.map(nodes, function(node) {
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
                        title: title
                    }
                };

                var cyNode = cy.add(cyNodeData);

                cyNode.addClass(node.subType);
                cyNode.addClass(node.type);
                return cyNode;
            });

            this.setWorkspaceDirty();
        };

        this.removeSelectedNodes = function() {
            var nodesToDelete = $.map(cy.nodes().filter(':selected'), function(node) {
                return {
                    rowKey: node.data('rowKey'),
                    type: node.data('type'),
                    subType: node.data('subType')
                };
            });

            this.trigger(document, 'nodesDelete', { nodes: nodesToDelete });
        };

        this.onNodesDelete = function(event, data) {
            var matchingNodes = cy.nodes().filter(function(idx, node) {
                return data.nodes.filter(function(nodeToDelete) { return node.data('rowKey') == nodeToDelete.rowKey; }).length > 0;
            });
            matchingNodes.remove();
            this.setWorkspaceDirty();
        };

        this.onNodesUpdate = function(evt, data) {
            data.nodes.forEach(function(updatedNode) {
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

        this.graphSelect = function(event) {
            // TODO: multiple selection is two different events
            this.trigger(document, 'searchResultSelected', event.cyTarget.data());
        };
        this.graphUnselect = function(event) {
            // TODO: send empty event? needs detail to support
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
            var nodes = event.cyTarget.selected() ? cy.nodes().filter(':selected') : event.cyTarget;
            this.grabbedNodes = nodes.each(function() {
                var p = this.position();
                this.data('originalPosition', { x:p.x, y:p.y });
                this.data('freed', false );
            });
        };

        this.graphFree = function(event) {
            var $this = this;

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

                p = $this.pixelsToPoints(p);
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
                        graphPosition: $this.pixelsToPoints({
                            x: node.data('targetPosition').x,
                            y: node.data('targetPosition').y
                        })
                    };
                })
            };
            $this.trigger(document, 'nodesUpdate', graphMovedNodesData);

            this.setWorkspaceDirty();
        };

        this.setWorkspaceDirty = function() {
            this.checkEmptyGraph();
        };

        this.checkEmptyGraph = function() {
            this.select('emptyGraphSelector').toggle(cy.nodes().length === 0);
        };

        this.resetGraph = function() {
            cy.nodes().remove();
        };

        this.onWorkspaceLoaded = function(evt, workspace) {
            this.resetGraph();
            if (workspace.data && workspace.data.nodes) {
                this.addNodes(workspace.data.nodes);
            }

            this.checkEmptyGraph();
        };

        this.onRelationshipsLoaded = function(evt, relationshipData) {
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
            var $this = this;
            this.$node.html(template({}));

            this.$node.droppable({
                drop: function( event, ui ) {
                    var draggable = ui.draggable,
                        droppableOffset = $(event.target).offset(),
                        text = draggable.text();

                    var info = draggable.data('info') || draggable.parents('li').data('info');
                    if ( !info ) {
                        console.warn('No data-info attribute for draggable element found');
                        return;
                    }

                    this.trigger(document, 'nodesAdd', {
                        nodes: [{
                            title: text,
                            rowKey: info.rowKey,
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
                minZoom: 0.5,
                maxZoom: 2,
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
                      'width': 18 * scale,
                      'height': 30 * scale,
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
                      'width': 23 * scale,
                      'height': 30 * scale 
                    })
                  .selector('node')
                    .css({
                      'width': 25 * scale,
                      'height': 25 * scale,
                      'content': 'data(title)',
                      'font-family': 'helvetica',
                      'font-size': 14 * scale,
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
                    cy = this;

                    var container = cy.container(),
                        options = cy.options();

                    $(container).cytoscapePanzoom({
                        minZoom: options.minZoom,
                        maxZoom: options.maxZoom
                    }).focus().on({
                        click: function() { this.focus(); },
                        keydown: $this.onKeyHandler.bind($this),
                        keyup: $this.onKeyHandler.bind($this)
                    });

                    var panZoom = $this.select('graphToolsSelector');
                    $this.on(document, 'detailPaneResize', function(e, data) {
                        panZoom.css({
                            right: data.width + 'px'
                        });
                    });
                    $this.on(document, 'addToGraph', $this.onAddToGraph);

                    cy.on({
                        select: $this.graphSelect.bind($this),
                        unselect: $this.graphUnselect.bind($this),
                        grab: $this.graphGrab.bind($this),
                        free: $this.graphFree.bind($this),
                        drag: $this.graphDrag.bind($this)
                    });
                }
            });
        });
    }

});

