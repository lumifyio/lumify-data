
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
            detailedObjectSelector: '.entity, .artifact, .relationship'
        });

        this.after('initialize', function() {
            this.on('click', {
                detailedObjectSelector: this.onDetailedObjectClicked
            });

            this.$node.html(template({
                title: this.attr.data.originalTitle || this.attr.data.title || 'No Title',
                highlightButton: this.highlightButton(),
                id: this.attr.data.id || this.attr.data.graphNodeId
            }));

            this.loadEntity();
        });


        this.loadEntity = function() {
            var self = this;
            var nodeInfo = {
                id: this.attr.data.id || this.attr.data.graphNodeId,
                properties: {
                    title: this.attr.data.originalTitle || this.attr.data.title || 'No Title',
                    graphNodeId: this.attr.data.graphNodeId,
                    type: this.attr.data.type,
                    subType: this.attr.data.subType,
                    _rowKey: this.attr.data._rowKey
                }
            };

            this.getProperties(this.attr.data.id || this.attr.data.graphNodeId, function(properties) {
                self.select('propertiesSelector').html(propertiesTemplate({properties: properties}));
            });

            this.getRelationships(this.attr.data.id || this.attr.data.graphNodeId, function(relationships) {
                var relationshipsTplData = [];

                relationships.forEach(function(relationship) {
                    var relationshipTplData = {};
                    relationshipTplData.relationship = relationship.relationship;
                    relationshipTplData.dataInfo = JSON.stringify({
                        source: relationship.relationship.sourceNodeId,
                        target: relationship.relationship.destNodeId,
                        type: 'relationship'
                    });

                    relationship.node.properties.graphNodeId = relationship.node.id;

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

        this.onDetailedObjectClicked = function(evt) {
            var self = this;
            var $target = $(evt.target);

            this.trigger(document, 'searchResultSelected', $target.data('info'));

            evt.stopPropagation();
        }
    }
});
