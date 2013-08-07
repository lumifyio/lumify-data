
define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./properties',
    'tpl!./relationships'
], function(defineComponent, withTypeContent, withHighlighting, template, propertiesTemplate, relationshipsTemplate) {

    'use strict';

    return defineComponent(Entity, withTypeContent, withHighlighting);

    function Entity() {

        this.defaultAttrs({
            propertiesSelector: '.entity-properties',
            relationshipsSelector: '.entity-relationships',
        });

        this.after('initialize', function() {
            this.$node.html(template({
                title: this.attr.data.originalTitle || this.attr.data.title || 'No Title',
                highlightButton: this.highlightButton(),
                id: this.attr.data.id
            }));

            this.loadEntity();
        });


        this.loadEntity = function() {
            var self = this;

            var nodeInfo = {
                id: this.attr.data.id,
                properties: {
                    title: this.attr.data.originalTitle || this.attr.data.title || 'No Title'
                }
            }

            this.getProperties(this.attr.data.id, function(properties) {
                self.select('propertiesSelector').html(propertiesTemplate({properties: properties}));
            });

            this.getRelationships(this.attr.data.id, function(relationships) {
                var relationshipsTplData = []

                relationships.forEach(function(relationship) {
                    var relationshipTplData = {};
                    relationshipTplData.relationship = relationship.relationship;

                    if(nodeInfo.id == relationship.relationship.sourceNodeId) {
                        relationshipTplData.sourceNode = nodeInfo;
                        relationshipTplData.destNode = relationship.node;
                    } else {
                        relationshipTplData.sourceNode = relationship.node;
                        relationshipTplData.destNode = nodeInfo;
                    }

                    relationshipsTplData.push(relationshipTplData);
                });

                self.select('relationshipsSelector').html(relationshipsTemplate({relationships: relationshipsTplData }))
            });
        };

        this.getProperties = function(graphNodeId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getNodeProperties(encodeURIComponent(graphNodeId), function(err, properties) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                console.log("Properties:", properties);
                callback(properties);
            }));
        };

        this.getRelationships = function(graphNodeId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getNodeRelationships(encodeURIComponent(graphNodeId), function(err, relationships) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                console.log("Relationships:", relationships);
                callback(relationships);
            }));
        };
    }
});
