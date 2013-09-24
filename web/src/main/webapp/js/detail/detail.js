
define([
    'flight/lib/component',
    'flight/lib/registry',
    'tpl!./detail'
], function( defineComponent, registry, template) {
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

            this.on(document, 'verticesSelected', this.onVerticesSelected);
            this.on('verticesSelected', this.onVerticesSelectedWithinContents);
            this.preventDropEventsFromPropagating();
            
            this.$node.html(template({}));

            if (this.attr.loadGraphVertexData) {
                this.onVerticesSelected(null, [this.attr.loadGraphVertexData]);
            }
        });


        // Ignore drop events so they don't propagate to the graph/map
        this.preventDropEventsFromPropagating = function() {
            this.$node.droppable({ tolerance: 'pointer', accept: '*' });
        };


        this.onMapCoordinatesClicked = function(evt, data) {
            evt.preventDefault();
            var $target = $(evt.target);
            data = {
                latitude: $target.data('latitude'),
                longitude: $target.data('longitude')
            };
            this.trigger('mapCenter', data);
        };

        this.onVerticesSelectedWithinContents = function(evt, data) {
            if (data.remoteEvent) {
                return;
            }
            evt.stopPropagation();
            this.onVerticesSelected(evt, data);
        };

        this.onVerticesSelected = function(evt, data) {
            if (data && data.remoteEvent) {
                return;
            }

            if ($.isArray(data) && data.length === 1) {
                data = data[0];
            }

            var typeContentNode = this.select('detailTypeContentSelector'),
                instanceInfos = registry.findInstanceInfoByNode(typeContentNode[0]);
            if (instanceInfos.length) {
                instanceInfos.forEach(function(info) {
                    info.instance.teardown();
                });
            }

            if ( !data || data.length === 0 ) {
                return;
            }

            var self = this,
                moduleName = (
                    ($.isArray(data) ? 'multiple' :
                        (data.properties._type != 'artifact' && data.properties._type != 'relationship') ? 'entity' : 
                        data.properties._type) || 'entity'
                ).toLowerCase();

            require([
                'detail/' + moduleName + '/' + moduleName,
            ], function(Module) {
                Module.attachTo(typeContentNode, { 
                    data: data,
                    highlightStyle: self.attr.highlightStyle
                });
            });
        };
    }
});
