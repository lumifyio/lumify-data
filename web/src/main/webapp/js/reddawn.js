
require([
    'flight/lib/compose',
    'flight/lib/registry',
    'flight/lib/advice',
    'flight/lib/logger',
    'flight/tools/debug/debug'
],
function(compose, registry, advice, withLogging, debug) {

    debug.enable(true);
    DEBUG.events.logAll();

    require(['app'], function(App) {
        App.attachTo('#app');
    });
});


