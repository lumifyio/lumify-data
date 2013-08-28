define([
    'flight/lib/component',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./relationship',
    'tpl!./properties',
    'detail/dropdowns/propertyForm/propForm',
    'service/ontology',
    'service/relationship',
    'sf',
    'underscore'
], function(defineComponent, withTypeContent, withHighlighting, template, propertiesTemplate, PropertyForm, OntologyService, RelationshipService, sf, _) {

    'use strict';

    var ontologyService = new OntologyService();
    var relationshipService = new RelationshipService();

    return defineComponent(Relationship, withTypeContent, withHighlighting);

    function Relationship() {

        this.defaultAttrs({
            vertexToVertexRelationshipSelector: '.vertex-to-vertex-relationship',
            addNewPropertiesSelector: '.add-relationship-properties',
            addPropertySelector: '.add-property',
            propertiesSelector: '.properties'
        });

        this.after('teardown', function() {
            this.$node.off('click.paneClick');
        });

        this.after('initialize', function() {
            this.$node.on('click.paneClick', this.onPaneClicked.bind(this));
            this.on('click', {
                vertexToVertexRelationshipSelector: this.onVertexToVertexRelationshipClicked,
                addNewPropertiesSelector: this.onAddNewPropertiesClicked
            });

            this.loadRelationship ();
            this.on('addProperty', this.onAddProperty);
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

            this.trigger (document, 'searchResultSelected',  $target.data('info'));
        };

        this.onPaneClicked = function(evt) {
            var $target = $(evt.target);

            if ($target.not('.add-relationship-properties') && $target.parents('.underneath').length === 0) {
                PropertyForm.teardownAll();
            }

            if ($target.is('.entity, .artifact, span.relationship')) {
                this.trigger(document, 'searchResultSelected', $target.data('info'));
                evt.stopPropagation();
            }
        };

        this.onAddProperty = function(event, data) {
            var self = this;
            relationshipService.setProperty(
                    data.property.name,
                    data.property.value,
                    this.attr.data.source,
                    this.attr.data.target,
                    this.attr.data.relationshipType,
                    function (err, properties){
                        if(err) {
                            if (err.xhr.status == 400){
                                console.error('Validation error');
                                self.trigger(self.$node.find('.underneath'), 'addPropertyError', {});
                                return;
                            }
                            console.error('Error', err);
                            return self.trigger(document, 'error', { message: err.toString() });
                        }
                        self.displayProperties (properties);
                        PropertyForm.teardownAll();
                    }
            );
        };

        this.onAddNewPropertiesClicked = function (evt){
            var root = $('<div class="underneath">').insertAfter(evt.target);

            PropertyForm.attachTo(root, {
                service: ontologyService,
                data: this.attr.data
            });
        };

        this.displayProperties = function (properties){
            var self = this;
            ontologyService.properties(function(err, ontologyProperties) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                var propertiesTpl = [];
                properties.forEach(function(property) {
                    var displayName, value,
                        ontologyProperty = ontologyProperties.byTitle[property.key];

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

                    if(/^[^_]/.test(property.key)) {
                        propertiesTpl.push(data);
                    }

                    if(property.key === '_glyphIcon') {
                        self.trigger(self.select('glyphIconSelector'), 'iconUpdated', { src: property.value });
                    }
                });

                self.select('propertiesSelector').html(propertiesTemplate({properties: propertiesTpl}));
            });
        };
    }
});











