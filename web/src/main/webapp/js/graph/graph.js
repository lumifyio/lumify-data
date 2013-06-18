

define([
    'flight/lib/component',
    'cytoscape',
    './renderer',
    'tpl!./graph',
    'util/undoManager'
], function(defineComponent, cytoscape, Renderer, template, undoManager) {
    'use strict';

    return defineComponent(Graph);

    function Graph() {
        var cy = null;

        this.defaultAttrs({
            cytoscapeContainerSelector: '.cytoscape-container',
            emptyGraphSelector: '.empty-graph',
            graphToolsSelector: '.ui-cytoscape-panzoom'
        });

        this.onGraphAddNode = function(evt, data) {
            this.addNode(data);
        };

        this.addNode = function(node) {
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

            undoManager.performedAction( 'Add node: ' + title, {
                undo: function() {
                    cyNode.remove();
                    this.setWorkspaceDirty();
                },
                redo: function() {
                    cyNode.restore();
                    this.setWorkspaceDirty();
                },
                bind: this
            });

            this.setWorkspaceDirty();
        };

        this.removeSelectedNodes = function() {
            var nodes = cy.nodes().filter(':selected').remove();

            undoManager.performedAction( 'Delete ' + nodes.length + ' nodes', {
                undo: function() {
                    nodes.restore();
                    this.setWorkspaceDirty();
                },
                redo: function() {
                    nodes.remove();
                    this.setWorkspaceDirty();
                },
                bind: this
            });
            this.setWorkspaceDirty();
        };

        this.onAddToGraph = function(event, data) {
            var el = $(event.target),
                p = el.offset(),
                c = this.$node.offset(),
                position = {
                    x: p.left - c.left + el.width() / 2.0, 
                    y: p.top - c.top + el.height() / 2.0
                };

            this.addNode(data.text, data.info, position); 
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
                $this.trigger(document, 'graphNodeMoved', {
                    id: e.data('id'),
                    x: p.x,
                    y: p.y
                });
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
            undoManager.performedAction( 'Move ' + nodes.length + ' nodes', {
                undo: function() {
                    nodes.each(function(i, e) {
                        e.position( originalPositions[i] );
                    });
                    this.setWorkspaceDirty();
                },
                redo: function() {
                    nodes.each(function(i, e) {
                        e.position( targetPositions[i] );
                    });
                    this.setWorkspaceDirty();
                },
                bind: this
            });


            this.setWorkspaceDirty();
        };

        this.setWorkspaceDirty = function() {
            this.checkEmptyGraph();
        };

        this.checkEmptyGraph = function() {
            this.select('emptyGraphSelector').toggle(cy.nodes().length === 0);
        };

        this.onWorkspaceLoaded = function(evt, workspace) {
            if (workspace.data.nodes) {
                for(var i=0; i<workspace.data.nodes.length; i++) {
                    this.addNode(workspace.data.nodes[i]);
                }
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

        this.after('initialize', function() {
            var $this = this;
            this.$node.html(template({}));

            this.$node.droppable({
                drop: function( event, ui ) {
                    var draggable = ui.draggable,
                        droppableOffset = $(event.target).offset(),
                        text = draggable.text();

                    var info = draggable.parents('li').data('info');

                    this.trigger(document, 'graphAddNode', {
                        title: text,
                        rowKey: info.rowKey,
                        subType: info.subType,
                        type: info.type,
                        graphPosition: {
                            x: event.clientX - droppableOffset.left,
                            y: event.clientY - droppableOffset.top
                        }
                    });
                }.bind(this)
            });

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'graphAddNode', this.onGraphAddNode);
            this.on(document, 'relationshipsLoaded', this.onRelationshipsLoaded);

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
                      'width': 18,
                      'height': 30,
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
                      'width': 23,
                      'height': 30
                    })
                  .selector('node')
                    .css({
                      'content': 'data(title)',
                      'font-family': 'helvetica',
                      'font-size': 14,
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

