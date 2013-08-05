
define([
    'flight/lib/component',
    'underscore',
    'tpl!./detail'
], function( defineComponent, _, template) {

    'use strict';

    return defineComponent(Detail);

    function Detail() {


        this.defaultAttrs({
            mapCoordinatesSelector: '.map-coordinates',
            detailTypeContentSelector: '.type-content'
        });

        this.after('initialize', function() {
            this.on('click', {
                mapCoordinatesSelector: this.onMapCoordinatesClicked
            });

            this.on(document, 'searchResultSelected', this.onSearchResultSelected);
            
            this.$node.html(template({}));
        });


        this.onMapCoordinatesClicked = function(evt, data) {
            evt.preventDefault();
            var $target = $(evt.target);
            data = {
                latitude: $target.attr('latitude'),
                longitude: $target.attr('longitude')
            };
            this.trigger('mapCenter', data);
        };


        this.onSearchResultSelected = function(evt, data) {
            if ($.isArray(data) && data.length === 1) {
                data = data[0];
            }

            if (this.typeContentModule) {
                this.typeContentModule.teardownAll();
            }

            if ( !data || data.length === 0 ) {
                return;
            }

            var self = this,
                moduleName = $.isArray(data) ? 'multiple' : data.type;

            // Attach specific module based on the object selected
            require([
                'detail/' + moduleName + '/' + moduleName,
            ], function(Module) {
                var node = self.select('detailTypeContentSelector');
                (self.typeContentModule = Module).attachTo(node, { data:data });
            });
        };
    }
});
