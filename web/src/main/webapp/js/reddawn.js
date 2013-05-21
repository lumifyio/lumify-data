
require.config({
    basePath: '/js',
    //urlArgs: "cache-bust=" +  Date.now(),
    paths: {
        flight: '../libs/flight'
    }
});

define([
    'app'
], function(App) {
    'use strict';

    App.attachTo('#app', {});
});


