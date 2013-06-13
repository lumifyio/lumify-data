//
// !!! Also add changes to test/runner/main.js for testing !!!
//

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
	atmosphere: '../libs/jquery.atmosphere/jquery.atmosphere'
  },
  shim: {
    ejs: { exports: 'ejs' },
    cytoscape: { exports: 'cytoscape' },
	atmosphere: { exports: 'jQuery' }
  },
  deps : ['reddawn']
};

cytoscapePlugins.forEach(function(plugin) {
  require.paths[plugin] = '../libs/cytoscape/' + plugin;
  require.shim[plugin] = { exports: 'jQuery' };
  require.shim.cytoscape.deps = require.shim.cytoscape.deps || [];
  require.shim.cytoscape.deps.push(plugin);
});
