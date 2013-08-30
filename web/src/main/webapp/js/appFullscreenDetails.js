

define([
    'flight/lib/component',
    'service/vertex',
    'service/ucd',
    'detail/detail',
    'tpl!appFullscreenDetails',
    'underscore'
], function(defineComponent, VertexService, UCD, Detail, template, _) {

    return defineComponent(FullscreenDetails);


    function FullscreenDetails() {

        this.vertexService = new VertexService();
        this.ucd = new UCD();

        this.defaultAttrs({
            detailSelector: '.detail-pane .content'
        });

        this.after('initialize', function() {

            this.$node.addClass('fullscreen-details');

            this.vertexService
                .getMultiple(this.attr.graphVertexIds)
                .done(this.handleVerticesLoaded.bind(this));
        
        });

        this.handleVerticesLoaded = function(vertices) {
            if (vertices.length === 1) {
                var vertex = vertices[0];

                if (vertex.properties._type === 'artifact' && vertex.properties._subType === 'document') {
                    this.ucd
                        .getArtifactById(vertex.properties._rowKey)
                        .done(function(a) {
                            document.title = a.Generic_Metadata.subject;
                        });
                } else document.title = vertex.properties.title;

            } else {
                document.title = vertices.length + ' item' + (vertices.length > 1 ? 's' : '');
            }

            vertices.forEach(function(v) {
                v.properties.graphVertexId = v.id;

                this.$node.append(template({}));
                Detail.attachTo(this.$node.find('.detail-pane').last().find('.content'), {
                    loadGraphVertexData: v.properties,
                    highlightStyle: 0
                });
            }.bind(this));
        };
    }
});

