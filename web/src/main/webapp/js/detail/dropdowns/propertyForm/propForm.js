
define([
    'flight/lib/component',
    '../withDropdown',
    'tpl!./propForm',
    'tpl!./options'
], function(defineComponent, withDropdown, template, options) {

    return defineComponent(PropertyForm, withDropdown);

    function PropertyForm() {

        this.defaultAttrs({
            propertySelector: 'select',
            propertyValueSelector: '.property-value',
            addPropertySelector: '.add-property'
        });

        this.after('initialize', function() {

            this.on('click', {
                addPropertySelector: this.onAddPropertyClicked
            });
            this.on('addPropertyError', this.onAddPropertyError);

            this.$node.html(template({}));

            var self = this;

            self.attr.service.propertiesByConceptId(self.attr.data._subType, function (err, properties){
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                var propertiesList = [];

                properties.list.forEach (function (property){
                    if (property.title.charAt(0) != '_'){
                        var data = {
                            title: property.title,
                            displayName: property.displayName
                        };
                        propertiesList.push (data);
                    }
                });

                self.select('propertySelector').html(options({
                    properties: propertiesList || ''
                }));
            });

        });

        this.onAddPropertyError = function(event) {
            this.select('propertyValueSelector').addClass('validation-error');
        };

        this.onAddPropertyClicked = function (evt){
            var vertexId = this.attr.data.id,
                propertyName = this.select('propertySelector').val(),
                value = $.trim(this.select('propertyValueSelector').val());

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
