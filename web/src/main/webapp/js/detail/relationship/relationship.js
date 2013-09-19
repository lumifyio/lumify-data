define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./relationship',
    'service/relationship',
    'sf'
], function(defineComponent, withTypeContent, withHighlighting, template, propertiesTemplate, PropertyForm, RelationshipService, sf) {
    'use strict';

    var relationshipService = new RelationshipService();

    return defineComponent(Relationship, withTypeContent, withHighlighting);

    function Relationship() {

        this.defaultAttrs({
            vertexToVertexRelationshipSelector: '.vertex-to-vertex-relationship',
            propertiesSelector: '.properties'
        });

        this.after('teardown', function() {
            this.$node.off('click.paneClick');
        });

        this.after('initialize', function() {
            this.$node.on('click.paneClick', this.onPaneClicked.bind(this));
            this.on('click', {
                vertexToVertexRelationshipSelector: this.onVertexToVertexRelationshipClicked
            });

            this.loadRelationship ();
        });


        this.loadRelationship = function() {
            var self = this,
                data = this.attr.data;

            this.ucdService.getVertexToVertexRelationshipDetails (data.source, data.target, data.relationshipType, function (err, relationshipData){
                if (err) {
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }

                self.$node.html (template({
                    highlightButton: self.highlightButton(),
                    relationshipData: relationshipData
                }));

                self.displayProperties(relationshipData.properties);
                self.updateEntityAndArtifactDraggables();
            });
        };

        this.onVertexToVertexRelationshipClicked = function(evt) {
            var self = this;
            var $target = $(evt.target);

            this.trigger('verticesSelected', $target.data('info'));
        };

        this.onPaneClicked = function(evt) {
            var $target = $(evt.target);

            if ($target.not('.add-relationship-properties') && $target.parents('.underneath').length === 0) {
                PropertyForm.teardownAll();
            }

            if ($target.is('.entity, .artifact, span.relationship')) {
                this.trigger('verticesSelected', $target.data('info'));
                evt.stopPropagation();
            }
        };
    }
});











