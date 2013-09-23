
define([
    'flight/lib/component',
    './image/image',
    '../withProperties',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./relationships',
    'util/vertexList/list',
    'detail/dropdowns/propertyForm/propForm',
    'service/ontology',
    'service/vertex',
    'sf'
], function(defineComponent, Image, withProperties, withTypeContent, withHighlighting, template, relationshipsTemplate, VertexList, PropertyForm, OntologyService, VertexService, sf) {

    'use strict';

    var ontologyService = new OntologyService();
    var vertexService = new VertexService();

    return defineComponent(Entity, withTypeContent, withHighlighting, withProperties);

    function Entity(withDropdown) {

        this.defaultAttrs({
            glyphIconSelector: '.entity-glyphIcon',
            propertiesSelector: '.properties',
            relationshipsSelector: '.relationships',
            addNewPropertiesSelector: '.add-new-properties',
            addPropertySelector: '.add-property',
            titleSelector: '.entity-title'
        });

        this.after('teardown', function() {
            this.$node.off('click.paneClick');
        });

        this.after('initialize', function() {
            var self = this;
            this.$node.on('click.paneClick', this.onPaneClicked.bind(this));

            this.handleCancelling(ontologyService.concepts(function(err, concepts) {
                if (err) {
                    console.error('handleCancelling', err);
                    return self.trigger(document, 'error', err);
                }

                var concept = concepts.byId[self.attr.data._subType];

                self.$node.html(template({
                    title: self.attr.data.originalTitle || self.attr.data.title || 'No title avaliable',
                    highlightButton: self.highlightButton(),
                    fullscreenButton: self.fullscreenButton([self.attr.data.id || self.attr.data.graphVertexId])
                }));

                Image.attachTo(self.select('glyphIconSelector'), {
                    data: self.attr.data,
                    service: self.entityService,
                    defaultIconSrc: concept && concept.glyphIconHref || ''
                });

                self.loadEntity();
            }));
        });

        this.loadEntity = function() {
            var self = this;
            var vertexInfo = {
                id: this.attr.data.id || this.attr.data.graphVertexId,
                properties: {
                    title: this.attr.data.originalTitle || this.attr.data.title || 'No title available',
                    graphVertexId: this.attr.data.graphVertexId,
                    _type: this.attr.data._type,
                    _subType: this.attr.data._subType,
                    _rowKey: this.attr.data._rowKey
                }
            };

            this.getProperties(vertexInfo.id, function(properties) {
                self.displayProperties (properties);
            });

            this.getRelationships(vertexInfo.id, function(relationships) {
                self.handleCancelling(self.ontologyService.relationships(function(err, ontologyRelationships) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    // Create source/dest/other properties
                    relationships.forEach(function(r) {
                        var src, dest, other;
                        if (vertexInfo.id == r.relationship.sourceVertexId) {
                            src = vertexInfo;
                            dest = other = r.vertex;
                        } else {
                            src = other = r.vertex;
                            dest = vertexInfo;
                        }

                        src.cssClasses = self.classesForVertex(src);
                        src.json = JSON.stringify($.extend({id:src.id,graphVertexId:src.id}, src.properties));
                        dest.cssClasses = self.classesForVertex(dest);
                        dest.json = JSON.stringify($.extend({id:dest.id,graphVertexId:dest.id}, dest.properties));

                        r.vertices = {
                            src: src,
                            dest: dest,
                            other: other
                        };
                        r.dataInfo = {
                            source: src.id,
                            target: dest.id,
                            _type: 'relationship',
                            relationshipType: r.relationship.label
                        };
                        r.displayLabel = ontologyRelationships.byTitle[r.relationship.label].displayName;
                    });

                    var groupedByType = _.groupBy(relationships, function(r) { 

                        // Has Entity are collected into references (no matter
                        // relationship direction
                        if (r.relationship.label === 'hasEntity') {
                            return 'references';
                        }

                        // Group all that are relations from this vertex (not dest)
                        if (r.relationship.sourceVertexId === vertexInfo.id) {
                            return r.displayLabel;
                        }

                        // Collect all relationships that are destined here
                        // into section
                        return 'inverse';
                    });
                    var sortedKeys = Object.keys(groupedByType);
                    sortedKeys.sort(function(a,b) {
                        // If in inverse group, sort by the type
                        if (a === b && a === 'inverse') {
                            return a.displayLabel === b.displayLabel ? 0 : a.displayLabel < b.displayLabel ? -1 : 1;
                        }

                        // If in references group sort by the title
                        if (a === b && a === 'references') {
                            return defaultSort(a.vertex.properties.title, b.vertex.properties.title);
                        }

                        // Specifies the special group sort order
                        var groups = { inverse:1, references:2 };
                        if (groups[a] && groups[b]) {
                            return defaultSort(groups[a], groups[b]);
                        } else if (groups[a]) {
                            return 1;
                        } else if (groups[b]) {
                            return -1;
                        }

                        return defaultSort(a, b);

                        function defaultSort(x,y) {
                            return x === y ? 0 : x < y ? -1 : 1;
                        }
                    });

                    var $rels = self.select('relationshipsSelector');
                    $rels.html(relationshipsTemplate({
                        relationshipsGroupedByType: groupedByType,
                        sortedKeys: sortedKeys
                    }));

                    VertexList.attachTo($rels.find('.references'), {
                        vertices: _.map(groupedByType.references, function(r) {
                            return $.extend({graphVertexId:r.vertices.other.id}, r.vertices.other.properties);
                        })
                    });

                }));
            });
        };

        this.getProperties = function(graphVertexId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getVertexProperties(encodeURIComponent(graphVertexId), function(err, data) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                callback(data.properties);
            }));
        };

        this.getRelationships = function(graphVertexId, callback) {
            var self = this;

            this.handleCancelling(this.ucdService.getVertexRelationships(encodeURIComponent(graphVertexId), function(err, data) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                return callback(data.relationships);
            }));
        };

        this.onPaneClicked = function(evt) {
            var $target = $(evt.target);

            if ($target.not('.add-new-properties') && $target.parents('.underneath').length === 0) {
                PropertyForm.teardownAll();
            }

            if ($target.is('.entity, .artifact, span.relationship')) {
                this.trigger('verticesSelected', $target.data('info'));
                evt.stopPropagation();
            }

        };

    }
});

