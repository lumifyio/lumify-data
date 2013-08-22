
define([
    'flight/lib/component',
    './image/image',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./properties',
    'tpl!./relationships',
    'service/ontology',
    'sf'
], function(defineComponent, Image, withTypeContent, withHighlighting, template, propertiesTemplate, relationshipsTemplate, OntologyService, sf) {

    'use strict';

    var ontologyService = new OntologyService();

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

            ontologyService.concepts(function(err, concepts) {
                if (err) {
                    return self.trigger(document, 'error', err);
                }

                var concept = concepts.byId[self.attr.data._subType];

                self.$node.html(template({
                    title: self.attr.data.originalTitle || self.attr.data.title || 'No title avaliable',
                    highlightButton: self.highlightButton(),
                    id: self.attr.data.id || self.attr.data.graphVertexId
                }));

                Image.attachTo(self.select('glyphIconSelector'), {
                    data: self.attr.data,
                    service: self.entityService,
                    defaultIconSrc: concept && concept.glyphIconHref || ''
                });

                self.loadEntity();
            });
        });


        this.loadEntity = function() {
            var self = this;
            var vertexInfo = {
                id: this.attr.data.id || this.attr.data.graphVertexId,
                properties: {
                    title: this.attr.data.originalTitle || this.attr.data.title || 'No title avaliable',
                    graphVertexId: this.attr.data.graphVertexId,
                    _type: this.attr.data._type,
                    _subType: this.attr.data._subType,
                    _rowKey: this.attr.data._rowKey
                }
            };

            this.getProperties(this.attr.data.id || this.attr.data.graphVertexId, function(properties) {
                self.ontologyService.properties(function(err, ontologyProperties) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    var propertiesTpl = [];
                    for(var i=0; i<properties.length; i++) {
                        var property = properties[i];
                        var displayName;
                        var value;
                        var ontologyProperty = ontologyProperties.byTitle[property.key];
                        if (ontologyProperty) {
                            displayName = ontologyProperty.displayName;
                            if(ontologyProperty.dataType == 'date') {
                                value = sf("{0:yyyy/MM/dd}", new Date(property.value));
                            } else {
                                value = property.value;
                            }
                        } else {
                            displayName = property.key;
                            value = property.value;
                        }

                        var data = {
                            key: property.key,
                            value: value,
                            displayName: displayName
                        };

                        if(property.key.charAt(0) != '_') {
                            propertiesTpl.push(data);
                        }

                        if(property.key == '_glyphIcon') {
                            self.trigger(self.select('glyphIconSelector'), 'iconUpdated', { src: property.value });
                            break;
                        }

                    }
                    self.select('propertiesSelector').html(propertiesTemplate({properties: propertiesTpl}));
                });
            });

            this.getRelationships(this.attr.data.id || this.attr.data.graphVertexId, function(relationships) {
                self.ontologyService.relationships(function(err, ontologyRelationships) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    var relationshipsTplData = [];

                    relationships.forEach(function(relationship) {
                        var ontologyRelationship = ontologyRelationships.byTitle[relationship.relationship.label];
                        var displayName;
                        if(ontologyRelationship) {
                            displayName = ontologyRelationship.displayName;
                        } else {
                            displayName = relationship.relationship.label;
                        }

                        var data = {};
                        data.displayName = displayName;
                        data.relationship = relationship.relationship;
                        data.dataInfo = JSON.stringify({
                            source: relationship.relationship.sourceVertexId,
                            target: relationship.relationship.destVertexId,
                            _type: 'relationship',
                            relationshipType: relationship.relationship.label
                        });

                        relationship.vertex.properties.graphVertexId = relationship.vertex.id;

                        if(vertexInfo.id == relationship.relationship.sourceVertexId) {
                            data.sourceVertex = vertexInfo;
                            data.sourceVertex.cssClasses = self.classesForVertex(vertexInfo);

                            data.destVertex = relationship.vertex;
                            data.destVertex.cssClasses = self.classesForVertex(relationship.vertex);
                        } else {
                            data.sourceVertex = relationship.vertex;
                            data.sourceVertex.cssClasses = self.classesForVertex(relationship.vertex);

                            data.destVertex = vertexInfo;
                            data.destVertex.cssClasses = self.classesForVertex(vertexInfo);
                        }

                        relationshipsTplData.push(data);
                    });
                    return self.select('relationshipsSelector').html(relationshipsTemplate({relationships: relationshipsTplData }));
                });
            });
        };

        this.getProperties = function(graphVertexId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getVertexProperties(encodeURIComponent(graphVertexId), function(err, properties) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                console.log("Properties:", properties);
                callback(properties);
            }));
        };

        this.getRelationships = function(graphVertexId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getVertexRelationships(encodeURIComponent(graphVertexId), function(err, relationships) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                console.log("Relationships:", relationships);
                return callback(relationships);
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
