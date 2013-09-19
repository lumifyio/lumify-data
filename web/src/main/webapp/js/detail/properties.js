
define([
    'flight/lib/component',
    'service/ontology',
    'service/vertex',
    './dropdowns/propertyForm/propForm',
    'tpl!./properties',
], function (defineComponent, OntologyService, VertexService, PropertyForm, propertiesTemplate) {
    'use strict';

    Properties.filterPropertiesForDisplay = filterPropertiesForDisplay;

    return defineComponent(Properties);

    function Properties() {

        this.ontologyService = new OntologyService();
        this.vertexService = new VertexService();

        this.defaultAttrs({
            addNewPropertiesSelector: '.add-new-properties'
        });

        this.after('initialize', function () {
            this.on('click', {
                addNewPropertiesSelector: this.onAddNewPropertiesClicked
            });
            this.on('addProperty', this.onAddProperty);
            this.on(document, 'socketMessage', this.onSocketMessage);

            this.getProperties(this.attr.data.id || this.attr.data.graphVertexId)
                .done(this.displayProperties.bind(this));
        });


        this.onAddProperty = function (event, data) {
            var self = this;

            self.vertexService.setProperty(
                this.attr.data.id || this.attr.data.graphVertexId,
                data.property.name,
                data.property.value)
                .fail(function(err) {
                    // TODO: check if correct
                    if (err.xhr.status == 400) {
                        console.error('Validation error');
                        self.trigger(self.$node.find('.underneath'), 'addPropertyError', {});
                        return;
                    }
                })
                .done(function(vertexData) {
                    self.displayProperties(vertexData.properties);
                    self.trigger (document, "updateVertices", { vertices: [vertexData.vertex] });
                }
            );
        };

        this.onAddNewPropertiesClicked = function (evt) {
            var root = $('<div class="underneath">').insertAfter(evt.target);

            PropertyForm.attachTo(root, {
                service: this.ontologyService,
                data: this.attr.data
            });
        };

        this.onSocketMessage = function (evt, message) {
            var self = this;
            switch (message.type) {
                case 'propertiesChange':
                    for(var i=0; i<message.data.properties.length; i++) {
                        var propertyChangeData = message.data.properties[i];
                        self.onPropertyChange(propertyChangeData);
                    }
                    break;
            }
        };

        this.onPropertyChange = function (propertyChangeData) {
            if (propertyChangeData.graphVertexId != this.attr.data.graphVertexId) {
                return;
            }
            if(propertyChangeData.propertyName == 'title') {
                this.select('titleSelector').html(propertyChangeData.value);
            }
            this.select('propertiesSelector')
                .find('.property-' + propertyChangeData.propertyName + ' .value')
                .html(propertyChangeData.value);
        };

        this.displayProperties = function (properties){
            var self = this;

            if (!this.ontologyProperties) {
                this.ontologyProperties = self.ontologyService.properties();
            }

            this.ontologyProperties
                .done(function(ontologyProperties) {
                    var filtered = filterPropertiesForDisplay(properties, ontologyProperties),
                        iconProperty = _.findWhere(properties, { key: '_glyphIcon' });

                    if (iconProperty) {
                        self.trigger(self.select('glyphIconSelector'), 'iconUpdated', { src: iconProperty.value });
                    }

                    var props = propertiesTemplate({properties:filtered});

                    var $props = self.select('propertiesSelector');
                    $props.find('ul').html(props);
                    $props.find('.loading').remove();
                });
        };
    }

    function filterPropertiesForDisplay(properties, ontologyProperties) {
        var displayProperties = [];

        if ($.isArray(properties)) {
            var o = {};
            properties.forEach(function (p) {
                o[p.key] = p.value;
            });
            properties = o;
        }

        Object.keys(properties).forEach(function (name) {
            var displayName, value,
            ontologyProperty = ontologyProperties.byTitle[name];

        if (ontologyProperty) {
            displayName = ontologyProperty.displayName;
            if (ontologyProperty.dataType == 'date') {
                value = sf("{0:yyyy/MM/dd}", new Date(properties[name]));
            } else {
                value = properties[name];
            }
        } else {
            displayName = name;
            value = properties[name];
        }

        var data = {
            key: name,
        value: value,
        displayName: displayName
        };

        if (/^[^_]/.test(name)) {
            displayProperties.push(data);
        }
        });
        return displayProperties;
    }
});

