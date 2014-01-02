
define([
    'flight/lib/component',
    'data',
    './image/image',
    '../properties',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./entity',
    'tpl!./relationships',
    'util/vertexList/list',
    'detail/dropdowns/propertyForm/propForm',
    'service/ontology',
    'service/vertex',
    'sf'
], function(defineComponent, 
    appData,
    Image,
    Properties,
    withTypeContent,
    withHighlighting,
    template,
    relationshipsTemplate,
    VertexList,
    PropertyForm,
    OntologyService,
    VertexService,
    sf) {
    'use strict';

    var ontologyService = new OntologyService();
    var vertexService = new VertexService();

    return defineComponent(Entity, withTypeContent, withHighlighting);

    function Entity(withDropdown) {

        this.defaultAttrs({
            glyphIconSelector: '.entity-glyphIcon',
            propertiesSelector: '.properties',
            relationshipsSelector: '.relationships',
            titleSelector: '.entity-title'
        });

        this.after('teardown', function() {
            this.$node.off('click.paneClick');
        });

        this.after('initialize', function() {
            var self = this;
            this.$node.on('click.paneClick', this.onPaneClicked.bind(this));

            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on('infiniteScrollRequest', this.handleReferenceLoadingRequest);

            self.loadEntity();
        });

        this.onVerticesUpdated = function(event, data) {
            var self = this;

            data.vertices.forEach(function(vertex) {
                if (vertex.id === self.attr.data.id) {
                    self.select('titleSelector').html(vertex.properties.title);
                }
            });
        };

        this.loadEntity = function() {
            var self = this;

            $.when( 
                this.handleCancelling(appData.refresh(this.attr.data)),
                this.handleCancelling(ontologyService.concepts())
            ).done(function(vertex, concepts) {
                var concept = concepts.byId[self.attr.data.properties._subType];

                self.$node.html(template({
                    vertex: self.attr.data,
                    fullscreenButton: self.fullscreenButton([self.attr.data.id]),
                    auditsButton: self.auditsButton()
                }));

                Image.attachTo(self.select('glyphIconSelector'), {
                    data: self.attr.data,
                    service: self.entityService,
                    defaultIconSrc: concept && concept.glyphIconHref || ''
                });

                Properties.attachTo(self.select('propertiesSelector'), {
                    data: self.attr.data
                });

                self.updateEntityAndArtifactDraggables();

                $.when(
                    self.handleCancelling(self.ontologyService.relationships()),
                    self.handleCancelling(self.service.getVertexRelationships(self.attr.data.id))
                ).done(self.loadRelationships.bind(self, vertex));
            });
        };

        this.loadRelationships = function(vertex, ontologyRelationships, vertexRelationships) {
            var self = this,
                totalReferences = vertexRelationships[0].totalReferences,
                relationships = vertexRelationships[0].relationships;

            // Create source/dest/other properties
            relationships.forEach(function(r) {
                var src, dest, other;
                if (vertex.id == r.relationship.sourceVertexId) {
                    src = vertex;
                    dest = other = r.vertex;
                } else {
                    src = other = r.vertex;
                    dest = vertex;
                }

                r.vertices = {
                    src: src,
                    dest: dest,
                    other: other,
                    classes: {
                        src: self.classesForVertex(src),
                        dest: self.classesForVertex(dest),
                        other: self.classesForVertex(other)
                    }
                };

                var id = r.relationship.sourceVertexId + '>' +
                        r.relationship.destVertexId + '|' + 
                        r.relationship.label;
                r.relationshipInfo = {
                    id: id,
                    properties: $.extend({}, r.relationship.properties, {
                        _type: 'relationship',
                        _rowKey: r.relationship.sourceVertexId + '->' + r.relationship.destVertexId,
                        id: id,
                        relationshipType: r.relationship.label,
                        source: r.relationship.sourceVertexId,
                        target: r.relationship.destVertexId
                    })
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
                if (r.relationship.sourceVertexId === vertex.id) {
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
                    return r.vertices.other;
                }),
                infiniteScrolling: (groupedByType.references && groupedByType.references.length) > 0,
                total: totalReferences
            });
        };

        this.handleReferenceLoadingRequest = function(evt, data) {
            var self = this;

            this.handleCancelling(this.service.getVertexRelationships(this.attr.data.id, data.paging))
                .done(function(response) {
                    var relationships = response.relationships,
                        total = response.totalReferences;

                    self.trigger(
                        self.select('relationshipsSelector').find('.references'),
                        'addInfiniteVertices', 
                        { 
                            vertices: _.pluck(relationships, 'vertex'),
                            total: total
                        }
                    );
                    
                });
        };

        this.onPaneClicked = function(evt) {
            var $target = $(evt.target);

            if (!$target.is('.add-new-properties') && $target.parents('.underneath').length === 0) {
                PropertyForm.teardownAll();
            }

            if ($target.is('.entity, .artifact')) {
                var id = $target.data('vertexId');
                this.trigger('selectObjects', { vertices:[appData.vertex(id)] });
                evt.stopPropagation();
            } else if ($target.is('.relationship')) {
                var info = $target.data('info');
                this.trigger('selectObjects', { vertices:[info] });
                evt.stopPropagation();
            }
        };

    }
});

