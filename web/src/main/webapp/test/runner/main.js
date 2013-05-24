var tests = Object.keys(window.__karma__.files).filter(function (file) {
    return (/^\/base\/test\/.*\.js$/).test(file);
});

// TODO: This duplicates index.html 'require' variable. Load that and override baseUrl
require.config({

    // Karma serves files from '/base'
    baseUrl: '/base/js',

    paths: {
        flight: '../libs/flight',
        text: '../libs/requirejs-text/text',
        ejs:  '../libs/ejs/ejs',
        tpl: '../libs/requirejs-ejs-plugin/rejs'
    },

    shim: {
        ejs: {
            exports: 'ejs'
        }
    }

});

require(['../libs/chai/chai'], function(chai) {

  global.chai = chai;

  assert = chai.assert;
  should = chai.should();
  expect = chai.expect;

  if (tests.length) {
    require(tests, function() {
      window.__karma__.start();
    });
  } else window.__karma__.start();
});

