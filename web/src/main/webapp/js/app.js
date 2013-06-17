
define([
    'tpl!app',
    'flight/lib/component',
    'menubar/menubar',
    'search/search',
    'users/users',
    'graph/graph',
    'detail/detail',
    'map/map'
], function(appTemplate, defineComponent, Menubar, Search, Users, Graph, Detail, Map) {
    'use strict';

    return defineComponent(App);

    function App() {

        this.onError = function(evt, err) {
            alert("Error: " + err.message); // TODO better error handling
        };

        this.defaultAttrs({
            menubarSelector: '.menubar-pane',
            searchSelector: '.search-pane',
            usersSelector: '.users-pane',
            graphPaneSelector: '.graph-pane',
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
                usersPane = content.filter('.users-pane'),
                graphPane = content.filter('.graph-pane'),
                detailPane = content.filter('.detail-pane'),
                mapPane = content.filter('.map-pane');


            Menubar.attachTo(menubarPane.find('.content'));
            Search.attachTo(searchPane.find('.content'));
            Users.attachTo(usersPane.find('.content'));
            Graph.attachTo(graphPane);
            Detail.attachTo(detailPane.find('.content'));
            Map.attachTo(mapPane);

            // Configure splitpane resizing
            resizable(searchPane, 'e');
            resizable(detailPane, 'w', 4, 500, this.onDetailResize.bind(this));

            this.$node.html(content);

            // Open search when the page is loaded
            this.trigger(document, 'menubarToggleDisplay', {name:'search'});

            this.on(document, 'modeSelect', this.onModeSelect);
            this.on('click', {
                modeSelectSelector: this.onModeSelectClick
            });
            this.lastModeSelect = 'graph';
        });

        this.onModeSelectClick = function(evt, data) {
            var $target = $(evt.target);
            if ($target.hasClass('mode-select-graph')) {
                this.trigger(document, 'modeSelect', { mode: 'graph' });
            } else if ($target.hasClass('mode-select-map')) {
                this.trigger(document, 'modeSelect', { mode: 'map' });
            }
        };

        this.onModeSelect = function(evt, data) {
            var mode = data.mode;

            if (this.lastModeSelect) {
                if (this.lastModeSelect == 'graph') {
                    this.trigger(document, 'graphHide');
                } else if (this.lastModeSelect == 'map') {
                    this.trigger(document, 'mapHide');
                }
            }

            if (mode == 'graph') {
                this.trigger(document, 'graphShow');
            } else if (mode == 'map') {
                this.trigger(document, 'mapShow');
            }

            this.lastModeSelect = mode;
        };

        this.toggleDisplay = function(e, data) {
            this.select(data.name + 'Selector').toggleClass('visible');
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

