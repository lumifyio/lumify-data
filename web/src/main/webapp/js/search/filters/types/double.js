
define([
    'flight/lib/component',
    'tpl!./double',
    './withFilter'
], function(defineComponent, template, withFilter) {
    'use strict';

    return defineComponent(DoubleFilter, withFilter);
       
    function makeNumber(v) {
        return parseFloat(v, 10);
    }

    function DoubleFilter() {

        this.after('initialize', function() {
            this.$node.html(template({}));

            this.updateRangeVisibility();

            this.on('change keyup', {
                inputSelector: function() {

                    this.updateRangeVisibility();

                    if (this.isValid()) {
                        this.filterUpdated(
                            this.getValues().map(function(v) {
                                return makeNumber(v); 
                            }),
                            this.select('predicateSelector').val()
                        );
                    }
                }
            });
        });

        this.isValid = function() {
            var values = this.getValues();

            return _.every(values, function(v) {
                return v.length && _.isNumber(makeNumber(v));
            });
        };
    }
});
