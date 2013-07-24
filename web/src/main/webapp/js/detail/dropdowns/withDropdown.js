
define(['underscore'], function(_) {

    function withDropdown() {

        this.open = function() {
            var node = this.$node;

            node.one('transitionend', function() {
                node.css({
                    transition: 'none',
                    height:'auto',
                    overflow: 'visible'
                });
            });
            var form = node.find('.form');
            node.css({ height:form.outerHeight(true) + 'px' });
        };

        this.after('teardown', function() {
            this.$node
                .parents('.sentence').removeClass('focused')
                .parents('.text').removeClass('focus');

            this.$node.remove();
        });

        this.after('initialize', function() {
            _.defer(this.open.bind(this));
        });
    }

    return withDropdown;
});

