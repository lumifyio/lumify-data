

define([
    'flight/lib/component',
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
    'util/withContextMenu',
    'underscore'
], function(
    defineComponent,
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
    withContextMenu,
    _) {
    'use strict';

        // Delay before showing hover effect on graph
    var HOVER_FOCUS_DELAY_SECONDS = 0.25,
        MAX_TITLE_LENGTH = 15,
        SELECTION_THROTTLE = 100;

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

        this.onVerticesAdded = function(evt, data) {
            this.addVertices(data.vertices);
        };

        this.addVertices = function(vertices, opts) {
            var options = $.extend({ fit:false }, opts);
            var addedVertices = [];
            var self = this;

            if ($(".instructions").length > 0) {
                $(".instructions").text ('Related Entities Added');
            }

            this.cy(function(cy) {
                var boundingBox = cy.nodes().boundingBox(),
                    validBox = isFinite(boundingBox.x1),
                    inc = 200,
                    nextAvailablePosition = retina.pixelsToPoints({ 
                        x: validBox ? (boundingBox.x1 + inc/2) : 0,
                        y: validBox ? (boundingBox.y2 + inc) : 0
                    }),
                    maxWidth = validBox ? retina.pixelsToPoints({ x:boundingBox.w, y:boundingBox.h}).x : 0,
                    startX = nextAvailablePosition.x;

                maxWidth = Math.max(maxWidth, inc * 10);

                vertices.forEach(function(vertex) {

                    var cyNodeData = {
                        group: 'nodes',
                        classes: self.classesForVertex(vertex),
                        data: {
                            id: vertex.graphVertexId,
                        },
                        selected: !!vertex.selected
                    };
                    self.updateCyNodeData(cyNodeData.data, vertex);

                    var needsUpdate = false;
                    if (vertex.graphPosition) {
                        cyNodeData.position = retina.pointsToPixels(vertex.graphPosition);
                    } else if (vertex.dropPosition) {
                        var offset = self.$node.offset();
                        cyNodeData.renderedPosition = retina.pointsToPixels({
                            x: vertex.dropPosition.x - offset.left,
                            y: vertex.dropPosition.y - offset.top
                        });
                        needsUpdate = true;
                    } else {

                        cyNodeData.position = retina.pointsToPixels(nextAvailablePosition);

                        nextAvailablePosition.x += inc;
                        if((nextAvailablePosition.x - startX) > maxWidth) {
                            nextAvailablePosition.y += inc;
                            nextAvailablePosition.x = startX;
                        }
                        needsUpdate = true;
                    }

                    var cyNode = cy.add(cyNodeData);

                    if (needsUpdate) {
                        addedVertices.push({
                            graphVertexId: vertex.graphVertexId,
                            graphPosition: retina.pixelsToPoints(cyNode.position())
                        });
                    }

                    if (vertex._type === 'artifact' && /^(image|video)$/i.test(vertex._subType)) {
                        _.delay(function() {
                            previews.generatePreview(vertex._rowKey, { width:178 * retina.devicePixelRatio }, function(dataUri) {
                                if (dataUri) {
                                    cyNode.css('background-image', dataUri);
                                }
                            });
                        }, 500);
                    }
                });

                if (options.fit && cy.nodes().length) {
                    this.fit();
                }

                if (addedVertices.length) {
                    this.trigger(document, 'updateVertices', { vertices:addedVertices });
                }

                this.setWorkspaceDirty();
            });
        };

        this.classesForVertex = function(vertex) {
            var cls = [];

            if (vertex._subType) cls.push('concept-' + vertex._subType);
            if (vertex._type) cls.push(vertex._type);
            if (vertex._glyphIcon) cls.push('hasCustomGlyph');
            
            return cls.join(' ');
        };

        this.updateCyNodeData = function (data, vertex) {
            var originalTitle = vertex.title || data.title || 'No title available',
                title = originalTitle;

            if (title.length > MAX_TITLE_LENGTH) {
                title = $.trim(title.substring(0, MAX_TITLE_LENGTH)) + "...";
            }

            var merged = $.extend(data, _.pick(vertex, '_rowKey', '_subType', '_type', '_glyphIcon', 'graphVertexId')); 
            merged.id = merged.graphVertexId;
            merged.title = title;
            merged.originalTitle = originalTitle;

            return merged;
        };

        this.removeSelectedVertices = function() {
            this.cy(function(cy) {
                var verticesToDelete = $.map(cy.nodes().filter(':selected'), function(vertex) {
                    return {
                        graphVertexId: vertex.data('graphVertexId'),
                        _type: vertex.data('_type'),
                        _subType: vertex.data('_subType')
                    };
                });

                this.trigger(document, 'deleteVertices', { vertices: verticesToDelete });
            });
        };

        this.onVerticesDeleted = function(event, data) {
            this.cy(function(cy) {
                var matchingVertices = cy.nodes().filter(function(idx, vertex) {
                    return data.vertices.filter(function(vertexToDelete) {
                        return vertex.data('graphVertexId') == vertexToDelete.graphVertexId;
                    }).length > 0;
                });
                matchingVertices.remove();
                this.setWorkspaceDirty();

                this.updateVertexSelections(cy);
            });
        };

        this.onVerticesSelected = function(evt, data) {
            if (data && data.remoteEvent) {
                return;
            }
            if ($(evt.target).is('.graph-pane')) {
                return;
            }

            this.cy(function(cy) {
                this.ignoreCySelectionEvents = true;
                
                cy.$(':selected').unselect();
                if (data.length) {
                    cy.$( 
                        data.map(function(v) {
                            return '#' + v.graphVertexId;
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
                        cy.nodes()
                            .filter(function(idx, vertex) {
                                return vertex.data('graphVertexId') === updatedVertex.graphVertexId;
                            })
                            .each(function(idx, vertex) {
                                if (updatedVertex.graphPosition) {
                                    vertex.position( retina.pointsToPixels(updatedVertex.graphPosition) );
                                    updatedVertex = _.omit(updatedVertex, 'graphPosition', 'dropPosition');
                                }

                                self.updateCyNodeData(vertex.data(), updatedVertex);
                                vertex._private.classes.length = 0;
                                vertex.addClass(self.classesForVertex(updatedVertex));
                            });
                    });
            });

        };

        this.onExistingVerticesAdded = function(evt, data) {
            if (this.$node.closest('.visible').length === 0) return;
            var self = this;
            this.cy(function(cy) {

                // FIXME: support multiple dragging
                var el = cy.getElementById( data.vertices[0].graphVertexId ),
                    p = retina.pixelsToPoints(el.renderedPosition()),
                    dragging = $('.ui-draggable-dragging:not(.clone-vertex)'),
                    position = dragging.position(),
                    offset = dragging.offset(),
                    graphOffset = this.$node.offset();

                if (dragging.length != 1) return;

                var cloned = dragging.clone()
                    .css({width:'auto'})
                    .addClass('clone-vertex')
                    .insertAfter(dragging);

                // Is existing element visible (not covered by search/detail panes)
                this.focusGraphToVertex(el, function() {
                    var p = retina.pixelsToPoints(el.renderedPosition());

                    // Adjust rendered position to page coordinate system
                    p.x += graphOffset.left;
                    p.y += graphOffset.top;

                    // Move draggable coordinates from top/left to center
                    offset.left += cloned.outerWidth(true) / 2;
                    offset.top += cloned.outerHeight(true) / 2;

                    cloned
                        .animate({
                            left: (position.left + (p.x-offset.left))  + 'px',
                            top: (position.top +  (p.y-offset.top)) + 'px'
                        }, {
                            complete: function() {
                                cloned.addClass('shrink');
                                _.delay(function() { 
                                    cloned.remove(); 
                                }, 1000); 
                            }
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
            var menu = this.select('edgeContextMenuSelector');
            var edgeId = menu.data('edgeId');
            this.cy(function(cy) {
                this.ucd.deleteEdge(menu.data('sourceId'), menu.data('targetId'), menu.data('relationshipType'), function(err) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    cy.remove (cy.getElementById (edgeId));
                });
            });
        };

        this.onContextMenuRemoveItem = function (){
            var menu = this.select('vertexContextMenuSelector');
            var vertices = [];
            var data = {
                _subType: menu.data("currentVertexSubtype"),
                _type: menu.data("currentVertexType"),
                graphVertexId: menu.data('currentVertexGraphVertexId')
            };
            vertices.push (data);
            this.trigger (document,'deleteVertices', {vertices: vertices});
        };

        this.onContextMenuFitToWindow = function() {
            this.fit();
        };

        this.fit = function() {
            this.cy(function(cy) {
                if( cy.elements().size() === 0 ){
                    cy.reset();
                } else if (this.graphPadding) {
                    // Temporarily adjust max zoom 
                    // prevents extreme closeup when one vertex
                    var maxZoom = cy._private.maxZoom;
                    cy._private.maxZoom *= 0.5;
                    cy.fit(undefined, $.extend({}, this.graphPadding));
                    cy._private.maxZoom = maxZoom;
                }
            });
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
                                duration: 1500,
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

            padding.r += this.select('graphToolsSelector').outerWidth(true);
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
                                graphVertexId: vertex.data('graphVertexId'),
                                graphPosition: retina.pixelsToPoints(vertex.position())
                            };
                        });
                        self.trigger(document, 'updateVertices', { vertices:updates });
                    }
                }, LAYOUT_OPTIONS[layout] || {});

                cy.layout(opts);
            });
        };


        this.focusGraphToVertex = function(el, callback) {
            var position = retina.pixelsToPoints(el.renderedPosition()),
                padding = $.extend({}, this.graphPadding),
                extraHPadding = el.width() / 2 + 5,
                extraVPadding = el.height() / 2 + 5,
                width = this.$node.width(),
                height = this.$node.height(),
                panToView = { x:0, y:0 };

            padding.l += extraHPadding; padding.r += extraHPadding; 
            padding.t += extraVPadding; padding.b += extraVPadding;

            if (position.x < padding.l) {
                panToView.x = padding.l - position.x;
            } else if (position.x > width - padding.r) {
                panToView.x = (width - padding.r) - position.x;
            }

            if (position.y < padding.t) {
                panToView.y = padding.t - position.y;
            } else if (position.y > height - padding.b) {
                panToView.y = (height - padding.b) - position.y;
            }

            this.cy(function(cy) {
                cy.panBy(retina.pointsToPixels(panToView));
                callback();
            });
        };

        this.graphTap = throttle('selection', SELECTION_THROTTLE, function(event) {
            if (event.cyTarget === event.cy) {
                this.trigger('verticesSelected');
            }
        });

        this.graphContextTap = function(event) {
            var menu;
            // TODO: create different vertexContext menus for vertices/edges
            if (event.cyTarget == event.cy){
                menu = this.select ('contextMenuSelector');
                this.select('vertexContextMenuSelector').blur().parent().removeClass('open');
                this.select('edgeContextMenuSelector').blur().parent().removeClass('open');
            } else if (event.cyTarget.group ('edges') == 'edges') {
                menu = this.select ('edgeContextMenuSelector');
                menu.data("edgeId", event.cyTarget.data('id'));
                menu.data("sourceId",event.cyTarget.data('source'));
                menu.data("targetId",event.cyTarget.data('target'));
                menu.data("relationshipType", event.cyTarget.data('relationshipType'));
                if (event.cy.nodes().filter(':selected').length > 1) {
                    return false;
                }
                this.select('vertexContextMenuSelector').blur().parent().removeClass('open');
                this.select('contextMenuSelector').blur().parent().removeClass('open');
            } else {
                menu = this.select ('vertexContextMenuSelector');
                menu.data("currentVertexRowKey",event.cyTarget.data('_rowKey'));
                menu.data("currentVertexGraphVertexId", event.cyTarget.data('graphVertexId'));
                menu.data("currentVertexPositionX", event.cyTarget.position ('x'));
                menu.data("currentVertexPositionY", event.cyTarget.position ('y'));
                menu.data("currentVertexType", event.cyTarget.data('_type'));
                menu.data("currentVertexSubtype", event.cyTarget.data('_subType'));
                if (event.cy.nodes().filter(':selected').length > 1) {
                    return false;
                }
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
                self.trigger('verticesSelected');
            }
        });

        this.updateVertexSelections = function(cy) {
            var selection = cy.nodes().filter(':selected');
            var edgeSelection = cy.edges().filter(':selected');
            var info = [];

            selection.each(function(index, vertex) {
                info.push(vertex.data());
            });
            edgeSelection.each(function(index, vertex) {
                info.push(vertex.data());
            });

            this.trigger('verticesSelected', [info]);
        };

        this.onKeyHandler = function(event) {
            var down = event.type === 'keydown',
                up = !down,
                handled = true;

            switch (event.which) {

                case $.ui.keyCode.BACKSPACE:
                case $.ui.keyCode.DELETE:
                    if ( down ) {
                        this.removeSelectedVertices();
                    }
                    break;
                case 65:
                    if (down && (event.metaKey || event.ctrlKey)) {
                        this.cy(function(cy) {
                            cy.nodes().select();
                        });
                    } else {
                        handled = false;
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
                var vertices = event.cyTarget.selected() ? cy.nodes().filter(':selected') : event.cyTarget;
                this.grabbedVertices = vertices.each(function() {
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
                        _rowKey: vertex.data('_rowKey'),
                        graphVertexId: vertex.data('id'),
                        graphPosition: vertex.data('targetPosition')
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

                if (noVertices) {
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
            if (workspace.data && workspace.data.vertices) {
                workspace.data.vertices.forEach(function(vertex){
                    vertex.selected = false;
                });
                this.addVertices(workspace.data.vertices, { fit:true });
            }

            this.checkEmptyGraph();
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
                                id: (relationship.from + '>' + relationship.to + '|' + relationship.relationshipType)
                            },
                        });
                    });
                    cy.add(relationshipEdges);
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

            var xOffset = 100, yOffset = 100;
            var x = data.originalPosition.x;
            var y = data.originalPosition.y;

            this.ucd.getRelatedVertices(data, function(err, vertices) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                vertices = vertices.vertices;

                vertices = vertices.map(function(vertex, index) {
                    if (index % 10 === 0) {
                        y += yOffset;
                    }
                    return $.extend({}, vertex.properties, {
                        graphVertexId: vertex.id,
                        graphPosition: {
                            x: x + xOffset * (index % 10 + 1),
                            y: y
                        },
                        selected: true
                    });
                });

                self.trigger(document, 'addVertices', { vertices: vertices });
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
            this.on(document, 'verticesAdded', this.onVerticesAdded);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'verticesSelected', this.onVerticesSelected);
            this.on(document, 'existingVerticesAdded', this.onExistingVerticesAdded);
            this.on(document, 'relationshipsLoaded', this.onRelationshipsLoaded);
            this.on(document, 'graphPaddingUpdated', this.onGraphPaddingUpdated);
            this.on(document, 'menubarToggleDisplay', this.onMenubarToggleDisplay);
            this.on(document, 'focusVertices', this.onFocusVertices);
            this.on(document, 'defocusVertices', this.onDefocusVertices);

            if (self.attr.vertices && self.attr.vertices.length) {
                this.select('emptyGraphSelector').hide();
                this.addVertices(self.attr.vertices);
            }

            this.ontologyService.concepts(function(err, concepts) {
                if (err) {
                    return self.trigger(document, 'error', err);
                }

                var templateData = {
                    firstLevelConcepts: concepts.entityConcept.children,
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
                hideEdgesOnViewport: true,
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
                                    this.focus();
                                    $(".instructions").remove();
                                },
                        keydown: self.onKeyHandler.bind(self),
                        keyup: self.onKeyHandler.bind(self)
                    });

                    // Override "Fit to Window" button and call our own
                    $('.ui-cytoscape-panzoom-reset').on('mousedown', function(e) {
						if (e.button !== 0) return;
                        e.stopPropagation();
                        self.fit();
                    });

                    self.panZoom = self.select('graphToolsSelector');
                    self.updatePanZoomLocation();
                    
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
        };
    }

});

