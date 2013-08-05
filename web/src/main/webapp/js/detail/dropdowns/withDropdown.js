
define(['underscore'], function(_) {

    function withDropdown() {

        this.open = function() {
            var self = this,
                node = this.$node;

            node.one('transitionend', function() {
                node.css({
                    transition: 'none',
                    height:'auto',
                    overflow: 'visible'
                });
                self.trigger('opened');
            });
            var form = node.find('.form');
            node.css({ height:form.outerHeight(true) + 'px' });
        };

        this.after('teardown', function() {
            this.$node
                .closest('.sentence').removeClass('focused')
                .closest('.text').removeClass('dropdown');

            this.$node.remove();
        });

        this.after('initialize', function() {
            this.$node.closest('.text').addClass('dropdown');
            _.defer(this.open.bind(this));
        });
    }

    return withDropdown;
});

