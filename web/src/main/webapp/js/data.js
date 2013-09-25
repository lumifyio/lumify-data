

define([
    'flight/lib/component',
    'flight/lib/registry',
    'data/withVertexCache',
    'data/withAjaxFilters',
    'util/withAsyncQueue',
    'service/workspace',
    'service/ucd',
    'service/vertex',
    'util/undoManager',
], function(
    // Flight
    defineComponent, registry,
    // Mixins
    withVertexCache, withAjaxFilters, withAsyncQueue,
    // Service
    WorkspaceService, UcdService, VertexService, undoManager) {
    'use strict';

    var WORKSPACE_SAVE_DELAY = 1000,
        RELOAD_RELATIONSHIPS_DELAY = 250;

    return initializeData();

    function initializeData() {
        var DataComponent = defineComponent(Data, withAsyncQueue, withVertexCache, withAjaxFilters);
        DataComponent.attachTo(document);

        var instanceInfo = _.find(registry.findInstanceInfoByNode(document), function(info) {
            return info.instance.constructor === DataComponent;
        });

        if (instanceInfo) {
            return instanceInfo.instance;
        } else {
            throw "Unable to find data instance";
        }
    }

    function freeze(obj) {
        if (_.isArray(obj)) {
            return _.map(obj, function(v) {
                return Object.freeze(Object.create(v));
            });
        }

        return Object.freeze(obj);
    }

    function Data() {

        this.workspaceService = new WorkspaceService();
        this.ucdService = new UcdService();
        this.vertexService = new VertexService();
        this.id = null;

        this.defaultAttrs({
            droppableSelector: 'body'
        });


        this.after('initialize', function() {
            this.setupAsyncQueue('workspace');
            this.setupDroppable();

            this.onSaveWorkspace = _.debounce(this.onSaveWorkspace.bind(this), WORKSPACE_SAVE_DELAY);
            this.refreshRelationships = _.debounce(this.refreshRelationships.bind(this), RELOAD_RELATIONSHIPS_DELAY);

            // Vertices
            this.on('addVertices', this.onAddVertices);
            this.on('updateVertices', this.onUpdateVertices);
            this.on('deleteVertices', this.onDeleteVertices);
            this.on('refreshRelationships', this.refreshRelationships);

            // Workspaces
            this.on('saveWorkspace', this.onSaveWorkspace);
            this.on('switchWorkspace', this.onSwitchWorkspace);
            this.on('workspaceDeleted', this.onWorkspaceDeleted);
            this.on('workspaceDeleting', this.onWorkspaceDeleting);

            this.on(document, 'socketMessage', this.onSocketMessage);
        });

        this.onSocketMessage = function (evt, message) {
            var self = this;
            switch (message.type) {
                case 'propertiesChange':
                    self.trigger('updateVertices', { vertices:[message.data.vertex]});
                    break;
            }
        };

        this.onSaveWorkspace = function(evt, workspace) {
            var self = this,
                saveFn;

            if (self.id) {
                saveFn = self.workspaceService.save.bind(self.workspaceService, self.id);
            } else {
                saveFn = self.workspaceService.saveNew.bind(self.workspaceService);
            }

            self.workspaceReady(function(ws) {
                self.trigger('workspaceSaving', ws);
                saveFn({ data:self.workspaceVertices }).done(function(data) {
                    self.id = data._rowKey;
                    self.trigger('workspaceSaved', data);
                });
            });
        };


        this.refreshRelationships = function() {
            var self = this,
                ids = this.getIds();

            this.ucdService.getRelationships(ids)
                .done(function(relationships) {
                    self.trigger('relationshipsLoaded', { relationships: relationships });
                });
        };


        this.onAddVertices = function(evt, data) {
            this.workspaceReady(function(ws) {
                var self = this,
                    added = [],
                    existing = [];


                // Check if vertices are missing properties (from search results)
                var needsRefreshing = data.vertices.filter(function(v) { 
                        var cached = self.vertex(v.id);
                        if (!cached) {
                            return !v.properties || !v.properties._refreshedFromServer;
                        }
                        return !cached.properties._refreshedFromServer;
                    }),
                    passedWorkspace = {};

                data.vertices.forEach(function(v) {
                    passedWorkspace[v.id] = self.copy(v.workspace);
                });

                var deferred = $.Deferred();
                if (needsRefreshing.length) {
                    this.vertexService.getMultiple(_.pluck(needsRefreshing, 'id')).done(function(vertices) {
                        deferred.resolve(data.vertices);
                    });
                } else deferred.resolve(data.vertices);

                deferred.done(function(vertices) {
                    vertices = self.vertices(vertices);

                    vertices.forEach(function(vertex) {
                        vertex.properties._refreshedFromServer = true;
                        if (passedWorkspace[vertex.id]) {
                            vertex.workspace = passedWorkspace[vertex.id];
                        }
                        var inWorkspace = self.workspaceVertices[vertex.id],
                            cache = self.updateCacheWithVertex(vertex);
                        
                        self.workspaceVertices[vertex.id] = cache.workspace;

                        if (inWorkspace) {
                            existing.push(cache);
                        } else {
                            added.push(cache);
                        }
                    });

                    if (existing.length) self.trigger('existingVerticesAdded', { vertices:freeze(existing) });

                    if (added.length === 0) {
                        // TODO: make mixin
                        $(".graph-pane .instructions").text("No New Vertices Added");
                        return;
                    }

                    if(!data.noUndo) {
                        var dataClone = JSON.parse(JSON.stringify(data));
                        dataClone.noUndo = true;
                        undoManager.performedAction( 'Add ' + dataClone.vertices.length + ' vertices', {
                            undo: function() {
                                self.trigger('deleteVertices', dataClone);
                            },
                            redo: function() {
                                self.trigger('addVertices', dataClone);
                            }
                        });
                    }

                    self.trigger('refreshRelationships');
                    if (!data.remoteEvent) self.trigger('saveWorkspace');
                    self.trigger('verticesAdded', { 
                        vertices:freeze(added),
                        remoteEvent: data.remoteEvent
                    });
                });
            });
        };


        this.onUpdateVertices = function(evt, data) {
            var self = this;

            this.workspaceReady(function(ws) {
                var undoData = { noUndo: true, vertices: [] };
                var redoData = { noUndo: true, vertices: [] };

                var shouldSave = false,
                    updated = data.vertices.map(function(vertex) {
                        if (!vertex.id && vertex.graphVertexId) {
                            vertex = {
                                id: vertex.graphVertexId,
                                properties: vertex
                            };
                        }

                        // Only save if workspace updated
                        if (self.workspaceVertices[vertex.id] && vertex.workspace) {
                            shouldSave = true;
                        }


                        if (shouldSave) undoData.vertices.push(self.workspaceOnlyVertexCopy({id:vertex.id}));
                        var cache = self.updateCacheWithVertex(vertex);
                        if (shouldSave) redoData.vertices.push(self.workspaceOnlyVertexCopy(cache));
                        return cache;
                    });

                if(!data.noUndo && undoData.vertices.length) {
                    undoManager.performedAction( 'Update ' + undoData.vertices.length + ' vertices', {
                        undo: function() { self.trigger('updateVertices', undoData); },
                        redo: function() { self.trigger('updateVertices', redoData); }
                    });
                }

                if (shouldSave && !data.remoteEvent) {
                    this.trigger('saveWorkspace');
                }
                this.trigger('verticesUpdated', { 
                    vertices:freeze(updated),
                    remoteEvent: data.remoteEvent
                });
            });
        };

        this.onDeleteVertices = function(evt, data) {
            var self = this;

            this.workspaceReady(function(ws) {

                var toDelete = [],
                    undoDelete = [],
                    redoDelete = [];
                data.vertices.forEach(function(deletedVertex) {
                    var workspaceInfo = self.workspaceVertices[deletedVertex.id];
                    if (workspaceInfo) {
                        redoDelete.push(self.workspaceOnlyVertexCopy(deletedVertex.id));
                        undoDelete.push(self.copy(self.vertex(deletedVertex.id)));
                        toDelete.push(self.vertex(deletedVertex.id));

                        delete self.workspaceVertices[deletedVertex.id];
                    }
                    var cache = self.vertex(deletedVertex.id);
                    if (cache) {
                        cache.workspace = {};
                    }
                });

                if(!data.noUndo && undoDelete.length) {
                    undoManager.performedAction( 'Delete ' + toDelete.length + ' vertices', {
                        undo: function() { self.trigger(document, 'addVertices', { noUndo:true, vertices:undoDelete }); },
                        redo: function() { self.trigger(document, 'deleteVertices', { noUndo:true, vertices:redoDelete }); }
                    });
                }

                if (!data.remoteEvent) {
                    this.trigger('saveWorkspace');
                }
                this.trigger('verticesDeleted', { 
                    vertices:freeze(toDelete),
                    remoteEvent: data.remoteEvent
                });
            });
        };

        this.loadActiveWorkspace = function() {
            var self = this;
            self.workspaceService.list()
                .done(function(data) {
                    var workspaces = data.workspaces || [];

                    if (workspaces.length === 0) {
                        self.workspaceService.saveNew({data:{}}).done(function(workspace) {
                            self.loadWorkspace(workspace);
                        });
                        return;
                    }

                    for (var i = 0; i < workspaces.length; i++) {
                        if (workspaces[i].active) {
                            return self.loadWorkspace(workspaces[i]);
                        }
                    }

                    self.loadWorkspace(workspaces[0]);
                });
        };

        this.onSwitchWorkspace = function(evt, data) {
            if (data._rowKey != this.id) {
                this.loadWorkspace(data._rowKey);
            }
        };

        this.onWorkspaceDeleted = function(evt, data) {
            if (this.id === data._rowKey) {
                this.id = null;
                this.loadActiveWorkspace();
            }
        };


        this.onWorkspaceDeleting = function (evt, data) {
            if (this.id == data._rowKey) {
                // TODO: use activity to display message
            }
        };

        this.loadWorkspace = function(workspaceData) {
            var self = this,
                workspaceRowKey = _.isString(workspaceData) ? workspaceData : workspaceData._rowKey;

            self.id = workspaceRowKey;

            // Queue up any requests to modify workspace
            self.workspaceUnload();

            self.getWorkspace(workspaceRowKey).done(function(workspace) {
                self.loadWorkspaceVertices(workspace).done(function(vertices) {
                    if (workspaceData && workspaceData.title) {
                        workspace.title = workspaceData.title;
                    }
                    workspace.data.vertices = freeze(vertices.sort(function(a,b) { 
                        if (a.workspace.graphPosition && b.workspace.graphPosition) return 0;
                        return a.workspace.graphPosition ? -1 : b.workspace.graphPosition ? 1 : 0;
                    }));
                    self.workspaceMarkReady(workspace);
                    self.trigger('workspaceLoaded', workspace);
                });
            });
        };

        this.getWorkspace = function(id) {
            var self = this,
                deferred = $.Deferred();

            if (id) {
                self.workspaceService.getByRowKey(id).done(function(workspace) { 
                    deferred.resolve(workspace); 
                });
            } else {
                deferred.resolve();
            }
            return deferred.then(function(workspace) {
                    workspace = workspace || {};
                    workspace.data = workspace.data || {};

                    if (workspace.data.vertices) {
                        console.warn('Legacy workspace found, resetting');
                        workspace.data = {};
                        self.trigger('saveWorkspace');
                    }

                    return workspace;
                });
        };

        this.loadWorkspaceVertices = function(workspace) {
            var self = this,
                deferred = $.Deferred(),
                ids = Object.keys(workspace.data);

            self.workspaceVertices = {};
            if (ids.length) {
                self.vertexService.getMultiple(ids).done(function(serverVertices) {

                    var vertices = serverVertices.map(function(vertex) {
                        var workspaceData = workspace.data[vertex.id];

                        var cache = self.updateCacheWithVertex(vertex);
                        cache.properties._refreshedFromServer = true;
                        cache.workspace = workspaceData || {};
                        cache.workspace.selected = false;
                        self.workspaceVertices[vertex.id] = cache.workspace;

                        return cache;
                    });

                    undoManager.reset();

                    self.refreshRelationships();
                    deferred.resolve(vertices);
                });
            } else {
                deferred.resolve([]);
            }

            return deferred;
        };


        this.getIds = function () {
            return Object.keys(this.workspaceVertices);
        };

        this.setupDroppable = function() {
            var self = this;

            var enabled = false,
                droppable = this.select('droppableSelector');

            // Other droppables might be on top of graph, listen to 
            // their over/out events and ignore drops if the user hasn't
            // dragged outside of them. Can't use greedy option since they are
            // absolutely positioned
            $(document.body).on('dropover dropout', function(e, ui) {
                var target = $(e.target),
                    appDroppable = target.is(droppable),
                    parentDroppables = target.parents('.ui-droppable');

                if (appDroppable) {
                    // Ignore events from this droppable
                    return;
                }

                // If this droppable has no parent droppables
                if (parentDroppables.length === 1 && parentDroppables.is(droppable)) {
                    enabled = e.type === 'dropout';
                }
            });

            droppable.droppable({
                tolerance: 'pointer',
                accept: function(item) {
                    return true;
                },
                drop: function( event, ui ) {
                    
                    // Early exit if should leave to a different droppable
                    if (!enabled) return;

                    var draggable = ui.draggable,
                        droppable = $(event.target),
                        graphVisible = $('.graph-pane').is('.visible'),
                        alsoDragging = draggable.data('ui-draggable').alsoDragging,
                        refresh = [];

                    if (alsoDragging && alsoDragging.length) {
                        draggable.add(alsoDragging);
                    }
                    var vertices = draggable.map(function(i, a) {
                        a = $(a);
                        var id = a.data('vertexId') || a.closest('li').data('vertexId');
                        if (!id) {

                            // Highlighted entities (legacy info)
                            var info = a.data('info') || a.closest('li').data('info');
                            if (info && info.graphVertexId) {

                                // TODO: fix on server
                                if (info.type) {
                                    info._type = info.type;
                                    delete info.type;
                                }

                                self.updateCacheWithVertex({
                                    id: info.graphVertexId,
                                    properties: _.omit(info, 'start', 'end', 'graphVertexId')
                                });
                                id = info.graphVertexId;
                            } 
                            
                            if (!id) return console.error('No data-vertex-id attribute for draggable element found', draggable[0]);
                        }

                        var vertex = self.vertex(id);
                        if (vertex) {
                            if (graphVisible) {
                                vertex.workspace.dropPosition = { x: event.clientX, y: event.clientY };
                            }
                            return vertex;
                        } else refresh.push(id);
                    }).toArray();

                    if (refresh.length) {
                        this.vertexService.getMultiple(refresh).done(function() {
                            self.trigger('verticesDropped', { vertices:vertices });
                        });
                    } else {
                        self.trigger('verticesDropped', { vertices:vertices });
                    }

                }.bind(this)
            });
        };
    }
});
