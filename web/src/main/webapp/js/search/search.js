
define([
    'flight/lib/component',
    'tpl!search/search'
], function(defineComponent, template) {
    'use strict';

    return defineComponent(Search);

    function Search() {
        this.after('initialize', function() {
            this.$node.html(template({}));
        });
    }

});


