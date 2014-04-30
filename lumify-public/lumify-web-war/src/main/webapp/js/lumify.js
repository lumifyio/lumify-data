
// Debug retina/non-retina by changing to 1/2
// window.devicePixelRatio = 1;

// Call this function and reload to enable liveReload when `grunt` is running
window.enableLiveReload = function(enable) {
    if ('localStorage' in window) {
        if (enable === true || typeof enable === 'undefined') {
            console.debug('Enabling LiveReload...')
            require(['//localhost:35729/livereload.js'], function() {
                console.debug('LiveReload successfully enabled');
            });
            localStorage.setItem('liveReloadEnabled', true);
        } else {
            console.debug('Disabling LiveReload')
            localStorage.removeItem('liveReloadEnabled');
        }
    }
}
if ('localStorage' in window) {
    if (localStorage.getItem('liveReloadEnabled')) {
        enableLiveReload(true);
    }
}
window.gremlins = function() {
    require(['gremlins'], function(gremlins) {
        gremlins.createHorde()
        .gremlin(gremlins.species.formFiller())
        .gremlin(gremlins.species.clicker())
        .gremlin(
            gremlins.species.clicker()
            .clickTypes(['click'])
            .canClick(function(element) {
                return $(element).is('button,a,li') &&
                    $(element).closest('.logout').length === 0;
            }))
        .gremlin(gremlins.species.typer())
        .mogwai(gremlins.mogwais.fps())
        .unleash({nb: 1000});
    })
}

window.requestAnimationFrame =
    typeof window === 'undefined' ?
    function() { } :
    (
        window.requestAnimationFrame ||
        window.mozRequestAnimationFrame ||
        window.webkitRequestAnimationFrame ||
        window.msRequestAnimationFrame ||
        function(callback) {
            setTimeout(callback, 1000 / 60);
        }
    );
window.TRANSITION_END = 'transitionend webkitTransitionEnd MSTransitionEnd oTransitionEnd otransitionend';
window.ANIMATION_END = 'animationend webkitAnimationEnd MSAnimationEnd oAnimationEnd oanimationend';

require([
    'jquery',
    'jqueryui',
    'bootstrap',
    'es5shim',
    'es5sham',

    'flight/lib/compose',
    'flight/lib/registry',
    'flight/lib/advice',
    'flight/lib/logger',
    'flight/lib/debug',

     // Make underscore available everywhere
    'underscore',

    'util/visibility',
    'util/privileges',
    'service/user',

    'easing',
    'scrollStop',
    'bootstrap-datepicker',
    'util/jquery.flight',
    'util/jquery.removePrefixedClasses',

    // Handlebar helpers (also add to test/unit/runner/testRunner.js)
    'util/handlebars/eachWithLimit',
    'util/handlebars/date'
],
function(jQuery,
         jQueryui,
         bootstrap,
         es5shim,
         es5sham,
         compose,
         registry,
         advice,
         withLogging,
         debug,
         _,
         Visibility,
         Privileges,
         UserService) {
    'use strict';

    configureApplication();

    function configureApplication() {
        // Flight Logging
        try {
            debug.enable(true);
            DEBUG.events.logNone();
        } catch(e) {
            console.warn('Error enabling DEBUG mode for flight, probably because Safari Private Browsing enabled', e);
        }

        // Default templating
        _.templateSettings.interpolate = /\{([\s\S]+?)\}/g;

        // Default datepicker options
        $.fn.datepicker.defaults.format = 'yyyy-mm-dd';
        $.fn.datepicker.defaults.autoclose = true;

        Visibility.attachTo(document);
        Privileges.attachTo(document);
        $(window).on('hashchange', loadApplicationTypeBasedOnUrlHash);

        loadApplicationTypeBasedOnUrlHash();
    }

    /**
     * Switch between lumify and lumify-fullscreen-details based on url hash
     */
    function loadApplicationTypeBasedOnUrlHash(e) {
        var toOpen = graphVertexIdsToOpen(),

            ids = toOpen && toOpen.ids,

            workspaceId = toOpen && toOpen.workspaceId,

            // Is this the popoout details app? ids passed to hash?
            popoutDetails = !!(ids && ids.length),

            // If this is a hash change
            event = e && e.originalEvent,

            // Is this the default lumify application?
            mainApp = !popoutDetails;

        // Ignore hash change if not in popout and not going to popout
        if (event && !isPopoutUrl(event.newURL) && !isPopoutUrl(event.oldURL)) {
            return;
        }

        new UserService().isLoginRequired()
            .done(function() {
                attachApplication(false);
            })
            .fail(function(message, options) {
                attachApplication(true, message, options);
            });

        function attachApplication(loginRequired, message, options) {
            var user = !loginRequired && window.currentUser;

            $('html')
                .toggleClass('fullscreenApp', mainApp)
                .toggleClass('fullscreenDetails', popoutDetails);

            window.isFullscreenDetails = popoutDetails;

            if (loginRequired) {
                require(['login'], function(Login) {
                    Login.teardownAll();
                    Login.attachTo('#login', {
                        errorMessage: message,
                        errorMessageOptions: options
                    });
                });
            } else if (popoutDetails) {
                $('#login').remove();
                require(['appFullscreenDetails'], function(PopoutDetailsApp) {
                    PopoutDetailsApp.teardownAll();
                    PopoutDetailsApp.attachTo('#app', {
                        graphVertexIds: ids,
                        workspaceId: workspaceId
                    });
                });
            } else {
                $('#login').remove();
                require(['app'], function(App) {
                    if (event) {
                        location.replace(location.href);
                    } else {
                        App.teardownAll();
                        App.attachTo('#app');
                    }

                    _.defer(function() {
                        // Cache login in case server goes down
                        require(['login'], function(Login) {
                        });
                    });
                });
            }
        }
    }

    function isPopoutUrl(url) {
        var toOpen = graphVertexIdsToOpen(url)

        return toOpen && toOpen.ids && toOpen.ids.length;
    }

    function graphVertexIdsToOpen(url) {
        // http://...#v=1,2,3&w=[workspaceid]

        var h = location.hash;

        if (url) {
            var urlMatch = url.match(/.*?(#.*)$/);
            if (urlMatch) {
                h = urlMatch[1];
            } else h = '';
        }

        if (!h || h.length === 0) return;

        var m = h.match(/^#?v=(.+?)(?:&w=(.+))?$/);

        if (m && m.length === 3) {
            var vertexIds = [],
                workspaceId = null;

            if (m[1]) {
                vertexIds = decodeURIComponent(m[1]).split(',');
            }

            if (m[2]) {
                workspaceId = decodeURIComponent(m[2]);
            }

            return {
                ids: vertexIds,
                workspaceId: workspaceId
            };
        }
    }
});
