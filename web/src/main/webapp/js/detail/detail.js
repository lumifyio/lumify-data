
define([
    'flight/lib/component',
    'tpl!./detail'
], function(defineComponent, template) {
    'use strict';

    return defineComponent(Detail);

    function Detail() {
        this.after('initialize', function() {

            this.$node.html(template({
                text: 'Detail pane'
            }));

        });
    }
});
