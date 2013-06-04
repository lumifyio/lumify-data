var tests = Object.keys(window.__karma__.files).filter(function (file) {
    return (/^\/base\/test\/spec\/.*\.js$/).test(file);
});

// TODO: This duplicates index.html 'require' variable. Load that and override baseUrl
require.config({

    // Karma serves files from '/base'
    baseUrl: '/base/js',
    
    paths: {
        flight: '../libs/flight',
        text: '../libs/requirejs-text/text',
        ejs:  '../libs/ejs/ejs',
        tpl: '../libs/requirejs-ejs-plugin/rejs',
        cytoscape: '../libs/cytoscape/cytoscape',

        chai: '../libs/chai/chai',
        sinon: '../libs/sinon/lib/sinon',
        'sinon-chai': '../libs/sinon-chai/lib/sinon-chai',
        'flight-mocha': '../libs/flight-mocha/lib/flight-mocha'
    },

    shim: {
        ejs: {
            exports: 'ejs'
        },
        sinon: {
            exports: 'sinon'
        }
    },

    deps: [ 
        'chai', 
        'sinon', 
        '../libs/es5-shim/es5-shim',
        '../libs/es5-shim/es5-sham'  
    ],

    callback: function(chai, sinon) {
        sinon.spy = sinon.spy || {};

        require([
                'sinon-chai', 
                'sinon/util/event',
                'sinon/call',
                'sinon/stub',
                'sinon/spy',
                'sinon/mock',
                'flight-mocha'
        ], function(sinonChai) {

            // Use sinon as mocking framework
            chai.use(sinonChai);

            // Expose as global variables
            global.chai = chai;
            global.sinon = sinon;

            // Globals for assertions
            assert = chai.assert;
            expect = chai.expect;

            // Use the twitter flight interface to mocha
            //mocha.ui('bdd');
            mocha.ui('flight-mocha');

            // Run tests after loading
            if (tests.length) {
                require(tests, function() {
                    window.__karma__.start();
                });
            } else window.__karma__.start();
        });

    }

});

