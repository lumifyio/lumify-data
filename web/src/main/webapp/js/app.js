
define([
    'search/search'
], function(Search) {
    'use strict';

    return initialize;

    function initialize() {
        var searchPane = $('.search-pane').resizable({
            handles: 'e',
            autoHide: true,
            minWidth: 150,
            maxWidth: 300
        });
        Search.attachTo(searchPane);

        var detailPane = $('.detail-pane').resizable({
            handles: 'w',
            autoHide: true,
            minWidth: 150,
            maxWidth: 300
        });
    }
});


