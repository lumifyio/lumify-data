define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./multiple'
], function (defineComponent, withTypeContent, withHighlighting, template) {

    'use strict';

    return defineComponent(Multiple, withTypeContent, withHighlighting);

    function Multiple() {

        this.after('initialize', function () {
            var vertices = this.attr.data
                .filter(function (v) {
                    return v._type != 'relationship';
                });
            this.$node.html(template({getClasses: this.classesForVertex, vertices: vertices}));
            this.updateEntityAndArtifactDraggables();
        });
    }
});
