
define([
    'tpl!app',
    'flight/lib/component',
    'menubar/menubar',
    'search/search',
    'workspaces/workspaces',
    'workspaces/overlay',
    'sync/sync',
    'users/users',
    'graph/graph',
    'detail/detail',
    'map/map',
    'service/workspace',
    'service/ucd',
    'util/keyboard',
    'util/undoManager',
    'underscore'
], function(appTemplate, defineComponent, Menubar, Search, Workspaces, WorkspaceOverlay, Sync, Users, Graph, Detail, Map, WorkspaceService, UcdService, Keyboard, undoManager, _) {
    'use strict';

    return defineComponent(App);

    function App() {
        var WORKSPACE_SAVE_TIMEOUT = 1000;
        var MAX_RESIZE_TRIGGER_INTERVAL = 250;
        var DATA_MENUBAR_NAME = 'menubar-name';

        this.workspaceService = new WorkspaceService();
        this.ucdService = new UcdService();

        this.onError = function(evt, err) {
            alert("Error: " + err.message); // TODO better error handling
        };

        this.defaultAttrs({
            menubarSelector: '.menubar-pane',
            searchSelector: '.search-pane',
            workspacesSelector: '.workspaces-pane',
            workspaceOverlaySelector: '.workspace-overlay',
            usersSelector: '.users-pane',
            graphSelector: '.graph-pane',
            mapSelector: '.map-pane',
            detailPaneSelector: '.detail-pane',
            droppableSelector: '.graph-pane, .map-pane'
        });

        var wsStack = [];
        this.workspace = function(callback) {
            if (this.workspaceData) {
                callback.call(this, this.workspaceData);
            } else {
                wsStack.push(callback);
            }
        };

        this.drainWorkspaceQueue = function() {
            var self = this;
            wsStack.forEach(function(c) {
                c.call(self, self.workspaceData);
            });
        };

        this.after('initialize', function() {
            window.lumifyApp = this;


            this.on(document, 'error', this.onError);
            this.on(document, 'menubarToggleDisplay', this.toggleDisplay);
            this.on(document, 'chatMessage', this.onChatMessage);
            this.on(document, 'verticesSelected', this.onVerticesSelected);
            this.on(document, 'syncStarted', this.onSyncStarted);
            this.on(document, 'paneResized', this.onInternalPaneResize);
            this.on(document, 'toggleGraphDimensions', this.onToggleGraphDimensions);

            // Prevent the fragment identifier from changing after an anchor
            // with href="#" not stopPropagation'ed
            $(document).on('click', 'a', this.trapAnchorClicks.bind(this));

            var content = $(appTemplate({})),
                menubarPane = content.filter('.menubar-pane'),
                searchPane = content.filter('.search-pane').data(DATA_MENUBAR_NAME, 'search'),
                workspacesPane = content.filter('.workspaces-pane').data(DATA_MENUBAR_NAME, 'workspaces'),
                usersPane = content.filter('.users-pane').data(DATA_MENUBAR_NAME, 'users'),
                graphPane = content.filter('.graph-pane').data(DATA_MENUBAR_NAME, 'graph'),
                mapPane = content.filter('.map-pane').data(DATA_MENUBAR_NAME, 'map'),
                detailPane = content.filter('.detail-pane');

//            Sync.attachTo(window);
            Menubar.attachTo(menubarPane.find('.content'));
            Search.attachTo(searchPane.find('.content'));
            Workspaces.attachTo(workspacesPane.find('.content'));
            Users.attachTo(usersPane.find('.content'));
            Graph.attachTo(graphPane);
            Detail.attachTo(detailPane.find('.content'));
            Map.attachTo(mapPane);
            Keyboard.attachTo(document);
            WorkspaceOverlay.attachTo(content.filter('.workspace-overlay'));

            // Configure splitpane resizing
            resizable(searchPane, 'e', 160, 200, this.onPaneResize.bind(this));
            resizable(workspacesPane, 'e', undefined, 200, this.onPaneResize.bind(this));
            resizable(detailPane, 'w', 4, 500, this.onPaneResize.bind(this));

            this.$node.html(content);

            this.setupDroppable();

            // Open search when the page is loaded
            this.trigger(document, 'menubarToggleDisplay', { name: graphPane.data(DATA_MENUBAR_NAME) });

            this.on(document, 'addVertices', this.onAddVertices);
            this.on(document, 'updateVertices', this.onUpdateVertices);
            this.on(document, 'deleteVertices', this.onDeleteVertices);

            this.on(document, 'refreshRelationships', this.refreshRelationships);

            this.on(document, 'switchWorkspace', this.onSwitchWorkspace);

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'workspaceSave', this.onSaveWorkspace);
            this.on(document, 'workspaceDeleted', this.onWorkspaceDeleted);
            this.on(document, 'workspaceDeleting', this.onWorkspaceDeleting);

            this.on(document, 'mapCenter', this.onMapAction);

            this.on(document, 'changeView', this.onChangeView);

            this.loadActiveWorkspace();
            this.setupWindowResizeTrigger();
            this.triggerPaneResized();
        });

        this.trapAnchorClicks = function(e) {
            var $target = $(e.target);

            if ($target.is('a') && $target.attr('href') === '#') {
                e.preventDefault();
            }
        };

        var resizeTimeout;
        this.setupWindowResizeTrigger = function() {
            var self = this;
            this.on(window, 'resize', function() {
                clearTimeout(resizeTimeout);
                resizeTimeout = setTimeout(function() {
                    self.trigger(document, 'windowResize');
                }, MAX_RESIZE_TRIGGER_INTERVAL);
            });
        };

        this.onMapAction = function(event, data) {
            this.trigger(document, 'changeView', { view: 'map' });
        };

        this.onChangeView = function(event, data) {
            var view = data && data.view;

            var pane = view && this.select(view + 'Selector');
            if (pane && pane.hasClass('visible')) {
                return;
            } else if (pane) {
                this.trigger(document, 'menubarToggleDisplay', { name: pane.data(DATA_MENUBAR_NAME) });
            } else {
                console.log("View " + data.view + " isn't supported");
            }
        };

        this.setupDroppable = function() {
            var self = this;

            var enabled = false,
                droppable = this.select('droppableSelector');

            // Other droppables might be on top of graph, listen to 
            // their over/out events and ignore drops if the user hasn't
            // dragged outside of them. Can't use greedy option since they are
            // absolutely positioned
            $(document).on('dropover dropout', function(e, ui) {
                var target = $(e.target),
                    appDroppable = target.is(droppable),
                    noParentDroppables = target.parents('.ui-droppable').length === 0;

                if (appDroppable) {
                    // Ignore events from this droppable
                    return;
                }

                // If this droppable has no parent droppables
                if (noParentDroppables) {
                    enabled = e.type === 'dropout';
                }
            });

            droppable.droppable({
                accept: function(item) {
                    return true;
                },
                drop: function( event, ui ) {
                    // Early exit if should leave to a different droppable
                    if (!enabled) return;
                    var draggable = ui.draggable,
                        droppable = $(event.target);

                    var info = draggable.data('info') || draggable.parents('li').data('info');
                    if ( !info ) {
                        console.warn('No data-info attribute for draggable element found');
                        return;
                    }

                    var dropPosition = $(event.target).is('.graph-pane') ?
                        {
                            x: event.clientX,
                            y: event.clientY
                        } : {
                            x: parseInt(Math.random() * droppable.width(), 10),
                            y: parseInt(Math.random() * droppable.height(), 10)
                        };

                    var vertices = [$.extend({
                        dropPosition: dropPosition
                    }, info)];

                    if(info.resolvedGraphVertexId) {
                        this.ucdService.getGraphVertexById(info.resolvedGraphVertexId, function(err, data) {
                            if(err) {
                                console.error('Error', err);
                                return self.trigger(document, 'error', { message: err.toString() });
                            }

                            vertices.push({
                                graphVertexId: data.id,
                                title: data.properties.title || 'No title available',
                                _type: data.properties._type,
                                _subType: data.properties._subType,
                                dropPosition: {
                                    x: dropPosition.x + droppable.width()/10,
                                    y: dropPosition.y
                                }
                            });

                            self.trigger(document, 'addVertices', {
                                vertices: vertices
                            });
                        });
                    } else {
                        self.trigger(document, 'addVertices', {
                            vertices: vertices
                        });
                    }
                }.bind(this)
            });
        };

        this.loadActiveWorkspace = function() {
            var self = this;
            self.workspaceService.list(function(err, workspaces) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                if(workspaces.length === 0) {
                    self.loadWorkspace(null);
                } else {
                    for (var i = 0; i < workspaces.length; i++) {
                        if (workspaces[i].active) {
                            self.loadWorkspace(workspaces[i]._rowKey);
                            return;
                        }
                    }
                    // backwards compatibility when no current workspace
                    self.loadWorkspace(workspaces[0]._rowKey);
                }
            });
        };

        this.loadWorkspace = function(workspaceRowKey) {
            var self = this;
            self.workspaceRowKey = workspaceRowKey;
            if(self.workspaceRowKey == null) {
                self.trigger(document, 'workspaceLoaded', { data: { vertices: [] } });
                return;
            }

            self.workspaceService.getByRowKey(self.workspaceRowKey, function(err, workspace) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                self.trigger(document, 'workspaceLoaded', workspace);
            });
        };

        this.onSwitchWorkspace = function(evt, data) {
            if (data._rowKey != this.workspaceRowKey) {
                this.loadWorkspace(data._rowKey);
            }
        };

        this.onSaveWorkspace = function(evt, workspace) {
            var self = this;
            var saveFn;
            if(self.workspaceRowKey) {
                saveFn = self.workspaceService.save.bind(self.workspaceService, self.workspaceRowKey);
            } else {
                saveFn = self.workspaceService.saveNew.bind(self.workspaceService);
            }

            self.trigger(document, 'workspaceSaving', workspace);
            saveFn({ data: workspace }, function(err, data) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
				self.workspaceRowKey = data._rowKey;
                self.trigger(document, 'workspaceSaved', data);
            });
        };

        this.onWorkspaceLoaded = function(evt, data) {
            this.workspaceData = data || {};
            this.workspaceData.data = this.workspaceData.data || {};
            this.workspaceData.data.vertices = this.workspaceData.data.vertices || [];

            if (this.workspaceData.data.vertices.length === 0) {
                this.trigger(document, 'menubarToggleDisplay', { name:'search' });
            }

            undoManager.reset();
            this.refreshRelationships();
            this.drainWorkspaceQueue();
        };

        this.onWorkspaceDeleting = function (evt, data) {
            if (this.workspaceRowKey == data._rowKey) {
                var instructions = $('<div>')
                                .text("Deleting current workspace...")
                                .addClass('instructions')
                                .appendTo(this.$node);
            }
        }

        this.onWorkspaceDeleted = function(evt, data) {
            if (this.workspaceRowKey == data._rowKey) {
                $(".instructions").remove();
                this.workspaceRowKey = null;
                this.loadActiveWorkspace();
            }
        };

        this.setWorkspaceDirty = function() {
            if(this.saveWorkspaceTimeout) {
                clearTimeout(this.saveWorkspaceTimeout);
            }
            this.saveWorkspaceTimeout = setTimeout(this.saveWorkspace.bind(this), WORKSPACE_SAVE_TIMEOUT);
        };

        this.saveWorkspace = function() {
            this.trigger(document, 'workspaceSave', this.workspaceData.data);
        };

        this.onUpdateVertices = function(evt, data) {

            this.workspace(function(ws) {
                var undoData = {
                    noUndo: true,
                    vertices: []
                };
                var redoData = {
                    noUndo: true,
                    vertices: []
                };
                data.vertices.forEach(function(vertex) {
                    var matchingWorkspaceVertices = ws.data.vertices.filter(function(workspaceVertex) {
                        return workspaceVertex.graphVertexId == vertex.graphVertexId;
                    });

                    matchingWorkspaceVertices.forEach(function(workspaceVertex) {
                        undoData.vertices.push(JSON.parse(JSON.stringify(workspaceVertex)));
                        $.extend(workspaceVertex, vertex);
                        redoData.vertices.push(JSON.parse(JSON.stringify(workspaceVertex)));
                    });
                });

                if(!data.noUndo) {
                    undoManager.performedAction( 'Update ' + undoData.vertices.length + ' vertices', {
                        undo: function() {
                            this.trigger(document, 'updateVertices', undoData);
                        },
                        redo: function() {
                            this.trigger(document, 'updateVertices', redoData);
                        },
                        bind: this
                    });
                }

                this.setWorkspaceDirty();

                this.trigger(document, 'verticesUpdated', data);
            });
        };

        this.onAddVertices = function(evt, data) {
            this.workspace(function(ws) {
                var allVertices = this.workspaceData.data.vertices,
                    added = [],
                    existing = [],
                    win = $(window);

                // FIXME: How should we store vertices in the workspace?
                // currently mapping { id:[graphVertexId], properties:{} }
                // to { graphVertexId:..., [properties] }
                data.vertices = data.vertices.map(function(n) {
                    var vertex = n;
                    if (n.properties) {
                        vertex = n.properties;
                        vertex.graphVertexId = n.id;
                    }
                    // Legacy names
                    vertex._rowKey = encodeURIComponent((vertex._rowKey || vertex.rowKey || vertex.rowkey || '').replace(/\\[x](1f)/ig, '\u001f'));

                    if ( !vertex.dropPosition && !vertex.graphPosition) {
                        vertex.dropPosition = {
                            x: parseInt(Math.random() * win.width(), 10),
                            y: parseInt(Math.random() * win.height(), 10)
                        };
                    }
                    return vertex;
                });

                // Check if already in workspace
                data.vertices.forEach(function(vertex) {
                    if (ws.data.vertices.filter(function(n) { return n.graphVertexId === vertex.graphVertexId; }).length === 0) {
                        added.push(vertex);
                        ws.data.vertices.push(vertex);
                    } else {
                        existing.push(vertex);
                    }
                });

                if (existing.length) this.trigger(document, 'existingVerticesAdded', { vertices:existing });
                if (added.length === 0) {
                    $(".graph-pane .instructions").text("No New Vertices Added");
                    return;
                }

                if(!data.noUndo) {
                    var dataClone = JSON.parse(JSON.stringify(data));
                    dataClone.noUndo = true;
                    undoManager.performedAction( 'Add ' + dataClone.vertices.length + ' vertices', {
                        undo: function() {
                            this.trigger(document, 'deleteVertices', dataClone);
                        },
                        redo: function() {
                            this.trigger(document, 'addVertices', dataClone);
                        },
                        bind: this
                    });
                }

                this.setWorkspaceDirty();

                this.refreshRelationships();

                this.trigger(document, 'verticesAdded', { vertices:added } );
            });
        };

        this.onDeleteVertices = function(evt, data) {
            var self = this;

            this.workspace(function(ws) {

                // get all the workspace vertices to delete (used by the undo manager)
                var workspaceVerticesToDelete = ws.data.vertices
                    .filter(function(workspaceVertex) {
                        return data.vertices.filter(function(dataVertex) {
                            return workspaceVertex.graphVertexId == dataVertex.graphVertexId;
                        }).length > 0;
                    });

                // remove all workspace vertices from list
                ws.data.vertices = ws.data.vertices
                    .filter(function(workspaceVertex) {
                        return workspaceVerticesToDelete.filter(function(workspaceVerticesToDelete) {
                            return workspaceVertex._rowKey == workspaceVerticesToDelete._rowKey;
                        }).length === 0;
                    });

                if(!data.noUndo) {
                    var undoDataClone = JSON.parse(JSON.stringify({
                        noUndo: true,
                        vertices: workspaceVerticesToDelete
                    }));
                    undoManager.performedAction( 'Delete ' + data.vertices.length + ' vertices', {
                        undo: function() {
                            self.trigger(document, 'addVertices', undoDataClone);
                        },
                        redo: function() {
                            self.trigger(document, 'deleteVertices', undoDataClone);
                        },
                        bind: this
                    });
                }

                this.setWorkspaceDirty();

                this.trigger(document, 'verticesDeleted', data);
            });
        };

        this.refreshRelationships = function() {
            var self = this;
            var ids = this.getIds();

            this.ucdService.getRelationships(ids, function(err, relationships) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                self.trigger(document, 'relationshipsLoaded', { relationships: relationships });
            });
        };

        this.getIds = function () {
           if (this.workspaceData.data === undefined || this.workspaceData.data.vertices === undefined) {
               return [];
           }
           return this.workspaceData.data.vertices
               .map(function(vertex) {
                   return vertex.graphVertexId;
               });
        };

        this.getEntityIds = function() {
            if (this.workspaceData.data === undefined || this.workspaceData.data.vertices === undefined) {
                return [];
            }
            return this.workspaceData.data.vertices
                .map(function(vertex) {
                    return vertex.graphVertexId;
                });
        };

        this.getArtifactIds = function() {
            if (this.workspaceData.data === undefined || this.workspaceData.data.vertices === undefined) {
                return [];
            }
            return this.workspaceData.data.vertices
                .filter(function(vertex) {
                    return vertex.type == 'artifact';
                })
                .map(function(vertex) {
                    return vertex.graphVertexId;
                });
        };

        this.onToggleGraphDimensions = function(e) {
            var self = this,
                node = this.$node.find('.graph-pane');

            require(['graph/3d/graph'], function(Graph3D) {
                if (!self._graphDimensions || self._graphDimensions === 2) {
                    Graph.teardownAll();
                    Graph3D.attachTo(node, {
                        vertices: self.workspaceData.data.vertices
                    });
                    self._graphDimensions = 3;
                } else {
                    Graph3D.teardownAll();
                    Graph.attachTo(node, {
                        vertices: self.workspaceData.data.vertices
                    });
                    self._graphDimensions = 2;
                    self.triggerPaneResized();
                }

                self.refreshRelationships();
            });
        };

        this.toggleDisplay = function(e, data) {
            var SLIDE_OUT = 'search workspaces';
            var pane = this.select(data.name + 'Selector');

            if (data.name === 'graph' && !pane.hasClass('visible')) {
                this.trigger(document, 'mapHide');
                this.trigger(document, 'graphShow');
            } else if (data.name === 'map' && !pane.hasClass('visible')) {
                this.trigger(document, 'graphHide');
                this.trigger(document, 'mapShow');
                this.collapse([
                    this.select('searchSelector'),
                    this.select('workspacesSelector'),
                    this.select('detailPaneSelector')
                ]);
            }

            if (SLIDE_OUT.indexOf(data.name) >= 0) {
                var self = this, 
                    visible = pane.hasClass('visible');

                pane.one('transitionend webkitTransitionEnd oTransitionEnd otransitionend', function() {
                    pane.off('transitionend webkitTransitionEnd oTransitionEnd otransitionend');
                    self.triggerPaneResized();
                });
            }

            pane.toggleClass('visible');
        };

        this.onChatMessage = function(e, data) {
            if (!this.select('usersSelector').hasClass('visible')) {
                this.trigger(document, 'menubarToggleDisplay', { name: this.select('usersSelector').data(DATA_MENUBAR_NAME) });
            }
        };

        this.onVerticesSelected = function(e, data) {
            var detailPane = this.select('detailPaneSelector');
            var minWidth = 100;
            var width = 0;

            if (data && data.length !== 0) {
                if (detailPane.width() < minWidth) {
                    detailPane[0].style.width = null;
                }
                detailPane.removeClass('collapsed').addClass('visible');
                width = detailPane.width();
            } else {
                detailPane.removeClass('visible').addClass('collapsed');
            }

            this.triggerPaneResized();
        };

        this.onInternalPaneResize = function() {
            this.triggerPaneResized();
        };

        this.onPaneResize = function(e, ui) {
            var COLLAPSE_TOLERANCE = 50,
                width = ui.size.width,
                shouldCollapse = width < COLLAPSE_TOLERANCE;

            $(e.target).toggleClass('collapsed', shouldCollapse);
            $(e.target).toggleClass('visible', !shouldCollapse);

            this.triggerPaneResized();
        };

        this.triggerPaneResized = function() {
            var PANE_BORDER_WIDTH = 1,
                searchWidth = this.select('searchSelector')
                    .filter('.visible:not(.collapsed)')
                    .outerWidth(true) || 0,

                searchResultsWidth = searchWidth > 0 ? 
                    $('.search-results:visible:not(.collapsed)')
                        .outerWidth(true) || 0 : 0,

                searchFiltersWidth = searchWidth > 0 ? 
                    $('.search-filters:visible:not(.collapsed)')
                        .outerWidth(true) || 0 : 0,

                workspacesWidth = this.select('workspacesSelector')
                    .filter('.visible:not(.collapsed)')
                    .outerWidth(true) || 0,

                detailWidth = this.select('detailPaneSelector')
                    .filter('.visible:not(.collapsed)')
                    .outerWidth(true) || 0,

                padding = {
                    l:searchWidth + searchResultsWidth + searchFiltersWidth + workspacesWidth, 
                    r:detailWidth,
                    t:0, 
                    b:0
                };

            if (padding.l) {
                padding.l += PANE_BORDER_WIDTH;
            }
            if (padding.r) {
                padding.r += PANE_BORDER_WIDTH;
            }

            this.trigger(document, 'graphPaddingUpdated', { padding: padding });
        };


        this.onSyncStarted = function() {
            this.collapse([
                this.select('searchSelector'),
                this.select('workspacesSelector'),
                this.select('detailPaneSelector')
            ]);
            // TODO: fix this smellyness
            $('.search-results').hide();

            var graph = this.select('graphSelector');
            if ( ! graph.hasClass('visible') ) {
                self.trigger(document, 'menubarToggleDisplay', { name:graph.data(DATA_MENUBAR_NAME), syncToRemote:false });
            }
        };

        this.collapse = function(panes) {
            var self = this,
                detailPane = this.select('detailPaneSelector');

            panes.forEach(function(pane) {
                if (pane.hasClass('visible')) {
                    var name = pane.data(DATA_MENUBAR_NAME),
                        isDetail = pane.is(detailPane);

                    if ( !name ) {
                        if ( isDetail ) {
                            return detailPane.addClass('collapsed').removeClass('visible');
                        }
                        return console.warn('No ' + DATA_MENUBAR_NAME + ' attribute, unable to collapse');
                    }

                    self.trigger(document, 'menubarToggleDisplay', { name:name, syncToRemote:false });
                }
            });
        };
    }


    function resizable( el, handles, minWidth, maxWidth, callback ) {
        return el.resizable({
            handles: handles,
            minWidth: minWidth || 150,
            maxWidth: maxWidth || 300,
            resize: callback 
        });
    }

});

