
define([
    'flight/lib/component',
    'tpl!./date',
    './withFilter'
], function(defineComponent, template, withFilter) {
    'use strict';

    return defineComponent(DateFilter, withFilter);

    function DateFilter() {

        this.defaultAttrs({
        });

        this.after('initialize', function() {
            this.$node.html(template({}));

            this.updateRangeVisibility();

            this.on('change keyup', {
                inputSelector: function() {

                    this.updateRangeVisibility();

                    if (this.isValid()) {
                        this.filterUpdated(
                            this.getValues(),
                            this.select('predicateSelector').val()
                        );
                    }
                }
            });
        });

        this.isValid = function() {
            return _.every(this.getValues(), function(v) {
                return (/^\s*\d{4}-\d{1,2}-\d{1,2}\s*$/).test(v);
            });
        };
    }
});

