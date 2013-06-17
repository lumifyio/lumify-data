
define([
    'flight/lib/component',
    'service/ucd',
    'tpl!./artifactDetails',
    'tpl!./entityDetails',
    'tpl!./relationshipDetails'
], function(defineComponent, UCD, artifactDetailsTemplate, entityDetailsTemplate, relationshipDetailsTemplate) {
    'use strict';

    return defineComponent(Detail);

    function Detail() {

        this.defaultAttrs({
            locationSelector: '.location',
        });

        this.after('initialize', function() {
            this.on('click', {
                locationSelector: this.onLocationClicked
            });
            this.on(document, 'searchResultSelected', this.onSearchResultSelected);
        });

        this.onLocationClicked = function(evt, data) {
            var $target = $(evt.target);
            var data = {
                latitude: $target.attr('latitude'),
                longitude: $target.attr('longitude')
            };
            this.trigger('mapCenter', data);
        };

        this.onSearchResultSelected = function(evt, data) {
            if(data.type == 'artifacts') {
                this.onArtifactSelected(evt, data);
            } else if(data.type == 'entities') {
                this.onEntitySelected(evt, data);
            } else if(data.type == 'relationship') {
                this.onRelationshipSelected(evt, data);
            } else {
                var message = 'Unhandled type: ' + data.type;
                console.error(message);
                return this.trigger(document, 'error', { message: message });
            }
        };

        this.onRelationshipSelected = function(evt, data) {
            var self = this;
            this.$node.html("Loading...");
            // TODO show something more useful here.
            console.log('Showing relationship:', data);
            self.$node.html(relationshipDetailsTemplate(data));
        };

        this.onArtifactSelected = function(evt, data) {
            var self = this;
            this.$node.html("Loading...");
            new UCD().getArtifactById(data.rowKey, function(err, artifact) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                console.log('Showing artifact:', artifact);
                artifact.contentHtml = artifact.Content.highlighted_text || artifact.Content.doc_extracted_text;
                artifact.contentHtml = artifact.contentHtml.replace(/[\n]+/g, "<br><br>\n");
                self.$node.html(artifactDetailsTemplate({ artifact: artifact }));
            });
        };

        this.onEntitySelected = function(evt, data) {
            var self = this;
            this.$node.html("Loading...");
            new UCD().getEntityById(data.rowKey, function(err, entity) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                console.log('Showing entity:', entity);
                self.$node.html(entityDetailsTemplate(entity));
            });
        };
    }
});
