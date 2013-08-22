
define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./properties',
    'tpl!./relationships',
    'service/ontology'
], function(defineComponent, withTypeContent, withHighlighting, template, propertiesTemplate, relationshipsTemplate, OntologyService) {

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

                var glyphIconHref = '';
                var concept = concepts.byId[self.attr.data._subType];
                if(concept) {
                    glyphIconHref = concept.glyphIconHref;
                }

                self.$node.html(template({
                    title: self.attr.data.originalTitle || self.attr.data.title || 'No title avaliable',
                    highlightButton: self.highlightButton(),
                    glyphIconHref: glyphIconHref,
                    id: self.attr.data.id || self.attr.data.graphVertexId
                }));

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
//                self.ontologyService.properties(function(err, ontologyProperties) {
//                    if(err) {
//                        console.error('Error', err);
//                        return self.trigger(document, 'error', { message: err.toString() });
//                    }
//
//                    console.log('ontologyProperties', ontologyProperties);
//                    console.log('properties', properties);

                    for(var i=0; i<properties.length; i++) {
                        var property = properties[i];
                        if(property.key == '_glyphIcon') {
                            self.select('glyphIconSelector').attr('src', '/resource/' + property.value);
                            break;
                        }
                    }
                    self.select('propertiesSelector').html(propertiesTemplate({properties: properties}));
//                });
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
