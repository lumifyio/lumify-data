define([
    'flight/lib/component',
    'data',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./relationship',
    'service/relationship',
    'detail/properties',
    'sf'
], function(
    defineComponent,
    appData,
    withTypeContent,
    withHighlighting,
    template,
    RelationshipService,
    Properties,
    sf) {
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

            this.loadRelationship();
        });


        this.loadRelationship = function() {
            var self = this,
                data = this.attr.data;

            this.ucdService.getVertexToVertexRelationshipDetails(
                    data.properties.source,
                    data.properties.target,
                    data.properties.relationshipType
            ).done(function(relationshipData) {
                self.$node.html(template({
                    highlightButton: self.highlightButton(),
                    relationshipData: relationshipData
                }));

                Properties.attachTo(self.select('propertiesSelector'), {
                    data: data
                });

                self.updateEntityAndArtifactDraggables();
            });
        };

        this.onVertexToVertexRelationshipClicked = function(evt) {
            var $target = $(evt.target);
            var id = $target.data('vertexId');
            this.trigger('verticesSelected', [appData.vertex(id)]);
        };

        this.onPaneClicked = function(evt) {
            var $target = $(evt.target);

            if ($target.is('.entity, .artifact, span.relationship')) {
                var id = $target.data('vertexId');
                this.trigger('verticesSelected', [appData.vertex(id)]);
                evt.stopPropagation();
            }
        };
    }
});











