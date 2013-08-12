define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./relationship',
    'underscore'
], function(defineComponent, withTypeContent, withHighlighting, template, _) {

    'use strict';

    return defineComponent(Relationship, withTypeContent, withHighlighting);

    function Relationship() {

        this.defaultAttrs({
            nodeToNodeRelationshipSelector: '.node-to-node-relationship',
        });

        this.after('initialize', function() {

            this.on('click', {
                nodeToNodeRelationshipSelector: this.onNodeToNodeRelationshipClicked
            });

            var data = this.attr.data;
            this.loadRelationship ();
        });


        this.loadRelationship = function() {
            var self = this,
                data = this.attr.data;

            this.ucdService.getNodeToNodeRelationshipDetails (data.source, data.target, function (err, relationshipData){
                if (err) {
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }

                self.$node.html (template({
                    highlightButton: self.highlightButton(),
                    relationshipData: relationshipData
                }));
                self.updateEntityAndArtifactDraggables();
            });
        };

        this.onNodeToNodeRelationshipClicked = function(evt) {
            var self = this;
            var $target = $(evt.target);

            this.trigger (document, 'searchResultSelected',  $target.data('info'));
        };
    }
});











