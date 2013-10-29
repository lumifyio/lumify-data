
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
            this.on('selectVertices', this.onSelectVerticesWithinContents);
            this.preventDropEventsFromPropagating();

            this.before('teardown',this.teardownComponents);
            
            this.$node.html(template({}));

            if (this.attr.loadGraphVertexData) {
                this.onVerticesSelected(null, { vertices:[this.attr.loadGraphVertexData] });
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

        this.onSelectVerticesWithinContents = function(evt, data) {
            evt.stopPropagation();
            var vertices = data && data.vertices || { vertices:[] };
            this.onVerticesSelected(evt, { vertices:vertices });
        };

        this.onVerticesSelected = function(evt, data) {
            var vertices = data.vertices;

            this.teardownComponents();

            var typeContentNode = this.select('detailTypeContentSelector');
            if ( vertices.length === 0 ) {
                return;
            }

            var self = this,
                moduleData = vertices.length > 1 ? vertices : vertices[0],
                moduleName = (
                    (vertices.length > 1 ? 'multiple' :
                        (moduleData.properties._type != 'artifact' && moduleData.properties._type != 'relationship') ? 'entity' :
                        moduleData.properties._type) || 'entity'
                ).toLowerCase();

            require([
                'detail/' + moduleName + '/' + moduleName,
            ], function(Module) {
                Module.attachTo(typeContentNode, { 
                    data: moduleData,
                    highlightStyle: self.attr.highlightStyle
                });
            });
        };

        this.teardownComponents = function () {
            var typeContentNode = this.select('detailTypeContentSelector'),
                instanceInfos = registry.findInstanceInfoByNode(typeContentNode[0]);
            if (instanceInfos.length) {
                instanceInfos.forEach(function(info) {
                    info.instance.teardown();
                });
            }
        }
    }
});
