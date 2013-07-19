

define([
    'flight/lib/component',
    'cytoscape',
    './renderer',
    'tpl!./graph',
    'util/throttle',
    'util/previews'
], function(defineComponent, cytoscape, Renderer, template, throttle, previews) {
    'use strict';

    var FIT_PADDING = 50;
    var pixelScale = 'devicePixelRatio' in window ? devicePixelRatio : 1;

    return defineComponent(Graph);

    function Graph() {
        var callbackQueue = [];

        this.defaultAttrs({
            cytoscapeContainerSelector: '.cytoscape-container',
            emptyGraphSelector: '.empty-graph',
            graphToolsSelector: '.ui-cytoscape-panzoom',
            contextMenuSelector: '.graph-context-menu',
            contextMenuItemSelector: '.graph-context-menu a',
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
            var options = $.extend({ fit:false }, opts);
            var addedNodes = [];
            var self = this;
            var LAYOUT_OPTIONS = {
                // Customize layout options
                random: { padding: FIT_PADDING },
                arbor: { friction: 0.6, repulsion: 5000 * pixelScale, targetFps: 60, stiffness: 300 }
            };

            this.cy(function(cy) {

               nodes.forEach(function(node) {
                    var title = node.title;
                    if (title.length > 15) {
                        title = title.substring(0, 10) + "...";
                    }

                    if (node.selected != true){
                        node.selected = false;
                    }

                    var cyNodeData = {
                        group: 'nodes',
                        classes: $.trim(node.subType + ' ' + node.type),
                        data: {
                            id: node.rowKey,
                            rowKey: node.rowKey,
                            subType: node.subType,
                            type: node.type,
                            title: title,
                            originalTitle: node.title,
                        },
                        selected: node.selected
                    };

                    var needsUpdate = false;
                    if (node.graphPosition) {
                        cyNodeData.position = self.pointsToPixels(node.graphPosition);
                    } else if (node.dropPosition) {
                        var offset = self.$node.offset();
                        cyNodeData.renderedPosition = self.pointsToPixels({
                            x: node.dropPosition.x - offset.left,
                            y: node.dropPosition.y - offset.top
                        });
                        needsUpdate = true;
                    }

                    var cyNode = cy.add(cyNodeData);

                    if (needsUpdate) {
                        addedNodes.push({
                            rowKey: node.rowKey,
                            graphPosition: self.pixelsToPoints(cyNode.position())
                        });
                    }

                    if (node.type === 'artifact') {
                        previews.generatePreview(node.rowKey, { width:178 * pixelScale }, function(dataUri) {
                            if (dataUri) {
                                cyNode.css('background-image', dataUri);
                            }
                        });
                    }
                });

                var unselected = cy.nodes().filter(':unselected');
                unselected.lock ();
                var opts = $.extend({
                    name:'grid',
                    fit: false,
                    stop: function() {
                        if (unselected) {
                            unselected.unlock();
                        }
                        var updates = $.map(cy.nodes(), function(node) {
                            return {
                                rowKey: node.data('rowKey'),
                                graphPosition: self.pixelsToPoints(node.position())
                            };
                        });
                        self.trigger(document, 'updateNodes', { nodes:updates });
                    }
                }, LAYOUT_OPTIONS['grid'] || {});

                cy.layout(opts);

                if (options.fit && cy.nodes().length) {
                    cy.fit(undefined, FIT_PADDING);
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
                        rowKey: node.data('rowKey'),
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
                        return node.data('rowKey') == nodeToDelete.rowKey; 
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
                                return node.data('rowKey') === updatedNode.rowKey;
                            })
                            .each(function(idx, node) {
                                node.position( self.pointsToPixels(updatedNode.graphPosition) );
                            });
                    });
            });
        };


        this.onContextMenu = function(event) {
            var target = $(event.target),
                name = target.data('func'),
                functionName = name && 'onContextMenu' + name.substring(0, 1).toUpperCase() + name.substring(1),
                func = functionName && this[functionName],
                args = target.data('args');


            if (func) {
                if (!args) {
                    args = [];
                }
                func.apply(this, args);
            } else {
                console.error('No function exists for context menu command: ' + functionName);
            }

            setTimeout(function() {
                target.blur();
                this.select('contextMenuSelector').blur().parent().removeClass('open');
                this.select('nodeContextMenuSelector').blur().parent().removeClass('open');
            }.bind(this), 0);
        };

        this.onContextMenuZoom = function(level) {
            this.cy(function(cy) {
                cy.zoom(level);
            });
        };

        this.onContextMenuLoadRelatedItems = function (){
            var menu = this.select('nodeContextMenuSelector');
            var currentNodeRK = menu.attr('data-currentNode-rowKey');
            var position = {x: menu.attr ('data-currentNode-positionX'), y: menu.attr ('data-currentNode-positionY')};
            var currentNodeOriginalPosition = this.pixelsToPoints(position);
            var data = { rowKey : currentNodeRK,
                         originalPosition: currentNodeOriginalPosition,
                         type : menu.attr("data-currentNode-type")};
            this.trigger (document, 'loadRelatedSelected', data);
                  };

        this.onContextMenuFitToWindow = function() {
            this.cy(function(cy) {
                if( cy.elements().size() === 0 ){
                    cy.reset();
                } else {
                    cy.fit(undefined, FIT_PADDING);
                }
                
                var $container = this.select('cytoscapeContainerSelector');
                var length = Math.max( $container.width(), $container.height() );
                var zoom = cy.zoom() * (length - FIT_PADDING * 2)/length;

                cy.zoom({
                    level: zoom,
                    renderedPosition: {
                        x: $container.width()/2,
                        y: $container.height()/2
                    }
                });

            });
        };

        this.onContextMenuLayout = function(layout, opts) {
            var self = this;
            var options = $.extend({onlySelected:false}, opts);
            var LAYOUT_OPTIONS = {
                // Customize layout options
                random: { padding: FIT_PADDING },
                arbor: { friction: 0.6, repulsion: 5000 * pixelScale, targetFps: 60, stiffness: 300 }
            };
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
                                rowKey: node.data('rowKey'),
                                graphPosition: self.pixelsToPoints(node.position())
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
                menu.attr("data-currentNode-rowkey",event.cyTarget.data('rowKey'));
                menu.attr("data-currentNode-positionX", event.cyTarget.position ('x'));
                menu.attr("data-currentNode-positionY", event.cyTarget.position ('y'));
                menu.attr("data-currentNode-type", event.cyTarget.data('type'));
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

            // TODO: extract this context menu viewport fitting
            var offset = this.$node.offset(),
            padding = 10,
            windowSize = { x: $(window).width(), y: $(window).height() },
            menuSize = { x: menu.outerWidth(), y: menu.outerHeight() },
            submenu = menu.find('li.dropdown-submenu ul'),
            submenuSize = menuSize,// { x:submenu.outerWidth(), y:submenu.outerHeight() },
            placement = {
                left: Math.min(
                    event.originalEvent.pageX - offset.left,
                    windowSize.x - offset.left - menuSize.x - padding
                ),
                top: Math.min(
                    event.originalEvent.pageY - offset.top,
                    windowSize.y - offset.top - menuSize.y - padding
                )
            },
            submenuPlacement = { left:'100%', right:'auto', top:0, bottom:'auto' };

            if ((placement.left + menuSize.x + submenuSize.x + padding) > windowSize.x) {
                submenuPlacement = $.extend(submenuPlacement, { right: '100%', left:'auto' });
            }
            if ((placement.top + menuSize.y + (submenu.children('li').length * 26) + padding) > windowSize.y) {
                submenuPlacement = $.extend(submenuPlacement, { top: 'auto', bottom:'0' });
            }

            menu.parent('div').css($.extend({ position:'absolute' }, placement));
            submenu.css(submenuPlacement);

            menu.dropdown('toggle');
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
                    var p = self.pixelsToPoints(this.position());
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
                var p = self.pixelsToPoints(this.position());
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
                p = self.pixelsToPoints(target.position()),
                originalPosition = target.data('originalPosition'),
                dx = p.x - originalPosition.x,
                dy = p.y - originalPosition.y,
                distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 5) {
                target.select();
                if (distance < 1) {
                    return;
                }
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
                    var start = Date.now ();
                    var relationshipEdges = [];

                    relationshipData.relationships.forEach(function(relationship) {
                        console.log ('relationshipsLoaded');
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
                    console.log ("start",  (Date.now() - start));
                }
            });
        };

        this.pixelsToPoints = function(position) {
            return {
                x: position.x / pixelScale,
                y: position.y / pixelScale
            };
        };

        this.pointsToPixels = function(position) {
            return {
                x: position.x * pixelScale,
                y: position.y * pixelScale
            };
        };

        this.after('initialize', function() {
            var self = this;
            this.$node.html(template({}));


            this.select('contextMenuItemSelector').on('click', this.onContextMenu.bind(this));
            this.select('nodeContextMenuSelector').on('click', this.onContextMenu.bind(this));

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'nodesAdded', this.onNodesAdded);
            this.on(document, 'nodesDeleted', this.onNodesDeleted);
            this.on(document, 'nodesUpdated', this.onNodesUpdated);
            this.on(document, 'relationshipsLoaded', this.onRelationshipsLoaded);

            cytoscape("renderer", "red-dawn", Renderer);
            cytoscape({
                showOverlay: false,
                minZoom: 1 / 4,
                maxZoom: 4,
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
                      'width': 35 * pixelScale,
                      'height': 35 * pixelScale,
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
                      'width': 60 * pixelScale,
                      'height': 60 * 1.2 * pixelScale,
                      'border-color': '#ccc',
                      'border-width': 1
                    })
                  .selector('node.video')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
                      'shape': 'movieStrip',
                      'width': 60 * 1.3 * pixelScale,
                      'height': 60 * pixelScale,
                      'border-color': '#ccc',
                      'border-width': 1
                    })
                  .selector('node.image')
                    .css({
                      'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
                      'shape': 'rectangle',
                      'width': 60 * 1.3 * pixelScale,
                      'height': 60 * pixelScale,
                      'border-color': '#ccc',
                      'border-width': 1
                    })
                  .selector('node')
                    .css({
                      'width': 30 * pixelScale,
                      'height': 30 * pixelScale,
                      'content': 'data(title)',
                      'font-family': 'helvetica',
                      'font-size': 18 * pixelScale,
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
                        maxZoom: options.maxZoom,
                        fitPadding: FIT_PADDING
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
                }
            });
        });
    }

});

