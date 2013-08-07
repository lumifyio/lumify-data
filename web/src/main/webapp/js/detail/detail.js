
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
            this.preventDropEventsFromPropagating();
            
            this.$node.html(template({}));
        });


        // Ignore drop events so they don't propagate to the graph/map
        this.preventDropEventsFromPropagating = function() {
            this.$node.droppable({ accept: '.entity' });
        };


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
            if (moduleName != "artifact" && moduleName != "relationship") {
                moduleName = "entity";
            }

            require([
                'detail/' + moduleName + '/' + moduleName,
            ], function(Module) {
                var node = self.select('detailTypeContentSelector');
                (self.typeContentModule = Module).attachTo(node, { data:data });
            });
        };
    }
});
