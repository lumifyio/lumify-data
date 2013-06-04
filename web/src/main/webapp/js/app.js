
define([
    'tpl!app',
    'flight/lib/component',
    'menubar/menubar',
    'search/search',
    'users/users',
    'graph/graph',
    'detail/detail'
], function(appTemplate, defineComponent, Menubar, Search, Users, Graph, Detail) {
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
            detailPaneSelector: '.detail-pane'
        });

        this.after('initialize', function() {
            this.on(document, 'error', this.onError);
            this.on(document, 'menubarToggleDisplay', this.toggleDisplay);

            var content = $(appTemplate({})),
                menubarPane = content.filter('.menubar-pane'),
                searchPane = content.filter('.search-pane'),
                usersPane = content.filter('.users-pane'),
                graphPane = content.filter('.graph-pane'),
                detailPane = content.filter('.detail-pane');


            Menubar.attachTo(menubarPane.find('.content'));
            Search.attachTo(searchPane.find('.content'));
            Users.attachTo(usersPane.find('.content'));
            Graph.attachTo(graphPane);
            Detail.attachTo(detailPane.find('.content'));

            // Configure splitpane resizing
            resizable(searchPane, 'e');
            resizable(detailPane, 'w', 4, 500);

            this.$node.html(content);


            // Open search when the page is loaded
            this.trigger(document, 'menubarToggleDisplay', {name:'search'});
        });


        this.toggleDisplay = function(e, data) {
            this.select(data.name + 'Selector').toggleClass('visible');
        };
    }


    function resizable( el, handles, minWidth, maxWidth ) {
        return el.resizable({
            handles: handles,
            minWidth: minWidth || 150,
            maxWidth: maxWidth || 300
        });
    }

});

