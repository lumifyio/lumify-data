
define([
    'search/search'
], function(Search) {
    'use strict';

    return initialize;

    function initialize() {
        Search.attachTo('#app:first-child');
    }
});


