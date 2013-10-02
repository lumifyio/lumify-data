define([
    'flight/lib/component',
    '../withDropdown',
    'tpl!./propForm',
    'tpl!./options',
    'service/ontology'
], function (
    defineComponent,
    withDropdown,
    template,
    options,
    OntologyService) {
    'use strict';

    return defineComponent(PropertyForm, withDropdown);

    function PropertyForm() {

        this.ontologyService = new OntologyService();

        this.defaultAttrs({
            propertySelector: 'select',
            addPropertySelector: '.add-property',
            buttonDivSelector: '.buttons',
            configurationSelector: '.configuration'
        });

        this.after('initialize', function () {
            var self = this,
                vertex = this.attr.data;

            this.on('click', {
                addPropertySelector: this.onAddPropertyClicked
            });

            this.on('addPropertyError', this.onAddPropertyError);
            this.on('propertychange', this.onPropertyChange);

            this.on('change', {
                propertySelector: this.onConceptChanged
            });

            this.$node.html(template({}));

            self.select('addPropertySelector').attr('disabled', true);

            (vertex.properties._subType ?
                self.attr.service.propertiesByConceptId(vertex.properties._subType) :
                self.attr.service.propertiesByRelationshipLabel(vertex.properties.relationshipLabel)
            ).done(function (properties) {
                var propertiesList = [];

                properties.list.forEach(function (property) {
                    if (property.title.charAt(0) !== '_') {
                        var data = {
                            title: property.title,
                            displayName: property.displayName
                        };
                        propertiesList.push(data);
                    }
                });
                
                propertiesList.sort(function(pa, pb) {
                    var a = pa.title, b = pb.title;
                    if (a === 'startDate' && b === 'endDate') return -1;
                    if (b === 'startDate' && a === 'endDate') return 1;
                    if (a === b) return 0;
                    return a < b ? -1 : 1;
                });

                self.select('propertySelector').html(options({
                    properties: propertiesList || ''
                }));
            });
        });

        this.onInputKeyUp = function (event) {
            if (!this.select('addPropertySelector').is(":disabled")) {
                switch (event.which) {
                    case $.ui.keyCode.ENTER:
                        this.onAddPropertyClicked(event);
                }
            }
        };

        this.onConceptChanged = function (event) {
            var self = this,
                propertyName = this.select('propertySelector').val(),
                config = self.select('configurationSelector');

            config.teardownAllComponents();

            if (propertyName) {
                var previousValue = this.attr.data.properties[propertyName];
                this.currentValue = previousValue;
                if (this.currentValue && this.currentValue.latitude) {
                    this.currentValue = 'point(' + this.currentValue.latitude + ',' + this.currentValue.longitude + ')';
                }

                this.select('addPropertySelector').html((previousValue ? 'Update' : 'Add') + ' Property')
                                                  .removeAttr('disabled');

                this.ontologyService.properties().done(function(properties) {
                    var propertyDetails = properties.byTitle[propertyName];
                    if (propertyDetails) {
                        require(['fields/' + propertyDetails.dataType], function(PropertyField) {
                            PropertyField.attachTo(config, {
                                property: propertyDetails,
                                value: previousValue,
                                predicates: false
                            });
                        });
                    } else console.warn('Property ' + propertyName + ' not found in ontology');
                });
            } else {
                this.select('addPropertySelector').attr('disabled', true);
            }
        };

        this.onPropertyChange = function (event, data) {
            event.stopPropagation();

            if (data.values.length === 1) {
                this.currentValue = data.values[0];
            } else if (data.values.length > 1) {
                // Must be geoLocation
                this.currentValue = 'point(' + data.values.join(',') + ')';
            }
        };

        this.onAddPropertyError = function (event) {
            this.$node.find('input').addClass('validation-error');
            _.defer(this.clearLoading.bind(this));
        };

        this.onAddPropertyClicked = function (evt) {
            var vertexId = this.attr.data.id,
                propertyName = this.select('propertySelector').val(),
                value = this.currentValue;

            _.defer(this.buttonLoading.bind(this));

            this.$node.find('input').removeClass('validation-error');
            if (propertyName.length && value.length) {
                this.trigger('addProperty', {
                    property: {
                        name: propertyName,
                        value: value
                    }
                });
            }
        };
    }
});
