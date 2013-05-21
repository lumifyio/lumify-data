
define([
    'flight/lib/component',
    'search/search'
], function(defineComponent) {
    'use strict';

    return defineComponent(App);

    function App() {
        this.defaultAttrs({
            paneItemSelector: '.pane'
        });
    }
});


