
// Debug retina/non-retina by changing to 1/2
// window.devicePixelRatio = 1;

window.requestAnimationFrame = 
    typeof window === 'undefined' ? 
    function(){} : 
    ( window.requestAnimationFrame || 
      window.mozRequestAnimationFrame ||
      window.webkitRequestAnimationFrame ||
      window.msRequestAnimationFrame || function(callback) { setTimeout(callback, 1000/60); } );

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


