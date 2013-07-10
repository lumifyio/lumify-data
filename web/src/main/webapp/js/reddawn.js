
// Debug retina/non-retina by changing to 1/2
// window.devicePixelRatio = 1;

require([
    'flight/lib/compose',
    'flight/lib/registry',
    'flight/lib/advice',
    'flight/lib/logger',
    'flight/tools/debug/debug',

    // Make jQuery plugins available
    'withinScrollable',
    'scrollStop'
],
function(compose, registry, advice, withLogging, debug) {

    debug.enable(true);
    DEBUG.events.logAll();

    require(['app'], function(App) {
        App.attachTo('#app');
    });
});


