

define([
    'flight/lib/component',
    'tpl!./menubar'
], function(defineComponent, template) {
    'use strict';

    return defineComponent(Menubar);

    function Menubar() {

        // Add class name of <li> buttons here
        var BUTTONS = 'search users metrics prefs';

        var attrs = {}, events = {};
        BUTTONS.split(' ').forEach(function(name) {
            var sel = name + 'IconSelector';

            attrs[sel] = '.' + name;
            events[sel] = function(e) {
                e.preventDefault();
                this.trigger(document, 'menubarToggleDisplay', {name:name});
            };
        });

        this.defaultAttrs(attrs);
        
        this.after('initialize', function() {
            this.$node.html(template({}));

            this.on('click', events);

            this.on(document, 'menubarToggleDisplay', function(e, data) {
                this.select(data.name + 'IconSelector').toggleClass('active');
            });
        });

    }
});
