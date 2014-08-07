define([
    'flight/lib/component'
], function(defineComponent) {
    'use strict';

    var PRIVILEGES = 'EDIT PUBLISH ADMIN'.split(' '),
        Component = defineComponent(Privileges);

    return Component;

    function Privileges() {

        this.workspaceEditable = false;

        this.after('initialize', function() {
            this.workspaceEditable = true;
            this.on('currentUserChanged', this.update);
            this.on('workspaceLoaded', this.onWorkspaceLoaded);
            this.update();
        });

        this.onWorkspaceLoaded = function(event, workspace) {
            this.workspaceEditable = workspace.isEditable;
            this.update();
        };

        this.update = function() {
            var user = window.currentUser,
                editable = this.workspaceEditable,
                cls = [];

            if (user) {
                $.extend(user, {
                    privilegesHelper: _.indexBy(user.privileges || [])
                });
            }
            PRIVILEGES.forEach(function(p) {
                var missingKey = 'missing' + p;

                if (p === 'ADMIN') {
                    Component[missingKey] = !user || !user.privilegesHelper[p];
                } else {
                    Component[missingKey] = !user || !user.privilegesHelper[p] || !editable;
                }
                Component['can' + p] = !Component[missingKey];

                if (Component[missingKey]) {
                    cls.push('no-privilege-' + p);
                }
            });

            $('html').removePrefixedClasses('no-privilege-').addClass(cls.join(' '));
        };

    }
});
