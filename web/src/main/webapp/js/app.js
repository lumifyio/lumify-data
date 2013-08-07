
define([
    'tpl!app',
    'flight/lib/component',
    'menubar/menubar',
    'search/search',
    'workspaces/workspaces',
    'users/users',
    'graph/graph',
    'detail/detail',
    'map/map',
    'service/workspace',
    'service/ucd',
    'util/keyboard',
    'util/undoManager'
], function(appTemplate, defineComponent, Menubar, Search, Workspaces, Users, Graph, Detail, Map, WorkspaceService, UcdService, Keyboard, undoManager) {
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
            window.reddawnApp = this;


            this.on(document, 'error', this.onError);
            this.on(document, 'menubarToggleDisplay', this.toggleDisplay);
            this.on(document, 'message', this.onMessage);
            this.on(document, 'searchResultSelected', this.onSearchResultSelection);
            this.on(document, 'syncStarted', this.onSyncStarted);
            this.on(document, 'requestGraphPadding', this.onRequestGraphPadding);

            var content = $(appTemplate({})),
                menubarPane = content.filter('.menubar-pane'),
                searchPane = content.filter('.search-pane').data(DATA_MENUBAR_NAME, 'search'),
                workspacesPane = content.filter('.workspaces-pane').data(DATA_MENUBAR_NAME, 'workspaces'),
                usersPane = content.filter('.users-pane').data(DATA_MENUBAR_NAME, 'users'),
                graphPane = content.filter('.graph-pane').data(DATA_MENUBAR_NAME, 'graph'),
                mapPane = content.filter('.map-pane').data(DATA_MENUBAR_NAME, 'map'),
                detailPane = content.filter('.detail-pane');

            Menubar.attachTo(menubarPane.find('.content'));
            Search.attachTo(searchPane.find('.content'));
            Workspaces.attachTo(workspacesPane.find('.content'));
            Users.attachTo(usersPane.find('.content'));
            Graph.attachTo(graphPane);
            Detail.attachTo(detailPane.find('.content'));
            Map.attachTo(mapPane);
            Keyboard.attachTo(document);

            // Configure splitpane resizing
            resizable(searchPane, 'e');
            resizable(workspacesPane, 'e');
            resizable(detailPane, 'w', 4, 500, this.onDetailResize.bind(this));

            this.$node.html(content);

            this.setupDroppable();

            // Open search when the page is loaded
            this.trigger(document, 'menubarToggleDisplay', { name: searchPane.data(DATA_MENUBAR_NAME) });
            this.trigger(document, 'menubarToggleDisplay', { name: graphPane.data(DATA_MENUBAR_NAME) });

            this.on(document, 'addNodes', this.onAddNodes);
            this.on(document, 'updateNodes', this.onUpdateNodes);
            this.on(document, 'deleteNodes', this.onDeleteNodes);

            this.on(document, 'refreshRelationships', this.refreshRelationships);

            this.on(document, 'switchWorkspace', this.onSwitchWorkspace);

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'workspaceSave', this.onSaveWorkspace);
            this.on(document, 'workspaceDeleted', this.onWorkspaceDeleted);

            this.on(document, 'mapCenter', this.onMapAction);

            this.on(document, 'changeView', this.onChangeView);

            this.loadActiveWorkspace();
            this.setupWindowResizeTrigger();
        });

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

        this.onRequestGraphPadding = function() {
            var searchWidth = this.select('searchSelector').filter('.visible').outerWidth(true) || 0,
                searchResultsWidth = searchWidth > 0 ? $('.search-results:visible').outerWidth(true) || 0 : 0,
                detailWidth = this.select('detailPaneSelector').filter('.visible').outerWidth(true) || 0,
                padding = {
                    l:searchWidth + searchResultsWidth, r:detailWidth,
                    t:0, b:0
                };

            this.trigger(document, 'graphPaddingResponse', { padding: padding });
        };

        this.setupDroppable = function() {

            var enabled = false,
                droppable = this.select('droppableSelector');

            // Other droppables might be on top of graph, listen to 
            // their over/out events and ignore drops if the user hasn't
            // dragged outside of them. Can't use greedy option since they are
            // absolutely positioned
            $(document).on('dropover dropout', function(e, ui) {
                var target = $(e.target),
                    notAppDroppable = !target.is(droppable),
                    noParentDroppables = target.parents('.ui-droppable').length === 0;

                if (notAppDroppable && noParentDroppables) {
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

                    if(info.rowKey == undefined) {
                        info.rowKey = info.rowkey;
                    }

                    this.trigger(document, 'addNodes', {
                        nodes: [{
                            title: info.title || draggable.text(),
                            graphNodeId: info.graphNodeId,
                            rowKey: (info.rowKey || '').replace(/\\[x](1f)/ig, '\u001f'),
                            subType: info.subType,
                            type: info.type,
                            dropPosition: dropPosition
                        }]
                    });
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
                            self.loadWorkspace(workspaces[i].rowKey);
                            return;
                        }
                    }
                    self.loadWorkspace(workspaces[0].rowKey); // backwards compatibility when no current workspace
                }
            });
        };

        this.loadWorkspace = function(workspaceRowKey) {
            var self = this;
            self.workspaceRowKey = workspaceRowKey;
            if(self.workspaceRowKey == null) {
                self.trigger(document, 'workspaceLoaded', { data: { nodes: [] } });
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
            this.loadWorkspace(data.rowKey);
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
				self.workspaceRowKey = data.workspaceId;
                self.trigger(document, 'workspaceSaved', data);
            });
        };

        this.onWorkspaceLoaded = function(evt, data) {
            this.workspaceData = data || {};
            this.workspaceData.data = this.workspaceData.data || {};
            this.workspaceData.data.nodes = this.workspaceData.data.nodes || [];

            undoManager.reset();
            this.refreshRelationships();
            this.drainWorkspaceQueue();
        };

        this.onWorkspaceDeleted = function(evt, data) {
            if (this.workspaceRowKey == data.rowKey) {
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

        this.onUpdateNodes = function(evt, data) {

            this.workspace(function(ws) {
                var undoData = {
                    noUndo: true,
                    nodes: []
                };
                var redoData = {
                    noUndo: true,
                    nodes: []
                };
                data.nodes.forEach(function(node) {
                    var matchingWorkspaceNodes = ws.data.nodes.filter(function(workspaceNode) {
                        return workspaceNode.graphNodeId == node.graphNodeId;
                    });

                    matchingWorkspaceNodes.forEach(function(workspaceNode) {
                        undoData.nodes.push(JSON.parse(JSON.stringify(workspaceNode)));
                        $.extend(workspaceNode, node);
                        redoData.nodes.push(JSON.parse(JSON.stringify(workspaceNode)));
                    });
                });

                if(!data.noUndo) {
                    undoManager.performedAction( 'Update ' + undoData.nodes.length + ' nodes', {
                        undo: function() {
                            this.trigger(document, 'updateNodes', undoData);
                        },
                        redo: function() {
                            this.trigger(document, 'updateNodes', redoData);
                        },
                        bind: this
                    });
                }

                this.setWorkspaceDirty();

                this.trigger(document, 'nodesUpdated', data);
            });
        };

        this.onAddNodes = function(evt, data) {
            this.workspace(function(ws) {
                var allNodes = this.workspaceData.data.nodes,
                    added = [],
                    win = $(window);

                // FIXME: How should we store nodes in the workspace? 
                // currently mapping { id:[graphNodeId], properties:{} } 
                // to { graphNodeId:..., [properties] }
                data.nodes = data.nodes.map(function(n) {
                    var node = n;
                    if (n.properties) {
                        node = n.properties;
                        node.graphNodeId = n.id;
                    }
                    if ( !node.dropPosition && !node.graphPosition) {
                        node.dropPosition = {
                            x: parseInt(Math.random() * win.width(), 10),
                            y: parseInt(Math.random() * win.height(), 10)
                        };
                    }
                    return node;
                });

                // Check if already in workspace
                data.nodes.forEach(function(node) {
                    if (ws.data.nodes.filter(function(n) { return n.graphNodeId === node.graphNodeId; }).length === 0) {
                        added.push(node);
                        ws.data.nodes.push(node);
                    }
                });

                if (added.length === 0) return;

                if(!data.noUndo) {
                    var dataClone = JSON.parse(JSON.stringify(data));
                    dataClone.noUndo = true;
                    undoManager.performedAction( 'Add ' + dataClone.nodes.length + ' nodes', {
                        undo: function() {
                            this.trigger(document, 'deleteNodes', dataClone);
                        },
                        redo: function() {
                            this.trigger(document, 'addNodes', dataClone);
                        },
                        bind: this
                    });
                }

                this.setWorkspaceDirty();

                this.refreshRelationships ();

                this.trigger(document, 'nodesAdded', { nodes:added } );
            });
        };

        this.onDeleteNodes = function(evt, data) {
            var self = this;

            this.workspace(function(ws) {

                // get all the workspace nodes to delete (used by the undo manager)
                var workspaceNodesToDelete = ws.data.nodes
                    .filter(function(workspaceNode) {
                        return data.nodes.filter(function(dataNode) {
                            return workspaceNode.rowKey == dataNode.rowKey;
                        }).length > 0;
                    });

                // remove all workspace nodes from list
                ws.data.nodes = ws.data.nodes
                    .filter(function(workspaceNode) {
                        return workspaceNodesToDelete.filter(function(workspaceNodeToDelete) {
                            return workspaceNode.rowKey == workspaceNodeToDelete.rowKey;
                        }).length === 0;
                    });

                if(!data.noUndo) {
                    var undoDataClone = JSON.parse(JSON.stringify({
                        noUndo: true,
                        nodes: workspaceNodesToDelete
                    }));
                    undoManager.performedAction( 'Delete ' + data.nodes.length + ' nodes', {
                        undo: function() {
                            self.trigger(document, 'addNodes', undoDataClone);
                        },
                        redo: function() {
                            self.trigger(document, 'deleteNodes', undoDataClone);
                        },
                        bind: this
                    });
                }

                this.setWorkspaceDirty();

                this.trigger(document, 'nodesDeleted', data);
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
           if (this.workspaceData.data === undefined || this.workspaceData.data.nodes === undefined) {
               return [];
           }
           return this.workspaceData.data.nodes
               .map(function(node) {
                   return node.graphNodeId;
               });
        };

        this.getEntityIds = function() {
            if (this.workspaceData.data === undefined || this.workspaceData.data.nodes === undefined) {
                return [];
            }
            return this.workspaceData.data.nodes
                .map(function(node) {
                    return node.graphNodeId;
                });
        };

        this.getArtifactIds = function() {
            if (this.workspaceData.data === undefined || this.workspaceData.data.nodes === undefined) {
                return [];
            }
            return this.workspaceData.data.nodes
                .filter(function(node) {
                    return node.type == 'artifact';
                })
                .map(function(node) {
                    return node.graphNodeId;
                });
        };

        this.toggleDisplay = function(e, data) {
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

            if (data.name == 'search' && !pane.hasClass('visible')) {
                var self = this;
                pane.one('transitionend webkitTransitionEnd oTransitionEnd otransitionend', function() {
                    self.trigger(document, 'focusSearchField');
                });
            }

            pane.toggleClass('visible');
        };

        this.onMessage = function(e, data) {
            if (!this.select('usersSelector').hasClass('visible')) {
                this.trigger(document, 'menubarToggleDisplay', { name: this.select('usersSelector').data(DATA_MENUBAR_NAME) });
            }
        };

        this.onSearchResultSelection = function(e, data) {
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

            this.trigger(document, 'detailPaneResize', { width: width });
        };

        this.onDetailResize = function(e, ui) {
            var COLLAPSE_TOLERANCE = 50,
                width = ui.size.width,
                shouldCollapse = width < COLLAPSE_TOLERANCE;

            this.trigger(document, 'detailPaneResize', { 
                width: shouldCollapse ? 0 : width
            });
            $(e.target).toggleClass('collapsed', shouldCollapse);
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
                            self.trigger('detailPaneResize', { width:0, syncToRemote:false });
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

