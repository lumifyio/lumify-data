define([
    'flight/lib/component',
    '../withDropdown',
    'tpl!./propForm',
    'tpl!./options'
], function (defineComponent, withDropdown, template, options) {
    'use strict';

    return defineComponent(PropertyForm, withDropdown);

    function PropertyForm() {

        this.defaultAttrs({
            propertySelector: 'select',
            propertyValueSelector: '.property-value',
            addPropertySelector: '.add-property',
            buttonDivSelector: '.buttons'
        });

        this.after('initialize', function () {
            var self = this,
                vertex = this.attr.data;

            this.on('click', {
                addPropertySelector: this.onAddPropertyClicked
            });

            this.on('keyup', {
                propertyValueSelector: this.onInputKeyUp
            });

            this.on('addPropertyError', this.onAddPropertyError);

            this.on('change', {
                propertySelector: this.onConceptChanged
            });

            this.$node.html(template({}));

            self.select('addPropertySelector').attr('disabled', true);

            if (vertex.properties._subType) {
                self.attr.service.propertiesByConceptId(vertex.properties._subType)
                    .done(function (properties) {
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

                        self.select('propertySelector').html(options({
                            properties: propertiesList || ''
                        }));
                    });
            } else {
                self.attr.service.propertiesByRelationshipLabel(vertex.properties.relationshipLabel)
                    .done(function (properties) {
                        var propertiesList = [];

                        properties.list.forEach(function (property) {
                            if (property.title.charAt(0) != '_') {
                                var data = {
                                    title: property.title,
                                    displayName: property.displayName
                                };
                                propertiesList.push(data);
                            }
                        });

                        self.select('propertySelector').html(options({
                            properties: propertiesList || ''
                        }));
                    });
            }

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
            var propertyName = this.select('propertySelector').val();
            if (propertyName != '') {
                var previousValue = this.attr.data.properties[propertyName];
                if(previousValue) {
                    if(previousValue.latitude) {
                        previousValue = 'point(' + previousValue.latitude + ',' + previousValue.longitude + ')';
                    }
                    this.select('addPropertySelector').html('Update Property');
                    this.select('propertyValueSelector').val(previousValue);
                } else {
                    this.select('addPropertySelector').html('Add Property');
                    this.select('propertyValueSelector').val('');
                }
                this.select('addPropertySelector').attr('disabled', false);
            } else if (propertyName == '') {
                this.select('addPropertySelector').attr('disabled', true);
            }
        };

        this.onAddPropertyError = function (event) {
            this.select('propertyValueSelector').addClass('validation-error');
            _.defer(this.clearLoading.bind(this));
        };

        this.onAddPropertyClicked = function (evt) {
            var vertexId = this.attr.data.id,
                propertyName = this.select('propertySelector').val(),
                value = $.trim(this.select('propertyValueSelector').val());

            _.defer(this.buttonLoading.bind(this));

            this.select('propertyValueSelector').removeClass('validation-error');
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
