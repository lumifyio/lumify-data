
define([
    'flight/lib/component',
    'tpl!app',
    'data',
    'menubar/menubar',
    'search/search',
    'workspaces/workspaces',
    'workspaces/overlay',
    'sync/sync',
    'users/users',
    'graph/graph',
    'detail/detail',
    'map/map',
    'util/keyboard'
], function(defineComponent, appTemplate, data, Menubar, Search, Workspaces, WorkspaceOverlay, Sync, Users, Graph, Detail, Map, Keyboard) {
    'use strict';

    return defineComponent(App);

    function App() {
        var MAX_RESIZE_TRIGGER_INTERVAL = 250;
        var DATA_MENUBAR_NAME = 'menubar-name';


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
            detailPaneSelector: '.detail-pane'
        });


        this.after('initialize', function() {
            window.lumifyApp = this;

            this.triggerPaneResized = _.debounce(this.triggerPaneResized.bind(this), 10);

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

            Sync.attachTo(window);
            Menubar.attachTo(menubarPane.find('.content'));
            Search.attachTo(searchPane.find('.content'));
            Workspaces.attachTo(workspacesPane.find('.content'));
            Users.attachTo(usersPane.find('.content'));
            Graph.attachTo(graphPane);
            Detail.attachTo(detailPane.find('.content'));
            // FIXME:
            // Map.attachTo(mapPane);
            Keyboard.attachTo(document);
            WorkspaceOverlay.attachTo(content.filter('.workspace-overlay'));

            // Configure splitpane resizing
            resizable(searchPane, 'e', 160, 200, this.onPaneResize.bind(this));
            resizable(workspacesPane, 'e', undefined, 200, this.onPaneResize.bind(this));
            resizable(detailPane, 'w', 4, 500, this.onPaneResize.bind(this));

            this.$node.html(content);

            // Open search when the page is loaded
            this.trigger(document, 'menubarToggleDisplay', { name: graphPane.data(DATA_MENUBAR_NAME) });

            this.on(document, 'windowResize', this.triggerPaneResized);
            this.on(document, 'mapCenter', this.onMapAction);
            this.on(document, 'changeView', this.onChangeView);

            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);

            this.setupWindowResizeTrigger();
            this.triggerPaneResized();

            data.loadActiveWorkspace();
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


        this.onWorkspaceLoaded = function(evt, workspace) {
            if (workspace.data.vertices.length === 0) {
                this.trigger(document, 'menubarToggleDisplay', { name:'search' });
            }
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

                self.trigger('refreshRelationships');
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
            if (data && data.remoteEvent) {
                return;
            }
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

