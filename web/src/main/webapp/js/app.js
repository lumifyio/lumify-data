
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
], function(appTemplate, defineComponent, Menubar, Search, Workspaces, Users, Graph, Detail, Map, WorkspaceService) {
    'use strict';

    return defineComponent(App);

    function App() {
        this.workspaceService = new WorkspaceService();

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

            this.on(document, 'workspaceSave', this.onSaveWorkspace);
            this.loadCurrentWorkspace();
        });

        this.loadCurrentWorkspace = function() {
            var self = this;
            self.workspaceService.getIds(function(err, ids) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                if(ids.length === 0) {
                    self.loadWorkspace(null);
                } else {
                    self.loadWorkspace(ids[0]); // TODO handle more workspaces
                }
            });
        };

        this.loadWorkspace = function(workspaceRowKey) {
            var self = this;
            self.workspaceRowKey = workspaceRowKey;
            if(self.workspaceRowKey == null) {
                self.trigger(document, 'workspaceLoaded', {});
                return;
            }

            self.workspaceService.getByRowKey(self.workspaceRowKey, function(err, data) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                self.trigger(document, 'workspaceLoaded', data);
            });
        };

        this.onSaveWorkspace = function(evt, data) {
            var $this = this;
            var saveFn;
            if($this.workspaceRowKey) {
                saveFn = $this.workspaceService.save.bind($this.workspaceService, $this.workspaceRowKey);
            } else {
                saveFn = $this.workspaceService.saveNew.bind($this.workspaceService);
            }

            $this.trigger(document, 'workspaceSaving', data);
            saveFn(data, function(err, data) {
                if(err) {
                    console.error('Error', err);
                    return $this.trigger(document, 'error', { message: err.toString() });
                }
                $this.trigger(document, 'workspaceSaved', data);
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

