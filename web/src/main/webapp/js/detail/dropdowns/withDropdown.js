
define(['underscore'], function(_) {

    function withDropdown() {

        this.open = function() {
            var self = this,
                vertex = this.$node;

            if (vertex.outerWidth() <= 0) {
                // Fix issue where dropdown is zero width/height 
                // when opening dropdown later in detail pane when
                // dropdown is already open earlier in detail pane
                vertex.css({position:'relative'});
                return _.defer(this.open.bind(this));
            }

            vertex.one('transitionend webkitTransitionEnd oTransitionEnd otransitionend', function() {
                vertex.off('transitionend webkitTransitionEnd oTransitionEnd otransitionend');
                vertex.css({
                    transition: 'none',
                    height: 'auto',
                    width: '100%',
                    overflow: 'visible'
                });
                self.trigger('opened');
            });
            var form = vertex.find('.form');
            vertex.css({ height:form.outerHeight(true) + 'px' });
        };

        this.after('teardown', function() {
            this.$node.closest('.text').removeClass('dropdown');

            this.$node.remove();
        });

        this.after('initialize', function() {
            this.$node.closest('.text').addClass('dropdown');
            _.defer(this.open.bind(this));
        });
    }

    return withDropdown;
});

