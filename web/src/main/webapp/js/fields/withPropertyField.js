

define([], function() {
    'use strict';

    return function() {

        this.defaultAttrs({
            predicateSelector: 'select',
            visibleInputsSelector: 'input:visible',
            inputSelector: 'input,select',
            value: ''
        });

        this.after('teardown', function() {
            this.$node.empty();
        });

        this.filterUpdated = function(values, predicate) {
            values = $.isArray(values) ? values : [values];

            if (
                (!this._previousValues || 
                    (this._previousValues && !_.isEqual(this._previousValues, values))) ||

                (!this._previousPredicate || 
                     (this._previousPredicate && !_.isEqual(this._previousPredicate, predicate)))
            ) {

                this.trigger('propertychange', {
                    id: this.attr.id,
                    propertyId: this.attr.property.id,
                    values: values,
                    predicate: predicate
                });
            }

            this._previousValues = values;
            this._previousPredicate = predicate;
        };

        this.getValues = function() {
            return this.select('visibleInputsSelector').map(function() {
                return $(this).val();
            }).toArray();
        };

        this.updateRangeVisibility = function() {
            var v = this.select('predicateSelector').val();

            this.$node.find('.range-only').toggle(v === 'range');
        };

    };
});
