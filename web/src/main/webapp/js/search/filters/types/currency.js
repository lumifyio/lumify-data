
define([
    'flight/lib/component',
    'tpl!./currency',
    './withFilter'
], function(defineComponent, template, withFilter) {

    return defineComponent(CurrencyFilter, withFilter);
       
    function makeNumber(v) {
        return parseFloat(v.replace(/[$,]/g, ''), 10);
    }

    function CurrencyFilter() {

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
                return _.isNumber(makeNumber(v));
            });
        };
    }
});
