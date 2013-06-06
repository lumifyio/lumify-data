
var cytoscapePlugins = [
  'jquery.cytoscape-panzoom',
  'jquery.cytoscape-edgehandles',
];

var require = {
  baseUrl: '/js',
  urlArgs: "cache-bust=" +  Date.now(),
  paths: {
    flight: '../libs/flight',
    text: '../libs/requirejs-text/text',
    ejs:  '../libs/ejs/ejs',
    tpl: '../libs/requirejs-ejs-plugin/rejs',
    cytoscape: '../libs/cytoscape/cytoscape'
  },
  shim: {
    ejs: { exports: 'ejs' },
    cytoscape: { exports: 'cytoscape' }
  },
  deps : ['reddawn']
};

cytoscapePlugins.forEach(function(plugin) {
  require.paths[plugin] = '../libs/cytoscape/' + plugin;
  require.shim[plugin] = { exports: 'jQuery' };
  require.shim.cytoscape.deps = require.shim.cytoscape.deps || [];
  require.shim.cytoscape.deps.push(plugin);
});
