
var jQueryPlugins = {
  atmosphere: '../libs/jquery.atmosphere/jquery.atmosphere',
  withinScrollable: 'util/jquery.withinScrollable',
  flightJquery: 'util/jquery.flight',
  'bootstrap-datepicker': '../libs/bootstrap-datepicker/js/bootstrap-datepicker',
  removePrefixedClasses: 'util/jquery.removePrefixedClasses',
  scrollStop: '../libs/jquery-scrollstop/jquery.scrollstop',
  bigText: '../libs/jquery-bigtext/jquery-bigtext'
};

var cytoscapePlugins = [
  'jquery.cytoscape-panzoom'
];

var require = {
  baseUrl: '/js',
  urlArgs: "cache-bust=" +  Date.now(),
  paths: {
    flight: '../libs/flight',
    text: '../libs/requirejs-text/text',
    ejs:  '../libs/ejs/ejs',
    tpl: '../libs/requirejs-ejs-plugin/rejs',
    cytoscape: '../libs/cytoscape/cytoscape',
    arbor: '../libs/cytoscape/arbor',
    videojs: '../libs/video.js/video',
    underscore: '../libs/underscore/underscore',
    colorjs: '../libs/color-js/color',
    sf: '../libs/sf/sf',
    d3: '../libs/d3/d3.v3',
    three: '../libs/3djs/src/three/lib/three',
    'three-trackball' : '../libs/3djs/src/three/plugins/TrackballControls',
    'three-stats' : '../libs/3djs/src/three/lib/stats',
    '3djs': '../libs/3djs/src/3djs',
    intercom: '../libs/intercom/intercom.amd'
  },
  shim: {
    ejs: { exports: 'ejs' },
    three: { exports: 'THREE' },
    'three-trackball': { exports: 'THREE', deps:['three'] },
    'three-stats': { exports: 'Stats', deps:['three'] },
    cytoscape: { exports: 'cytoscape', deps:['arbor'] },
    colorjs: { init: function() { return this.net.brehaut.Color; } },
    videojs: { exports: 'videojs' },
	underscore: { exports: '_' },
    d3: { exports: 'd3' }
  },
  deps : ['lumify']
};

Object.keys(jQueryPlugins).forEach(function(plugin) {
  require.paths[plugin] = jQueryPlugins[plugin];
  require.shim[plugin] = { exports: 'jQuery' };
});

cytoscapePlugins.forEach(function(plugin) {
  require.paths[plugin] = '../libs/cytoscape/' + plugin;
  require.shim[plugin] = { exports: 'jQuery' };
  require.shim.cytoscape.deps = require.shim.cytoscape.deps || [];
  require.shim.cytoscape.deps.push(plugin);
});


// For testing to use this configuration test/runner/main.js
if ('define' in window) {
    define([], function() {
        return require;
    });
}
