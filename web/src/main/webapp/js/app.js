
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
    'util/undoManager'
], function(appTemplate, defineComponent, Menubar, Search, Workspaces, Users, Graph, Detail, Map, WorkspaceService, UcdService, undoManager) {
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
            modeSelectSelector: '.mode-select',
            droppableSelector: '.graph-pane, .map-pane'
        });

        this.after('initialize', function() {
            window.reddawnApp = this;

            this.on(document, 'error', this.onError);
            this.on(document, 'menubarToggleDisplay', this.toggleDisplay);
            this.on(document, 'message', this.onMessage);
            this.on(document, 'searchResultSelected', this.onSearchResultSelection);
            this.on(document, 'syncStarted', this.onSyncStarted);

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

            // Configure splitpane resizing
            resizable(searchPane, 'e');
            resizable(workspacesPane, 'e');
            resizable(detailPane, 'w', 4, 500, this.onDetailResize.bind(this));

            this.$node.html(content);

            this.setupDroppable();

            // Open search when the page is loaded
            this.trigger(document, 'menubarToggleDisplay', { name: searchPane.data(DATA_MENUBAR_NAME) });
            this.trigger(document, 'menubarToggleDisplay', { name: graphPane.data(DATA_MENUBAR_NAME) });

            this.on(document, 'nodesAdd', this.onNodesAdd);
            this.on(document, 'nodesUpdate', this.onNodesUpdate);
            this.on(document, 'nodesDelete', this.onNodesDelete);

            this.on(document, 'switchWorkspace', this.onSwitchWorkspace);

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'workspaceSave', this.onSaveWorkspace);
            this.on(document, 'workspaceDeleted', this.onWorkspaceDeleted);

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

        this.setupDroppable = function() {

            this.select('droppableSelector').droppable({

                drop: function( event, ui ) {
                    var draggable = ui.draggable,
                        droppable = $(event.target),
                        droppableOffset = droppable.offset();

                    var info = draggable.data('info') || draggable.parents('li').data('info');
                    if ( !info ) {
                        console.warn('No data-info attribute for draggable element found');
                        return;
                    }

                    var graphPosition = $(event.target).is('.graph-pane') ?
                        {
                            x: event.clientX - droppableOffset.left,
                            y: event.clientY - droppableOffset.top
                        } : {
                            x: parseInt(Math.random() * droppable.width(), 10),
                            y: parseInt(Math.random() * droppable.height(), 10)
                        };

                    this.trigger(document, 'nodesAdd', {
                        nodes: [{
                            title: info.title || draggable.text(),
                            rowKey: info.rowKey.replace(/\\[x](1f)/ig, '\u001f'),
                            subType: info.subType,
                            type: info.type,
                            graphPosition: graphPosition
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
            this.workspaceData = data;
            undoManager.reset();
            this.refreshRelationships();
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

        this.onNodesUpdate = function(evt, data) {
            var self = this;
            var undoData = {
                noUndo: true,
                nodes: []
            };
            var redoData = {
                noUndo: true,
                nodes: []
            };
            data.nodes.forEach(function(node) {
                var matchingWorkspaceNodes = self.workspaceData.data.nodes.filter(function(workspaceNode) { return workspaceNode.rowKey == node.rowKey; });
                matchingWorkspaceNodes.forEach(function(workspaceNode) {
                    undoData.nodes.push(JSON.parse(JSON.stringify(workspaceNode)));
                    $.extend(workspaceNode, node);
                    redoData.nodes.push(JSON.parse(JSON.stringify(workspaceNode)));
                });
            });

            if(!data.noUndo) {
                undoManager.performedAction( 'Update ' + undoData.nodes.length + ' nodes', {
                    undo: function() {
                        self.trigger(document, 'nodesUpdate', undoData);
                    },
                    redo: function() {
                        self.trigger(document, 'nodesUpdate', redoData);
                    },
                    bind: this
                });
            }

            this.setWorkspaceDirty();
        };

        this.onNodesAdd = function(evt, data) {
            var self = this;
            this.workspaceData = this.workspaceData || {};
            this.workspaceData.data = this.workspaceData.data || {};
            this.workspaceData.data.nodes = this.workspaceData.data.nodes || [];
            data.nodes.forEach(function(node) {
                self.workspaceData.data.nodes.push(node);
            });

            if(!data.noUndo) {
                var dataClone = JSON.parse(JSON.stringify(data));
                dataClone.noUndo = true;
                undoManager.performedAction( 'Add ' + dataClone.nodes.length + ' nodes', {
                    undo: function() {
                        self.trigger(document, 'nodesDelete', dataClone);
                    },
                    redo: function() {
                        self.trigger(document, 'nodesAdd', dataClone);
                    },
                    bind: this
                });
            }

            this.setWorkspaceDirty();
            this.refreshRelationships();
        };

        this.onNodesDelete = function(evt, data) {
            var self = this;

            // get all the workspace nodes to delete (used by the undo manager)
            var workspaceNodesToDelete = this.workspaceData.data.nodes
                .filter(function(workspaceNode) {
                    return data.nodes.filter(function(dataNode) {
                        return workspaceNode.rowKey == dataNode.rowKey;
                    }).length > 0;
                });

            // remove all workspace nodes from list
            this.workspaceData.data.nodes = this.workspaceData.data.nodes
                .filter(function(workspaceNode) {
                    return workspaceNodesToDelete.filter(function(workspaceNodeToDelete) {
                        return workspaceNode.rowKey == workspaceNodeToDelete.rowKey;
                    }).length == 0;
                });

            if(!data.noUndo) {
                var undoDataClone = JSON.parse(JSON.stringify({
                    noUndo: true,
                    nodes: workspaceNodesToDelete
                }));
                undoManager.performedAction( 'Delete ' + data.nodes.length + ' nodes', {
                    undo: function() {
                        self.trigger(document, 'nodesAdd', undoDataClone);
                    },
                    redo: function() {
                        self.trigger(document, 'nodesDelete', undoDataClone);
                    },
                    bind: this
                });
            }

            this.setWorkspaceDirty();
        };

        this.refreshRelationships = function() {
            var self = this;
            var entityIds = this.getEntityIds();
            var artifactIds = this.getArtifactIds();
            this.ucdService.getRelationships(entityIds, artifactIds, function(err, relationships) {
                if(err) {
                    console.error('Error', err);
                    return $this.trigger(document, 'error', { message: err.toString() });
                }
                self.trigger(document, 'relationshipsLoaded', { relationships: relationships });
            });
        };

        this.getEntityIds = function() {
            if (this.workspaceData.data === undefined || this.workspaceData.data.nodes === undefined) {
                return [];
            }
            return this.workspaceData.data.nodes
                .filter(function(node) {
                    return node.type == 'entities';
                })
                .map(function(node) {
                    return node.rowKey;
                });
        };

        this.getArtifactIds = function() {
            if (this.workspaceData.data === undefined || this.workspaceData.data.nodes === undefined) {
                return [];
            }
            return this.workspaceData.data.nodes
                .filter(function(node) {
                    return node.type == 'artifacts';
                })
                .map(function(node) {
                    return node.rowKey;
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
            this.trigger('detailPaneResize', { width:0, syncToRemote:false });

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

