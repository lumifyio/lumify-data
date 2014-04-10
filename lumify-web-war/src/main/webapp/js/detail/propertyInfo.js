
define([
    'flight/lib/component',
    'tpl!./propertyInfo',
    'service/user',
    'util/formatters'
], function(
    defineComponent,
    template,
    UserService,
    formatters) {
    'use strict';

    return defineComponent(PropertyInfo);

    function PropertyInfo() {

        var userService = new UserService();

        this.defaultAttrs({
            deleteButtonSelector: '.btn-danger',
            editButtonSelector: '.btn-default',
            modifiedBySelector: '.property-modifiedBy'
        });

        this.after('initialize', function() {
            this.$node.html(template({
                property: this.attr.property,
                formatters: formatters
            }));

            this.on('click', {
                deleteButtonSelector: this.onDelete,
                editButtonSelector: this.onEdit
            })

            this.on('propertyerror', this.onPropertyError);
            this.on('willDisplayPropertyInfo', this.onDisplay);
        })

        this.onDisplay = function() {
            var self = this,
                field = this.select('modifiedBySelector'),
                metadata = this.attr.property.metadata,
                user = metadata && metadata['http://lumify.io#modifiedBy'];

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
                property: this.attr.property.key
            });
        };
    }

});
