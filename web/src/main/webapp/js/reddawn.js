
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

    'util/visibility',

    // Make jQuery plugins available
    'withinScrollable',
    'scrollStop'
],
function(compose, registry, advice, withLogging, debug, Visibility) {

    debug.enable(true);
    DEBUG.events.logAll();

    Visibility.attachTo(document);

    var ids = graphVertexIdsToOpen();

    if (ids && ids.length) {
        window.isFullscreenDetails = true;
        $(document.body).addClass('fullscreenDetails');
        require(['appFullscreenDetails'], function(FullscreenDetailApp) {
            FullscreenDetailApp.attachTo('#app', {
                graphVertexIds: ids
            });
        });
    } else {
        require(['app'], function(App) {
            App.attachTo('#app');
        });
    }


    function graphVertexIdsToOpen() {
        // http://...#v=1,2,3

        var h = location.hash;

        if (!h || h.length === 0) return;

        var m = h.match(/^#?v=(.+)$/);

        if (m && m.length === 2 && m[1].length) {
            return m[1].split(',');
        }
    }
});


