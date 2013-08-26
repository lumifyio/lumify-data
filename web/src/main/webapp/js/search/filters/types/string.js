
define([
    'flight/lib/component',
    'tpl!./string',
    './withFilter'
], function(defineComponent, template, withFilter) {

    return defineComponent(StringFilter, withFilter);

    function StringFilter() {

        this.after('initialize', function() {
            var self = this;

            this.$node.html(template({}));

            this.select('inputSelector').focus();

            this.on('change keyup', {
                inputSelector: function(event) { 
                    var val = $.trim($(event.target).val());

                    this.filterUpdated(val);
                }
            });
        });
    }
});
