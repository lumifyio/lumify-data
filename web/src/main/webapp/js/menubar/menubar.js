

define([
    'flight/lib/component',
    'tpl!./menubar'
], function(defineComponent, template) {
    'use strict';

    return defineComponent(Menubar);

    function Menubar() {

        // Add class name of <li> buttons here
        var BUTTONS = 'search activity users metrics prefs';

        // Don't change state to highlighted on click
        var DISABLE_ACTIVE_SWITCH = 'activity metrics prefs'.split(' ');

        this.activities = 0;

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

            this.select('activityIconSelector').tooltip({ 
                placement: 'right',
                title: 'No activity' 
            });

            this.on(document, 'menubarToggleDisplay', function(e, data) {
                var icon = this.select(data.name + 'IconSelector');

                if (DISABLE_ACTIVE_SWITCH.indexOf(data.name) === -1) {
                    icon.toggleClass('active');
                } else {

                    // Just highlight briefly to show click worked
                    icon.addClass('active');
                    setTimeout(function() {
                        icon.removeClass('active');
                    }, 200);
                }
            });

            this.on(document, 'workspaceSaving', function(e, data) {
                this.updateActivity(true, e.type);
                this.activities++;
            });
            this.on(document, 'workspaceSaved', function(e, data) {
                if (--this.activities === 0) {
                    this.updateActivity(false, e.type);
                }
            });
        });


        this.updateActivity = function(animating, message) {
            var activityIcon = this.select('activityIconSelector');
            activityIcon.attr('data-original-title', message).tooltip('fixTitle');
            activityIcon.toggleClass('animating', animating);
        };

    }
});
