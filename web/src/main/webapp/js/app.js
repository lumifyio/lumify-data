
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
    'service/ucd'
], function(appTemplate, defineComponent, Menubar, Search, Workspaces, Users, Graph, Detail, Map, WorkspaceService, UcdService) {
    'use strict';

    return defineComponent(App);

    function App() {
        var WORKSPACE_SAVE_TIMEOUT = 1000;
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
            modeSelectSelector: '.mode-select'
        });

        this.after('initialize', function() {
            this.on(document, 'error', this.onError);
            this.on(document, 'menubarToggleDisplay', this.toggleDisplay);
            this.on(document, 'message', this.onMessage);
            this.on(document, 'searchResultSelected', this.onSearchResultSelection);

            var content = $(appTemplate({})),
                menubarPane = content.filter('.menubar-pane'),
                searchPane = content.filter('.search-pane'),
                workspacesPane = content.filter('.workspaces-pane'),
                usersPane = content.filter('.users-pane'),
                graphPane = content.filter('.graph-pane'),
                detailPane = content.filter('.detail-pane'),
                mapPane = content.filter('.map-pane');


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

            // Open search when the page is loaded
            this.trigger(document, 'menubarToggleDisplay', {name:'search'});
            this.trigger(document, 'menubarToggleDisplay', {name:'graph'});

            this.on(document, 'graphAddNode', this.onGraphAddNode);
            this.on(document, 'graphNodeMoved', this.onGraphNodeMoved);
            this.on(document, 'nodeUpdate', this.onNodeUpdate);

            this.on(document, 'switchWorkspace', this.onSwitchWorkspace);

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on(document, 'workspaceSave', this.onSaveWorkspace);
            this.on(document, 'workspaceDeleted', this.onWorkspaceDeleted);
            this.loadActiveWorkspace();
        });

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
                self.trigger(document, 'workspaceSaved', data);
            });
        };

        this.onWorkspaceLoaded = function(evt, data) {
            this.workspaceData = data;
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

        this.onNodeUpdate = function(evt, nodeUpdateData) {
            var nodes = this.workspaceData.data.nodes.filter(function(n) { return n.rowKey == nodeUpdateData.rowKey });
            nodes.forEach(function(node) {
                $.extend(node, nodeUpdateData);
            });
        };

        this.onGraphAddNode = function(evt, data) {
            this.workspaceData = this.workspaceData || {};
            this.workspaceData.data = this.workspaceData.data || {};
            this.workspaceData.data.nodes = this.workspaceData.data.nodes || [];
            this.workspaceData.data.nodes.push(data);
            this.setWorkspaceDirty();
            this.refreshRelationships();
        };

        this.onGraphNodeMoved = function(evt, nodeMoveData) {
            var nodes = this.workspaceData.data.nodes.filter(function(n) { return n.rowKey == nodeMoveData.id });
            nodes.forEach(function(node) {
                node.graphPosition = {
                    x: nodeMoveData.x,
                    y: nodeMoveData.y
                };
            });
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
                this.trigger(document, 'mapShow', this.workspaceData); // TODO this is annoying that we have to pass this. The problem is that the graph is lazily loaded.
            }

            pane.toggleClass('visible');
        };

        this.onMessage = function(e, data) {
            if (!this.select('usersSelector').hasClass('visible')) {
                this.trigger(document, 'menubarToggleDisplay', {name:'users'});
            }
        };

        this.onSearchResultSelection = function(e, data) {
            var detailPane = this.select('detailPaneSelector');
            var minWidth = 100;

            if (detailPane.width() < minWidth) {
                detailPane[0].style.width = null;
            }
            detailPane.removeClass('collapsed').addClass('visible');

            this.trigger(document, 'detailPaneResize', { width: detailPane.width() });
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

