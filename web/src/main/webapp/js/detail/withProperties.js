define([
    'service/ontology',
    'service/vertex',
    'detail/dropdowns/propertyForm/propForm',
    'tpl!./properties',
], function (OntologyService, VertexService, PropertyForm, propertiesTemplate) {

    return withProperties;

    function withProperties() {

        this.ontologyService = new OntologyService();
        this.vertexService = new VertexService();

        this.after('initialize', function () {
            this.on('click', {
                addNewPropertiesSelector: this.onAddNewPropertiesClicked
            });
            this.on('addProperty', this.onAddProperty);
            this.on(document, 'socketMessage', this.onSocketMessage);
        });

        this.filterPropertiesForDisplay = function (properties, ontologyProperties) {
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
        };

        this.onAddProperty = function (event, data) {
            var self = this;

            self.vertexService.setProperty(
                this.attr.data.id || this.attr.data.graphVertexId,
                data.property.name,
                data.property.value,
                function (err, properties) {
                    if (err) {
                        if (err.xhr.status == 400) {
                            console.error('Validation error');
                            self.trigger(self.$node.find('.underneath'), 'addPropertyError', {});
                            return;
                        }
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    self.displayProperties(properties);
                }
            );
        };

        this.onAddNewPropertiesClicked = function (evt) {
            var self = this;
            var root = $('<div class="underneath">').insertAfter(evt.target);

            PropertyForm.attachTo(root, {
                service: self.ontologyService,
                data: this.attr.data
            });
        };

        this.onPropertyChange = function (propertyChangeData) {
            if (propertyChangeData.graphVertexId != this.attr.data.graphVertexId) {
                return;
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

            this.ontologyProperties.done(function(ontologyProperties) {
                var filtered = self.filterPropertiesForDisplay(properties, ontologyProperties),
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
});
