//
// !!! Also add changes to test/runner/main.js for testing !!!
//

var jQueryPlugins = {
  atmosphere: '../libs/jquery.atmosphere/jquery.atmosphere',
  withinScrollable: 'util/jquery.within-scrollable',
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
    html2canvas: '../libs/html2canvas/html2canvas',
    videojs: '../libs/video.js/video',
    underscore: '../libs/underscore/underscore',
    colorjs: '../libs/color-js/color',
    sf: '../libs/sf/sf'
  },
  shim: {
    ejs: { exports: 'ejs' },
    cytoscape: { exports: 'cytoscape', deps:['arbor'] },
    html2canvas: { exports: 'html2canvas' },
    videojs: { exports: 'videojs' },
	underscore: { exports: '_' }
  },
  deps : ['reddawn']
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
