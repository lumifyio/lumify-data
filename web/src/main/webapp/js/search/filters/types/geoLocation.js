
define([
    'flight/lib/component',
    'tpl!./geoLocation',
    './withFilter',
    'underscore'
], function(defineComponent, template, withFilter, _) {

    return defineComponent(GeoLocationFilter, withFilter);

    function makeNumber(v) {
        return parseFloat(v, 10);
    }

    function GeoLocationFilter() {

        this.after('initialize', function() {
            this.$node.html(template({}));

            this.on('change keyup', {
                inputSelector: function(event) {
                    if (this.isValid()) {
                        this.filterUpdated(this.getValues().map(function(v) {
                            return makeNumber(v);
                        }));
                    }
                }
            });
        });

        this.isValid = function() {
            return _.every(this.getValues(), function(v) {
                return v.length && _.isNumber(makeNumber(v));
            });
        };
    }
});
