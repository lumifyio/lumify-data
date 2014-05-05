
define([
    'flight/lib/component',
    'tpl!./propertyInfo',
    'service/user',
    'util/formatters',
    'data'
], function(
    defineComponent,
    template,
    UserService,
    F,
    appData) {
    'use strict';

    return defineComponent(PropertyInfo);

    function PropertyInfo() {

        var userService = new UserService();

        this.defaultAttrs({
            deleteButtonSelector: '.btn-danger',
            editButtonSelector: '.btn-default',
            modifiedBySelector: '.property-modifiedBy',
            justificationValueSelector: '.justificationValue'
        });

        this.after('initialize', function() {
            this.$node.html(template({
                property: this.attr.property,
                F: F
            }));

            this.on('click', {
                deleteButtonSelector: this.onDelete,
                editButtonSelector: this.onEdit,
            })

            this.on('propertyerror', this.onPropertyError);
            this.on('willDisplayPropertyInfo', this.onDisplay);
        });

        this.updateJustification = function() {
            var self = this,
                metadata = this.attr.property.metadata,
                justificationMetadata = metadata && metadata._justificationMetadata,
                sourceMetadata = metadata && metadata._sourceMetadata;

            if (justificationMetadata || sourceMetadata) {
                require(['util/vertex/justification/viewer'], function(JustificationViewer) {
                    JustificationViewer.attachTo(self.select('justificationValueSelector'), {
                        justificationMetadata: justificationMetadata,
                        sourceMetadata: sourceMetadata
                    });

                    self.trigger('positionPropertyInfo');
                });
            }
        };

        this.onDisplay = function() {
            var self = this,
                field = this.select('modifiedBySelector'),
                metadata = this.attr.property.metadata,
                user = metadata && metadata['http://lumify.io#modifiedBy'];

            this.updateJustification();

            if (this.userLoaded) {
                return;
            }

            if (user) {
                userService.userInfo(user)
                    .fail(function() {
                        field.text('Unknown');
                    })
                    .done(function(user) {
                        self.userLoaded = true;
                        field.text(user.userName);
                    })
            } else {
                field.text('Unknown');
            }
        };

        this.onPropertyError = function(event, data) {
            var button = this.select('deleteButtonSelector').removeClass('loading'),
                text = button.text();

            button.text(data.error || 'Unknown Error')
            _.delay(function() {
                button.text(text).removeAttr('disabled');
            }, 3000)
        };

        this.onEdit = function() {
            this.trigger('editProperty', {
                property: this.attr.property
            });
        };

        this.onDelete = function(e) {
            var button = this.select('deleteButtonSelector').addClass('loading').attr('disabled', true);
            this.trigger('deleteProperty', {
                property: _.pick(this.attr.property, 'name', 'key')
            });
        };
    }

});
