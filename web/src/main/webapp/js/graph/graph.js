

define([
    'flight/lib/component',
    'cytoscape',
    './renderer',
    'tpl!./graph',
    'util/throttle',
    'util/previews',
    'service/ucd',
    'util/retina',
    'util/withContextMenu'
], function(
    defineComponent,
    cytoscape,
    Renderer,
    template,
    throttle,
    previews,
    UCD,
    retina,
    withContextMenu) {
    'use strict';

    return defineComponent(Graph, withContextMenu);

    function Graph() {
        this.ucd = new UCD();

        var callbackQueue = [];
        var LAYOUT_OPTIONS = {
            // Customize layout options
            random: { padding: 10 },
            arbor: { friction: 0.6, repulsion: 5000 * retina.devicePixelRatio, targetFps: 60, stiffness: 300 }
        };

        this.defaultAttrs({
            cytoscapeContainerSelector: '.cytoscape-container',
            emptyGraphSelector: '.empty-graph',
            graphToolsSelector: '.ui-cytoscape-panzoom',
            contextMenuSelector: '.graph-context-menu',
            nodeContextMenuSelector: '.node-context-menu'
        });

        this.cy = function(callback) {
            if ( this.cyLoaded ) {
                callback.call(this, this._cy);
            } else {
                callbackQueue.push( callback );
            }
        };

        this.onNodesAdded = function(evt, data) {
            this.addNodes(data.nodes);
        };

        this.addNodes = function(nodes, opts) {
            console.log('addNodes:', nodes);
            var options = $.extend({ fit:false }, opts);
            var addedNodes = [];
            var self = this;

            this.cy(function(cy) {
                var existingNodes = $.map(cy.nodes(), function (node){
                    node.lock();
                });
                var opts = $.extend({
                    name:'grid',
                    fit: false,
                    stop: function() {
                        $.map(cy.nodes(), function (node) {
                            node.unlock();
                        });
                        var updates = $.map(cy.nodes(), function(node) {
                            return {
                                graphNodeId: node.data('graphNodeId'),
                                graphPosition: retina.pixelsToPoints(node.position())
                            };
                        });
                        self.trigger(document, 'updateNodes', { nodes:updates });
                    }
                }, LAYOUT_OPTIONS['grid'] || {});

                cy.layout(opts);

                nodes.forEach(function(node) {
                    console.log('adding node:', node);
                    var title = node.title || 'unknown';
                    if (title.length > 15) {
                        title = title.substring(0, 10) + "...";
                    }

                    var cyNodeData = {
                        group: 'nodes',
                        classes: $.trim(node.subType + ' ' + node.type),
                        data: {
                            id: node.graphNodeId,
                            rowKey: node.rowKey || node.rowkey,
                            graphNodeId: node.graphNodeId,
                            subType: node.subType,
                            type: node.type,
                            title: title,
                            originalTitle: node.title,
                        },
                        selected: !!node.selected
                    };

                    var needsUpdate = false;
                    if (node.graphPosition) {
                        cyNodeData.position = retina.pointsToPixels(node.graphPosition);
                    } else if (node.dropPosition) {
                        var offset = self.$node.offset();
                        cyNodeData.renderedPosition = retina.pointsToPixels({
                            x: node.dropPosition.x - offset.left,
                            y: node.dropPosition.y - offset.top
                        });
                        needsUpdate = true;
                    }

                    var cyNode = cy.add(cyNodeData);

                    if (needsUpdate) {
                        addedNodes.push({
                            graphNodeId: node.graphNodeId,
                            graphPosition: retina.pixelsToPoints(cyNode.position())
                        });
                    }

                    if (node.type === 'artifact') {
                        previews.generatePreview(node.rowKey, { width:178 * retina.devicePixelRatio }, function(dataUri) {
                            if (dataUri) {
                                cyNode.css('background-image', dataUri);
                            }
                        });
                    }
                });

                if (options.fit && cy.nodes().length) {
                    this.fit();
                }

                if (addedNodes.length) {
                    this.trigger(document, 'updateNodes', { nodes:addedNodes });
                }

                this.setWorkspaceDirty();
            });
        };

        this.removeSelectedNodes = function() {
            this.cy(function(cy) {
                var nodesToDelete = $.map(cy.nodes().filter(':selected'), function(node) {
                    return {
                        graphNodeId: node.data('graphNodeId'),
                        type: node.data('type'),
                        subType: node.data('subType')
                    };
                });

                this.trigger(document, 'deleteNodes', { nodes: nodesToDelete });
            });
        };

        this.onNodesDeleted = function(event, data) {
            this.cy(function(cy) {
                var matchingNodes = cy.nodes().filter(function(idx, node) {
                    return data.nodes.filter(function(nodeToDelete) { 
                        return node.data('graphNodeId') == nodeToDelete.graphNodeId; 
                    }).length > 0;
                });
                matchingNodes.remove();
                this.setWorkspaceDirty();

                this.updateNodeSelections(cy);
            });
        };

        this.onNodesUpdated = function(evt, data) {
            var self = this;
            this.cy(function(cy) {
                data.nodes
                    .filter(function(updatedNode) { return updatedNode.graphPosition; })
                    .forEach(function(updatedNode) {
                        cy.nodes()
                            .filter(function(idx, node) {
                                return node.data('graphNodeId') === updatedNode.graphNodeId;
                            })
                            .each(function(idx, node) {
                                node.position( retina.pointsToPixels(updatedNode.graphPosition) );
                            });
                    });
            });
        };



        this.onContextMenuZoom = function(level) {
            this.cy(function(cy) {
                cy.zoom(level);
            });
        };

        this.onContextMenuLoadRelatedItems = function () {
            var menu = this.select('nodeContextMenuSelector');
            var currentNodeRK = menu.data('currentNodeRowKey');
            var graphNodeId = menu.data('currentNodeGraphNodeId');
            var position = {x: menu.data ('currentNodePositionX'), y: menu.data ('currentNodePositionY')};
            var currentNodeOriginalPosition = retina.pixelsToPoints(position);
            var data = {
                rowKey: currentNodeRK,
                graphNodeId: graphNodeId,
                originalPosition: currentNodeOriginalPosition,
                type : menu.data("currentNodeType")
            };
            this.onLoadRelatedSelected(data);
        };

        this.onContextMenuConnect = function() {
            var menu = this.select('nodeContextMenuSelector');
            var graphNodeId = menu.data('currentNodeGraphNodeId');

            this.cy(function(cy) {
                var edge = null,
                    input = null,
                    complete = function(val) {
                        if (val) {
                            console.log(val);
                        }
                        cy.remove(edge);
                        edge = null;
                        cy.off(tapEvents);
                        cy.panningEnabled(true)
                          .zoomingEnabled(true)
                          .boxSelectionEnabled(true);
                        input.remove();
                    },
                    mouseEvents = {
                        mouseover: function(event) {
                            if (event.cy == event.cyTarget) return;
                            if (event.cyTarget.id() === graphNodeId) return;

                            edge = cy.add({
                              group: 'edges',
                              classes: 'temp',
                              data: {
                                  source: graphNodeId,
                                  target: event.cyTarget.id()
                              }
                            });
                        },
                        mouseout: function(event) {
                            if (edge && !edge.hasClass('label')) {
                                cy.remove(edge);
                                edge = null;
                            }
                        }
                    },
                    tapEvents = {
                        tap: function(event) {
                            if (edge) {
                                if (edge.hasClass('label')) {
                                    complete();
                                } else {
                                    cy.off(mouseEvents);

                                    var srcPosition = retina.pixelsToPoints(cy.getElementById(edge.data('source')).renderedPosition()),
                                        dstPosition = retina.pixelsToPoints(cy.getElementById(edge.data('target')).renderedPosition()),
                                        center = {
                                            left: (dstPosition.x - srcPosition.x) / 2 + srcPosition.x,
                                            top: (dstPosition.y - srcPosition.y) / 2 + srcPosition.y
                                        };

                                    cy.panningEnabled(false)
                                        .zoomingEnabled(false)
                                        .boxSelectionEnabled(false);

                                    input = $('<input placeholder="Enter label" type="text">')
                                        .css({
                                            left: (center.left - 50) + 'px',
                                            top: (center.top - 15) + 'px',
                                            width: '100px',
                                            position: 'absolute',
                                            zIndex: 100,
                                            textAlign: 'center'
                                        })
                                        .appendTo(document.body)
                                        .on({
                                            keydown: function(e) {
                                                if (e.which === $.ui.keyCode.TAB) {
                                                    e.preventDefault();
                                                    return false;
                                                }

                                                if (e.which === $.ui.keyCode.ENTER) {
                                                    complete($(this).val());
                                                }
                                            }
                                        });
                                    _.defer(input.focus.bind(input));
                                    edge.addClass('label');
                                }
                            }
                        }
                    };

                cy.on(mouseEvents);
                cy.on(tapEvents);
            });
        };

        this.onContextMenuFitToWindow = function() {
            this.fit();
        };


        this.fit = function() {
            this.trigger(document, 'requestGraphPadding');
        };

        this.onGraphPadding = function(e, data) {
            this.cy(function(cy) {
                if( cy.elements().size() === 0 ){
                    cy.reset();
                } else {
                    var border = 20;
                    data.padding.r += this.select('graphToolsSelector').outerWidth(true);
                    data.padding.l += border;
                    data.padding.t += border;
                    data.padding.b += border;
                    cy.fit(undefined, data.padding);
                }
            });
        };

        this.onContextMenuLayout = function(layout, opts) {
            var self = this;
            var options = $.extend({onlySelected:false}, opts);
            this.cy(function(cy) {

                var unselected;
                if (options.onlySelected) {
                    unselected = cy.nodes().filter(':unselected');
                    unselected.lock();
                }

                var opts = $.extend({
                    name:layout,
                    fit: false,
                    stop: function() {
                        if (unselected) {
                            unselected.unlock();
                        }
                        var updates = $.map(cy.nodes(), function(node) {
                            return {
                                graphNodeId: node.data('graphNodeId'),
                                graphPosition: retina.pixelsToPoints(node.position())
                            };
                        });
                        self.trigger(document, 'updateNodes', { nodes:updates });
                    }
                }, LAYOUT_OPTIONS[layout] || {});

                cy.layout(opts);
            });
        };

        this.graphTap = throttle('selection', 100, function(event) {
            if (event.cyTarget === event.cy) {
                this.trigger(document, 'searchResultSelected');
            }
        });

        this.graphContextTap = function(event) {
            var menu;
            if (event.cyTarget == event.cy){
                menu = this.select ('contextMenuSelector');
                this.select('nodeContextMenuSelector').blur().parent().removeClass('open');
            } else {
                menu = this.select ('nodeContextMenuSelector');
                menu.data("currentNodeRowKey",event.cyTarget.data('rowKey'));
                menu.data("currentNodeGraphNodeId",event.cyTarget.data('graphNodeId'));
                menu.data("currentNodePositionX", event.cyTarget.position ('x'));
                menu.data("currentNodePositionY", event.cyTarget.position ('y'));
                menu.data("currentNodeType", event.cyTarget.data('type'));
                if (event.cy.nodes().filter(':selected').length > 1) {
                    return false;
                }
                this.select('contextMenuSelector').blur().parent().removeClass('open');
            }

            // Show/Hide the layout selection menu item
            if (event.cy.nodes().filter(':selected').length) {
                menu.find('.layout-multi').show();
            } else {
                menu.find('.layout-multi').hide();
            }

            this.toggleMenu({positionUsingEvent:event}, menu);
        };

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
            var selection = cy.nodes().filter(':selected');
            var edgeSelection = cy.edges().filter(':selected');
            var info = [];

            console.log('selections: ', selection, edgeSelection);
            selection.each(function(index, node) {
                info.push(node.data());
            });
            edgeSelection.each(function(index, node) {
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
            var self = this;
            this.cy(function(cy) {
                var nodes = event.cyTarget.selected() ? cy.nodes().filter(':selected') : event.cyTarget;
                this.grabbedNodes = nodes.each(function() {
                    var p = retina.pixelsToPoints(this.position());
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
                var p = retina.pixelsToPoints(this.position());
                if ( !e.data('freed') ) {
                    dup = false;
                }
                e.data('targetPosition', {x:p.x, y:p.y});
                e.data('freed', true);
            });

            if (dup) {
                return;
            }

            // If the user didn't drag more than a few pixels, select the
            // object, it could be an accidental mouse move
            var target = event.cyTarget, 
                p = retina.pixelsToPoints(target.position()),
                originalPosition = target.data('originalPosition'),
                dx = p.x - originalPosition.x,
                dy = p.y - originalPosition.y,
                distance = Math.sqrt(dx * dx + dy * dy);

            if (distance === 0) return;
            if (distance < 5) {
                if (!event.originalEvent.shiftKey) {
                    event.cy.$(':selected').unselect();
                }
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
                        graphNodeId: node.data('id'),
                        graphPosition: node.data('targetPosition')
                    };
                })
            };
            self.trigger(document, 'updateNodes', graphMovedNodesData);

            this.setWorkspaceDirty();
        };

        this.setWorkspaceDirty = function() {
            this.checkEmptyGraph();
        };

        this.checkEmptyGraph = function() {
            this.cy(function(cy) {
                var noNodes = cy.nodes().length === 0;

                this.select('emptyGraphSelector').toggle(noNodes);
                cy.panningEnabled(!noNodes)
                    .zoomingEnabled(!noNodes)
                    .boxSelectionEnabled(!noNodes)
                    .graphPaperEnabled(!noNodes);

                if (noNodes) {
                    cy.reset();
                }
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
                workspace.data.nodes.forEach(function(node){
                    node.selected = false;
                });
                this.addNodes(workspace.data.nodes, { fit:true });
            }

            this.checkEmptyGraph();
        };

        this.onRelationshipsLoaded = function(evt, relationshipData) {
            this.cy(function(cy) {
                if (relationshipData.relationships != null){
                    var relationshipEdges = [];

                    relationshipData.relationships.forEach(function(relationship) {
                        relationshipEdges.push ({
                            group: "edges",
                            data: {
                                rowKey: relationship.from + "->" + relationship.to,
                                relationshipType: relationship.relationshipType,
                                source: relationship.from,
                                target: relationship.to,
                                type: 'relationship',
                                id: (relationship.from < relationship.to ? relationship.from + relationship.to : relationship.to + relationship.from)
                            },
                            classes: (relationship.bidirectional ? 'bidirectional' : '')
                        });

                    });
                    cy.add(relationshipEdges);
                }
            });
        };

        this.onLoadRelatedSelected = function(data) {
            var self = this;

            if ($.isArray(data) && data.length == 1){
                data = data[0];
            }

            console.log('Getting related nodes for:', data);

            var xOffset = 100, yOffset = 100;
            var x = data.originalPosition.x;
            var y = data.originalPosition.y;

            this.ucd.getRelatedNodes(data.graphNodeId, function(err, nodes) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                nodes = nodes.nodes;

                nodes = nodes.map(function(node, index) {
                    if (index % 10 === 0) {
                        y += yOffset;
                    }
                    return {
                        graphNodeId: node.id,
                        type: node.properties.type,
                        subType: node.properties.subType,
                        title: node.properties.title,
                        rowKey: node.properties.rowKey,
                        graphPosition: {
                            x: x + xOffset * (index % 10 + 1),
                            y: y
                        },
                        selected: true
                    };
                });

                console.log('trigger nodes', nodes);

                self.trigger(document, 'addNodes', {
                    nodes: nodes
                });
            });
        };

        this.onMenubarToggleDisplay = function(e, data) {
            if (data.name === 'graph') {
                this.cy(function(cy) {
                    cy.renderer().notify({type:'viewport'});
                });
            }
        };

        this.after('initialize', function() {
            var self = this;
            this.$node.html(template({}));

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'nodesAdded', this.onNodesAdded);
            this.on(document, 'nodesDeleted', this.onNodesDeleted);
            this.on(document, 'nodesUpdated', this.onNodesUpdated);
            this.on(document, 'relationshipsLoaded', this.onRelationshipsLoaded);
            this.on(document, 'graphPaddingResponse', this.onGraphPadding);
            this.on(document, 'menubarToggleDisplay', this.onMenubarToggleDisplay);

            cytoscape("renderer", "red-dawn", Renderer);
            cytoscape({
                showOverlay: false,
                minZoom: 1 / 4,
                maxZoom: 4,
                hideEdgesOnViewport: true,
                container: this.select('cytoscapeContainerSelector').css({height:'100%'})[0],
                renderer: {
                    name: 'red-dawn'
                },
                style: cytoscape.stylesheet()
                  // TODO: get the list of types and subTypes
                  .selector('node.person')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_003_user@2x.png'
                    })
                  .selector('node.location,node.place')
                    .css({
                      'background-image': '/img/pin@2x.png',
                      'width': 35 * retina.devicePixelRatio,
                      'height': 35 * retina.devicePixelRatio,
                      'border-color': 'white',
                      'shape': 'none',
                      'border-width': 0
                    })
                  .selector('node.organization,node.organisation')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_263_bank@2x.png',
                      'shape': 'roundrectangle'
                    })
                  .selector('node.document')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
                      'shape': 'rectangle',
                      'width': 60 * retina.devicePixelRatio,
                      'height': 60 * 1.2 * retina.devicePixelRatio,
                      'border-color': '#ccc',
                      'border-width': 1
                    })
                  .selector('node.video')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
                      'shape': 'movieStrip',
                      'width': 60 * 1.3 * retina.devicePixelRatio,
                      'height': 60 * retina.devicePixelRatio,
                      'border-color': '#ccc',
                      'border-width': 1
                    })
                  .selector('node.image')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
                      'shape': 'rectangle',
                      'width': 60 * 1.3 * retina.devicePixelRatio,
                      'height': 60 * retina.devicePixelRatio,
                      'border-color': '#ccc',
                      'border-width': 1
                    })
                  .selector('node')
                    .css({
                      'width': 30 * retina.devicePixelRatio,
                      'height': 30 * retina.devicePixelRatio,
                      'content': 'data(title)',
                      'font-family': 'helvetica',
                      'font-size': 18 * retina.devicePixelRatio,
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
                    })
                  .selector('edge.label')
                    .css({
                      'content': 'data(label)',
                      'font-size': 14 * retina.devicePixelRatio,
                      'text-outline-color': 'white',
                      'text-outline-width': 4,
                    })
                  .selector('edge.temp')
                    .css({
                      'width': 4,
                      'line-color': '#0088cc',
                      'line-style': 'dotted',
                      'target-arrow-color': '#0088cc'
                    })
                  .selector('.bidirectional')
                    .css ({
                      'width': 2,
                      'target-arrow-shape': 'triangle',
                      'source-arrow-shape': 'triangle'
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

                    // Override "Fit to Window" button and call our own
                    $('.ui-cytoscape-panzoom-reset').on('mousedown', function(e) {
						if (e.button !== 0) return;
                        e.stopPropagation();
                        self.fit();
                    });

                    var panZoom = self.select('graphToolsSelector');
                    self.on(document, 'detailPaneResize', function(e, data) {
                        panZoom.css({
                            right: data.width + 'px'
                        });
                    });

                    cy.on({
                        tap: self.graphTap.bind(self),
                        cxttap: self.graphContextTap.bind(self),
                        select: self.graphSelect.bind(self),
                        unselect: self.graphUnselect.bind(self),
                        grab: self.graphGrab.bind(self),
                        free: self.graphFree.bind(self),
                        drag: self.graphDrag.bind(self)
                    });

                },
                done: function() {
                    self.cyLoaded = true;
                    self.drainCallbackQueue();

                    
                    setTimeout(function() {
                        self.fit();
                    }, 100);
                }
            });
        });
    }

});

