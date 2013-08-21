

define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./multiple'
], function(defineComponent, withTypeContent, withHighlighting, template) {

    'use strict';

    return defineComponent(Multiple, withTypeContent, withHighlighting);

    function Multiple() {

        this.after('initialize', function() {
            this.$node.html(template({getClasses:this.classesForVertex, vertices:this.attr.data}));
            this.updateEntityAndArtifactDraggables();
        });
    }
});
