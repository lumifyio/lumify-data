
define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./properties',
    'tpl!./relationships',
    'service/entity'
], function(defineComponent, withTypeContent, withHighlighting, template, propertiesTemplate, relationshipsTemplate, EntityService) {

    'use strict';

    var entityService = new EntityService();

    return defineComponent(Entity, withTypeContent, withHighlighting);

    function Entity() {

        this.defaultAttrs({
            glyphIconSelector: '.entity-glyphIcon',
            propertiesSelector: '.entity-properties',
            relationshipsSelector: '.entity-relationships',
            detailedObjectSelector: '.entity, .artifact, .relationship'
        });

        this.after('initialize', function() {
            var self = this;
            this.on('click', {
                detailedObjectSelector: this.onDetailedObjectClicked
            });

            entityService.concepts(function(err, concepts, conceptMap) {
                if (err) {
                    return self.trigger(document, 'error', err);
                }

                var glyphIconHref = '';
                var concept = conceptMap[self.attr.data._subType];
                if(concept) {
                    glyphIconHref = concept.glyphIconHref;
                }

                self.$node.html(template({
                    title: self.attr.data.originalTitle || self.attr.data.title || 'No Title',
                    highlightButton: self.highlightButton(),
                    glyphIconHref: glyphIconHref,
                    id: self.attr.data.id || self.attr.data.graphNodeId
                }));

                self.loadEntity();
            });
        });


        this.loadEntity = function() {
            var self = this;
            var nodeInfo = {
                id: this.attr.data.id || this.attr.data.graphNodeId,
                properties: {
                    title: this.attr.data.originalTitle || this.attr.data.title || 'No Title',
                    graphNodeId: this.attr.data.graphNodeId,
                    _type: this.attr.data._type,
                    _subType: this.attr.data._subType,
                    _rowKey: this.attr.data._rowKey
                }
            };

            this.getProperties(this.attr.data.id || this.attr.data.graphNodeId, function(properties) {
                for(var i=0; i<properties.length; i++) {
                    var property = properties[i];
                    if(property.key == '_glyphIcon') {
                        self.select('glyphIconSelector').attr('src', '/resource/' + property.value);
                        break;
                    }
                }
                self.select('propertiesSelector').html(propertiesTemplate({properties: properties}));
            });

            this.getRelationships(this.attr.data.id || this.attr.data.graphNodeId, function(relationships) {
                var relationshipsTplData = [];

                relationships.forEach(function(relationship) {
                    var data = {};
                    data.relationship = relationship.relationship;
                    data.dataInfo = JSON.stringify({
                        source: relationship.relationship.sourceNodeId,
                        target: relationship.relationship.destNodeId,
                        _type: 'relationship',
                        relationshipType: relationship.relationship.label
                    });

                    relationship.node.properties.graphNodeId = relationship.node.id;

                    if(nodeInfo.id == relationship.relationship.sourceNodeId) {
                        data.sourceNode = nodeInfo;
                        data.sourceNode.cssClasses = self.classesForNode(nodeInfo);

                        data.destNode = relationship.node;
                        data.destNode.cssClasses = self.classesForNode(relationship.node);
                    } else {
                        data.sourceNode = relationship.node;
                        data.sourceNode.cssClasses = self.classesForNode(relationship.node);

                        data.destNode = nodeInfo;
                        data.destNode.cssClasses = self.classesForNode(nodeInfo);
                    }

                    relationshipsTplData.push(data);
                });
                self.select('relationshipsSelector').html(relationshipsTemplate({relationships: relationshipsTplData }));
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
