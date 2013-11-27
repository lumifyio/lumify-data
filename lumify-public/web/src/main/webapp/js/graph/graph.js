

define([
    'flight/lib/component',
    'data',
    'cytoscape',
    './renderer',
    './stylesheet',
    './contextmenu/withGraphContextMenuItems',
    'tpl!./graph',
    'util/throttle',
    'util/previews',
    'service/ucd',
    'service/ontology',
    'util/retina',
    'util/withContextMenu'
], function(
    defineComponent,
    appData,
    cytoscape,
    Renderer,
    stylesheet,
    withGraphContextMenuItems,
    template,
    throttle,
    previews,
    UCD,
    OntologyService,
    retina,
    withContextMenu) {
    'use strict';

        // Delay before showing hover effect on graph
    var HOVER_FOCUS_DELAY_SECONDS = 0.25,
        MAX_TITLE_LENGTH = 15,
        SELECTION_THROTTLE = 100,
        // How many edges are required before we don't show them on zoom/pan
        SHOW_EDGES_ON_ZOOM_THRESHOLD = 50,
        GRID_LAYOUT_X_INCREMENT = 175,
        GRID_LAYOUT_Y_INCREMENT = 100;

    return defineComponent(Graph, withContextMenu, withGraphContextMenuItems);

    function Graph() {
        this.ucd = new UCD();
        this.ontologyService = new OntologyService();

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
            vertexContextMenuSelector: '.vertex-context-menu',
            edgeContextMenuSelector: '.edge-context-menu'
        });

        this.cy = function(callback) {
            if ( this.cyLoaded ) {
                callback.call(this, this._cy);
            } else {
                callbackQueue.push( callback );
            }
        };

        this.onVerticesHoveringEnded = function(evt, data) {
            this.cy(function(cy) {
                cy.$('.hover').remove();
            });
        };

        var vertices, idToCyNode;
        this.onVerticesHovering = function(evt, data) {
            if (!this.isWorkspaceEditable) return;
            this.cy(function(cy) {
                var self = this,
                    offset = this.$node.offset(),
                    renderedPosition = retina.pointsToPixels({
                        x: data.position.x - offset.left,
                        y: data.position.y - offset.top
                    }),
                    start = {
                        x:renderedPosition.x,
                        y:renderedPosition.y
                    },
                    inc = GRID_LAYOUT_X_INCREMENT * cy.zoom() * retina.devicePixelRatio,
                    yinc = GRID_LAYOUT_Y_INCREMENT * cy.zoom() * retina.devicePixelRatio,
                    width = inc * 4;

                if (data.start) {
                    idToCyNode = {};
                    data.vertices.forEach(function(v) {
                        idToCyNode[v.id] = cy.getElementById(v.id);
                    });

                    // Sort existing nodes to end, except leave the first
                    // dragging vertex
                    vertices = data.vertices.sort(function(a,b) {
                        var cyA = idToCyNode[a.id], cyB = idToCyNode[b.id];
                        if (data.vertices[0].id === a.id) return -1;
                        if (data.vertices[0].id === b.id) return 1;
                        if (cyA.length && !cyB.length) return 1;
                        if (cyB.length && !cyA.length) return -1;

                        var titleA = a.properties.title.toLowerCase(),
                            titleB = b.properties.title.toLowerCase();

                        return titleA < titleB ? -1 : titleB < titleA ? 1 : 0;
                    });
                }

                vertices.forEach(function(vertex, i) {
                    var tempId = 'NEW-' + vertex.id,
                        node = cy.getElementById(tempId);

                    if (node.length) {
                        node.renderedPosition(renderedPosition);
                    } else {
                        var classes = self.classesForVertex(vertex) + ' hover',
                            cyNode = idToCyNode[vertex.id];

                        if (cyNode.length) {
                            classes += ' existing';
                        }

                        var cyNodeData = {
                            group: 'nodes',
                            classes: classes,
                            data: {
                                id: tempId,
                            },
                            renderedPosition: renderedPosition,
                            selected: false
                        };
                        self.updateCyNodeData(cyNodeData.data, vertex);
                        cy.add(cyNodeData);
                    }

                    renderedPosition.x += inc;
                    if (renderedPosition.x > (start.x + width) || i === 0) {
                        renderedPosition.x = start.x;
                        renderedPosition.y += yinc;
                    }
                });
            });
        };

        this.onVerticesDropped = function(evt, data) {
            if (!this.isWorkspaceEditable) return;
            this.cy(function(cy) {
                var self = this,
                    vertices = data.vertices, 
                    toFitTo = [],
                    toAnimateTo = [],
                    toRemove = [],
                    toAdd = [],
                    position;

                vertices.forEach(function(vertex, i) {
                    var node = cy.getElementById('NEW-' + vertex.id);
                    if (i === 0) position = node.position();
                    if (node.hasClass('existing')) {
                        var existingNode = cy.getElementById(node.id().replace(/^NEW-/, ''));
                        if (existingNode.length) toFitTo.push(existingNode);
                        toAnimateTo.push([node, existingNode]);
                        toFitTo.push(existingNode);
                    } else {
                        vertex.workspace.graphPosition = retina.pixelsToPoints(node.position());
                        toAdd.push(vertex);
                        toRemove.push(node);
                    }
                });

                if (toFitTo.length) {
                    cy.zoomOutToFit(cytoscape.Collection(cy, toFitTo), $.extend({}, this.graphPadding), position, finished);
                    animateToExisting(200);
                } else {
                    animateToExisting(0);
                    finished();
                }

                function animateToExisting(delay) {
                    toAnimateTo.forEach(function(args) {
                        self.animateFromToNode.apply(self, args.concat([delay]));
                    });
                }

                function finished() {
                    cytoscape.Collection(cy, toRemove).remove();
                    self.trigger('addVertices', { vertices:toAdd });
                    cy.container().focus();
                }
            });
        };

        this.onVerticesAdded = function(evt, data) {
            this.addVertices(data.vertices);
        };

        this.addVertices = function(vertices, opts) {
            var options = $.extend({ fit:false }, opts),
                addedVertices = [],
                updatedVertices = [],
                self = this;

            if ($(".instructions").length > 0) {
                $(".instructions").text ('Related Entities Added');
            }

            var dragging = $('.ui-draggable-dragging:not(.clone-vertex)'),
                cloned = null;
            if (dragging.length && this.$node.closest('.visible').length === 1) {
                cloned = dragging.clone()
                    .css({width:'auto'})
                    .addClass('clone-vertex')
                    .insertAfter(dragging);
            }

            this.cy(function(cy) {
                var currentNodes = cy.nodes(),
                    boundingBox = currentNodes.boundingBox(),
                    validBox = isFinite(boundingBox.x1),
                    zoom = cy.zoom(),
                    xInc = GRID_LAYOUT_X_INCREMENT,
                    yInc = GRID_LAYOUT_Y_INCREMENT,
                    nextAvailablePosition = retina.pixelsToPoints({ 
                        x: validBox ? (boundingBox.x1/* + xInc*/) : 0,
                        y: validBox ? (boundingBox.y2/* + yInc*/) : 0
                    });

                nextAvailablePosition.y += yInc;

                var maxWidth = validBox ? retina.pixelsToPoints({ x:boundingBox.w, y:boundingBox.h}).x : 0,
                    startX = nextAvailablePosition.x;

                maxWidth = Math.max(maxWidth, xInc * 10);

                var vertexIds = _.pluck(vertices, 'id'),
                    existingNodes = currentNodes.filter(function(i, n) { return vertexIds.indexOf(n.id()) >= 0; });

                vertices.forEach(function(vertex) {

                    var cyNodeData = {
                        group: 'nodes',
                        classes: self.classesForVertex(vertex),
                        data: {
                            id: vertex.id,
                        },
                        grabbable: self.isWorkspaceEditable,
                        selected: !!vertex.workspace.selected
                    };
                    self.updateCyNodeData(cyNodeData.data, vertex);

                    var needsAdding = false,
                        needsUpdating = false;

                    if (vertex.workspace.graphPosition) {
                        cyNodeData.position = retina.pointsToPixels(vertex.workspace.graphPosition);
                    } else if (vertex.workspace.dropPosition) {
                        var offset = self.$node.offset();
                        cyNodeData.renderedPosition = retina.pointsToPixels({
                            x: vertex.workspace.dropPosition.x - offset.left,
                            y: vertex.workspace.dropPosition.y - offset.top
                        });
                        needsAdding = true;
                    } else {

                        cyNodeData.position = retina.pointsToPixels(nextAvailablePosition);

                        nextAvailablePosition.x += xInc;
                        if((nextAvailablePosition.x - startX) > maxWidth) {
                            nextAvailablePosition.y += yInc;
                            nextAvailablePosition.x = startX;
                        }

                        if (dragging.length === 0) {
                            needsUpdating = true;
                        } else {
                            needsAdding = true;
                        }
                    }

                    var cyNode = cy.add(cyNodeData);

                    if (needsAdding || needsUpdating) {
                        (needsAdding ? addedVertices : updatedVertices).push({
                            id: vertex.id,
                            workspace: {
                                graphPosition: retina.pixelsToPoints(cyNode.position())
                            }
                        });
                    }

                    if (vertex.properties._type === 'artifact' && /^(image|video)$/i.test(vertex.properties._subType)) {
                        _.delay(function() {
                            previews.generatePreview(vertex.properties._rowKey, { width:178 * retina.devicePixelRatio }, function(dataUri) {
                                if (dataUri) {
                                    cyNode.css('background-image', dataUri);
                                }
                            });
                        }, 500);
                    }
                });

                if (options.fit && cy.nodes().length) {
                    _.defer(this.fit.bind(this));
                }

                if (existingNodes.length && cloned && cloned.length) {
                    // Animate to something
                } else if (cloned) cloned.remove();

                if (updatedVertices.length) {
                    this.trigger(document, 'updateVertices', { vertices:updatedVertices });
                } else if (addedVertices.length) {
                    cy.container().focus();
                    this.trigger(document, 'addVertices', { vertices:addedVertices });
                }

            });

            this.setWorkspaceDirty();
        };

        this.classesForVertex = function(vertex) {
            var cls = [];

            if (vertex.properties._subType) cls.push('concept-' + vertex.properties._subType);
            if (vertex.properties._type) cls.push(vertex.properties._type);
            if (vertex.properties._glyphIcon) cls.push('hasCustomGlyph');
            
            return cls.join(' ');
        };

        this.updateCyNodeData = function (data, vertex) {
            var truncatedTitle = vertex.properties.title;

            if (truncatedTitle.length > MAX_TITLE_LENGTH) {
                truncatedTitle = $.trim(truncatedTitle.substring(0, MAX_TITLE_LENGTH)) + "...";
            }

            var merged = $.extend(data, _.pick(vertex.properties, '_rowKey', '_subType', '_type', '_glyphIcon', 'title')); 
            merged.truncatedTitle = truncatedTitle;

            return merged;
        };

        this.onVerticesDeleted = function(event, data) {
            this.cy(function(cy) {

                if (data.vertices.length) {
                    cy.$( 
                        data.vertices.map(function(v) { return '#' + v.id; }).join(',')
                    ).remove();

                    this.setWorkspaceDirty();
                    this.updateVertexSelections(cy);
                }
            });
        };

        this.onObjectsSelected = function(evt, data) {
            if ($(evt.target).is('.graph-pane')) {
                return;
            }

            this.cy(function(cy) {
                this.ignoreCySelectionEvents = true;

                cy.$(':selected').unselect();

                var vertices = data.vertices,
                    edges = data.edges;
                if (vertices.length || edges.length) {
                    cy.$( 
                        vertices.concat(edges).map(function(v) {
                            return '#' + v.id;
                        }).join(',')
                    ).select();
                }

                setTimeout(function() {
                    this.ignoreCySelectionEvents = false;
                }.bind(this), SELECTION_THROTTLE * 1.5);
            });
        };

        this.onVerticesUpdated = function(evt, data) {
            var self = this;
            this.cy(function(cy) {
                data.vertices
                    .forEach(function(updatedVertex) {
                        var cyNode = cy.getElementById(updatedVertex.id);
                        if (cyNode.length) {
                            if (updatedVertex.workspace.graphPosition) {
                                cyNode.position( retina.pointsToPixels(updatedVertex.workspace.graphPosition) );
                            }

                            var newData = self.updateCyNodeData(cyNode.data(), updatedVertex);
                            cyNode.data(newData);
                            if (cyNode._private.classes) {
                                cyNode._private.classes.length = 0;
                            }
                            cyNode.addClass(self.classesForVertex(updatedVertex));
                        }
                    });
            });

            this.setWorkspaceDirty();
        };

        this.animateFromToNode = function(cyFromNode, cyToNode, delay) {
            var self = this,
                cy = cyFromNode.cy();
            
            if (cyToNode && cyToNode.length) {
                cyFromNode
                    .css('opacity', 1.0)
                    .stop(true)
                    .delay(delay)
                    .animate(
                        { 
                            position: cyToNode.position() 
                        }, 
                        { 
                            duration: 700,
                            easing: 'easeOutBack',
                            complete: function() {
                                cyFromNode.remove(); 
                            }
                        }
                    );
            } else {
                cyFromNode.remove();
            }
        };



        this.onContextMenuZoom = function(level) {
            this.cy(function(cy) {
                cy.zoom(level);
            });
        };

        this.onContextMenuLoadRelatedItems = function () {
            var data = this.setupLoadRelatedItems();
            this.onLoadRelatedSelected(data);
        };

        this.onContextMenuLoadRelatedItemsOfConcept = function(conceptId) {
            var data = this.setupLoadRelatedItems();
            data.limitParentConceptId = conceptId;
            this.onLoadRelatedSelected(data);
        };

        this.setupLoadRelatedItems = function() {
            var menu = this.select('vertexContextMenuSelector');
            var currentVertexRK = menu.data('currentVertexRowKey');
            var graphVertexId = menu.data('currentVertexGraphVertexId');
            var position = {x: menu.data ('currentVertexPositionX'), y: menu.data ('currentVertexPositionY')};
            var currentVertexOriginalPosition = retina.pixelsToPoints(position);
            var data = {
                _rowKey: currentVertexRK,
                graphVertexId: graphVertexId,
                originalPosition: currentVertexOriginalPosition,
                _type : menu.data("currentVertexType")
            };
            return data;
        };

        this.onContextMenuDeleteEdge = function () {
            var menu = this.select('edgeContextMenuSelector'),
                edge = {
                    id: menu.data('edge').id,
                    properties: menu.data('edge')
                };

            this.trigger('deleteEdges', { edges:[edge] });
        };

        this.onEdgesDeleted = function (event, data) {
            this.cy(function (cy) {
                cy.remove('#' + data.edgeId);
                this.updateEdgeOptions(cy);
            });
        };

        this.onContextMenuRemoveItem = function (){
            var menu = this.select('vertexContextMenuSelector'),
                vertex = {
                    id: menu.data('currentVertexGraphVertexId')
                };
            this.trigger(document,'deleteVertices', {vertices:[vertex] });
        };

        this.onContextMenuFitToWindow = function() {
            this.fit();
        };

        this.updateEdgeOptions = function(cy) {
            cy.renderer().hideEdgesOnViewport = cy.edges().length > SHOW_EDGES_ON_ZOOM_THRESHOLD;
        };

        this.onDevicePixelRatioChanged = function() {
            this.cy(function(cy) {
                cy.renderer().updatePixelRatio();
                this.fit(cy);
            });
        };

        this.fit = function(cy, nodes) {
            var self = this;

            if (cy) {
                _fit(cy);
            } else {
                this.cy(_fit);
            }

            function _fit(cy) {
                if( cy.elements().size() === 0 ){
                    cy.reset();
                } else if (self.graphPadding) {
                    // Temporarily adjust max zoom 
                    // prevents extreme closeup when one vertex
                    var maxZoom = cy._private.maxZoom;
                    cy._private.maxZoom *= 0.5;
                    cy.panningEnabled(true).zoomingEnabled(true).boxSelectionEnabled(true);
                    cy.fit(nodes, $.extend({}, self.graphPadding));
                    cy._private.maxZoom = maxZoom;
                }
            }
        };

        this.verticesForGraphIds = function(cy, vertexIds) {
            var selector = vertexIds.map(function(vertexId) { 
                return '#' + vertexId; 
            }).join(',');

            return cy.nodes(selector);
        };

        this.onFocusVertices = function(e, data) {
            this.cy(function(cy) {
                var vertexIds = data.vertexIds;
                this.hoverDelay = _.delay(function() {
                    var nodes = this.verticesForGraphIds(cy, vertexIds)
                            .css('borderWidth', 0)
                            .addClass('focus'),
                        start = 5,
                        end = 20;


                    function animate(borderWidth) {
                        if (!nodes.hasClass('focus')) {
                            nodes.css({
                                borderWidth: 0,
                                opacity: 1
                            });
                            return;
                        }

                        nodes.animate({
                                css: { 
                                    borderWidth: borderWidth,
                                    // Opacity         1 -> .75
                                    // borderWidth start -> end
                                    opacity: 1 - ((borderWidth - start) / (end - start) * 0.25)
                                }
                            }, { 
                                duration: 1200,
                                easing: 'easeInOutCirc',
                                complete: function() {
                                    animate(borderWidth === start ? end : start);
                                } 
                            }
                        );
                    }

                    animate(end);
                }.bind(this), HOVER_FOCUS_DELAY_SECONDS * 1000);
            });
        };

        this.onDefocusVertices = function(e, data) {
            clearTimeout(this.hoverDelay);
            this.cy(function(cy) {
                cy.nodes('.focus').removeClass('focus').stop(true, true);
            });
        };

        this.onGraphPaddingUpdated = function(e, data) {
            var border = 20;
            this.graphPaddingRight = data.padding.r;
            this.updatePanZoomLocation();

            var padding = $.extend({}, data.padding);

            padding.r += this.select('graphToolsSelector').outerWidth(true) || 65;
            padding.l += border;
            padding.t += border;
            padding.b += border;
            this.graphPadding = padding;
        };

        this.updatePanZoomLocation = function() {
            if (this.panZoom) {
                this.panZoom.css({ right: (this.graphPaddingRight||0) + 'px' });
            }
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
                        var updates = $.map(cy.nodes(), function(vertex) {
                            return {
                                id: vertex.id(),
                                workspace: {
                                    graphPosition: retina.pixelsToPoints(vertex.position())
                                }
                            };
                        });
                        self.trigger(document, 'updateVertices', { vertices:updates });
                    }
                }, LAYOUT_OPTIONS[layout] || {});

                cy.layout(opts);
            });
        };

        this.graphTap = throttle('selection', SELECTION_THROTTLE, function(event) {
            if (event.cyTarget === event.cy) {
                this.trigger('selectObjects');
            }
        });

        this.graphContextTap = function(event) {
            var menu;

            if (event.cyTarget == event.cy){
                menu = this.select ('contextMenuSelector');
                this.select('vertexContextMenuSelector').blur().parent().removeClass('open');
                this.select('edgeContextMenuSelector').blur().parent().removeClass('open');
            } else if (event.cyTarget.group ('edges') == 'edges') {
                menu = this.select ('edgeContextMenuSelector');
                menu.data("edge", event.cyTarget.data());
                if (event.cy.nodes().filter(':selected').length > 1) {
                    return false;
                }
                this.select('vertexContextMenuSelector').blur().parent().removeClass('open');
                this.select('contextMenuSelector').blur().parent().removeClass('open');
            } else {
                menu = this.select ('vertexContextMenuSelector');
                menu.data("currentVertexRowKey",event.cyTarget.data('_rowKey'));
                menu.data("currentVertexGraphVertexId", event.cyTarget.id());
                menu.data("currentVertexPositionX", event.cyTarget.position ('x'));
                menu.data("currentVertexPositionY", event.cyTarget.position ('y'));
                menu.data("currentVertexType", event.cyTarget.data('_type'));
                menu.data("currentVertexSubtype", event.cyTarget.data('_subType'));
                this.select('contextMenuSelector').blur().parent().removeClass('open');
                this.select('edgeContextMenuSelector').blur().parent().removeClass('open');
            }

            // Show/Hide the layout selection menu item
            if (event.cy.nodes().filter(':selected').length) {
                menu.find('.layout-multi').show();
            } else {
                menu.find('.layout-multi').hide();
            }

            this.toggleMenu({positionUsingEvent:event}, menu);
        };

        this.graphSelect = throttle('selection', SELECTION_THROTTLE, function(event) {
            if (this.ignoreCySelectionEvents) return;
            if (this.creatingStatement) {
                return event.cy.elements().unselect();
            }
            this.updateVertexSelections(event.cy);
        });

        this.graphUnselect = throttle('selection', SELECTION_THROTTLE, function(event) {
            if (this.ignoreCySelectionEvents) return;

            var self = this,
                selection = event.cy.nodes().filter(':selected');

            if (!selection.length) {
                self.trigger('selectObjects');
            }
        });

        this.updateVertexSelections = function(cy) {
            var nodes = cy.nodes().filter(':selected'),
                edges = cy.edges().filter(':selected'),
                vertices = [];

            nodes.each(function(index, cyNode) {
                vertices.push(appData.vertex(cyNode.id()));
            });

            edges.each(function(index, cyEdge) {
                vertices.push({ id: cyEdge.id(), properties:cyEdge.data() });
            });

            // Only allow one edge selected
            if (nodes.length === 0 && edges.length > 1) {
                vertices = [vertices[0]];
            }
            if (vertices.length > 0){
                this.trigger('selectObjects', { vertices:vertices });
            } else {
                this.trigger('selectObjects');
            }
        };

        this.graphGrab = function(event) {
            var self = this;
            this.cy(function(cy) {
                var vertices = event.cyTarget.selected() ? cy.nodes().filter(':selected') : event.cyTarget;
                this.grabbedVertices = vertices.each(function() {
                    var p = retina.pixelsToPoints(this.position());
                    this.data('originalPosition', { x:p.x, y:p.y });
                    this.data('freed', false );
                });
            });
        };

        this.graphFree = function(event) {
            if (!this.isWorkspaceEditable) return;
            var self = this;

            // CY is sending multiple "free" events, prevent that...
            var dup = true,
                vertices = this.grabbedVertices;

            vertices.each(function(i, e) {
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
            vertices.each(function(i, e) {
                originalPositions.push( e.data('originalPosition') );
                targetPositions.push( e.data('targetPosition') );
            });

            var graphMovedVerticesData = {
                vertices: $.map(vertices, function(vertex) {
                    return {
                        id: vertex.id(),
                        workspace: {
                            graphPosition: vertex.data('targetPosition')
                        }
                    };
                })
            };
            self.trigger(document, 'updateVertices', graphMovedVerticesData);

            this.setWorkspaceDirty();
        };

        this.setWorkspaceDirty = function() {
            this.checkEmptyGraph();
        };

        this.checkEmptyGraph = function() {
            this.cy(function(cy) {
                var noVertices = cy.nodes().length === 0;

                this.select('emptyGraphSelector').toggle(noVertices);
                cy.panningEnabled(!noVertices)
                    .zoomingEnabled(!noVertices)
                    .boxSelectionEnabled(!noVertices);

                this.select('graphToolsSelector').toggle(!noVertices);
                if (noVertices) {
                    cy.reset();
                }

                this.updateEdgeOptions(cy);
            });
        };

        this.resetGraph = function() {
            this.cy(function(cy) {
                cy.nodes().remove();
                this.setWorkspaceDirty();
            });
        };

        this.onWorkspaceLoaded = function(evt, workspace) {
            this.resetGraph();
            this.isWorkspaceEditable = workspace.isEditable;
            if (workspace.data.vertices.length) {
                this.addVertices(workspace.data.vertices, { fit:(this.previousWorkspace && this.previousWorkspace != workspace.id) });
            } else this.checkEmptyGraph();

            this.previousWorkspace = workspace.id;
        };

        this.onRelationshipsLoaded = function(evt, relationshipData) {
            this.cy(function(cy) {
                if (relationshipData.relationships) {
                    var relationshipEdges = [];
                    relationshipData.relationships.forEach(function(relationship) {
                        relationshipEdges.push ({
                            group: "edges",
                            data: {
                                _rowKey: relationship.from + "->" + relationship.to,
                                relationshipType: relationship.relationshipType,
                                source: relationship.from,
                                target: relationship.to,
                                _type: 'relationship',
                                id: (relationship.from + '-' + relationship.to + '-' + relationship.relationshipType)
                            },
                        });
                    });

                    // Hide edges when zooming if more than threshold
                    cy.add(relationshipEdges);
                    this.updateEdgeOptions(cy);
                }
            });
        };


        this.onLoadRelatedSelected = function(data) {
            var instructions = $('<div>')
                .text("Loading...")
                .addClass('instructions')
                .appendTo(this.$node);
            var self = this;

            if ($.isArray(data) && data.length == 1){
                data = data[0];
            }

            this.ucd.getRelatedVertices(data)
                .done(function(data) {
                    var added = data.vertices;
                    
                    self.cy(function(cy) {
                        cy.filter(':selected').unselect();
                        cy.container().focus();
                        added.forEach(function(vertex, index) {
                            vertex.workspace = {
                                selected: true
                            };
                        });

                        self.trigger(document, 'addVertices', { vertices: added });
                        self.trigger('selectObjects', { vertices:added })
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

        this.after('teardown', function() {
            this.$node.empty();
        });

        this.after('initialize', function() {
            var self = this;
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'verticesHovering', this.onVerticesHovering);
            this.on(document, 'verticesHoveringEnded', this.onVerticesHoveringEnded);
            this.on(document, 'verticesAdded', this.onVerticesAdded);
            this.on(document, 'verticesDropped', this.onVerticesDropped);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'objectsSelected', this.onObjectsSelected);
            this.on(document, 'relationshipsLoaded', this.onRelationshipsLoaded);
            this.on(document, 'graphPaddingUpdated', this.onGraphPaddingUpdated);
            this.on(document, 'devicePixelRatioChanged', this.onDevicePixelRatioChanged);
            this.on(document, 'menubarToggleDisplay', this.onMenubarToggleDisplay);
            this.on(document, 'focusVertices', this.onFocusVertices);
            this.on(document, 'defocusVertices', this.onDefocusVertices);
            this.on(document, 'edgesDeleted', this.onEdgesDeleted);

            if (self.attr.vertices && self.attr.vertices.length) {
                this.select('emptyGraphSelector').hide();
                this.addVertices(self.attr.vertices);
            }

            this.ontologyService.concepts(function(err, concepts) {
                if (err) {
                    console.error('concepts', err);
                    return self.trigger(document, 'error', err);
                }

                var templateData = {
                    firstLevelConcepts: concepts.entityConcept.children || [],
                    artifactConcept: concepts.artifactConcept,
                    pathHopOptions: ["2","3","4"]
                };
                self.$node.html(template(templateData));
                self.bindContextMenuClickEvent();
                self.checkEmptyGraph();

                stylesheet(function(style) {
                    self.initializeGraph(style);
                });
            });
        });

        this.initializeGraph = function(style) {
            var self = this;

            cytoscape("renderer", "lumify", Renderer);
            cytoscape({
                showOverlay: false,
                minZoom: 1 / 4,
                maxZoom: 4,
                graphPaperEnabled: false, // :( sorry chris
                container: this.select('cytoscapeContainerSelector').css({height:'100%'})[0],
                renderer: {
                    name: 'lumify'
                },
                style: style,

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
                        click: function() {
                            $(".instructions").remove();
                        }
                    });

                    // Override "Fit to Window" button and call our own
                    $('.ui-cytoscape-panzoom-reset').on('mousedown', function(e) {
						if (e.button !== 0) return;
                        e.stopPropagation();
                        self.fit(cy);
                    });

                    self.panZoom = self.select('graphToolsSelector');
                    self.updatePanZoomLocation();
                    
                    cy.on({
                        tap: self.graphTap.bind(self),
                        cxttap: self.graphContextTap.bind(self),
                        select: self.graphSelect.bind(self),
                        unselect: self.graphUnselect.bind(self),
                        grab: self.graphGrab.bind(self),
                        free: self.graphFree.bind(self)
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
        };
    }

});

