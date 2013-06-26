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
	atmosphere: '../libs/jquery.atmosphere/jquery.atmosphere',
    html2canvas: '../libs/html2canvas/html2canvas'
  },
  shim: {
    ejs: { exports: 'ejs' },
    cytoscape: { exports: 'cytoscape' },
	atmosphere: { exports: 'jQuery' },
    html2canvas: { exports: 'html2canvas' }
  },
  deps : ['reddawn']
};

cytoscapePlugins.forEach(function(plugin) {
  require.paths[plugin] = '../libs/cytoscape/' + plugin;
  require.shim[plugin] = { exports: 'jQuery' };
  require.shim.cytoscape.deps = require.shim.cytoscape.deps || [];
  require.shim.cytoscape.deps.push(plugin);
});
