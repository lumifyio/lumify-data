
define([
    'flight/lib/component',
    'flight/lib/registry',
    'tpl!./list',
    'util/previews',
    'util/video/scrubber',
    'underscore'
], function(defineComponent, registry, template, previews, VideoScrubber, _) {

    return defineComponent(List);

    function List() {
        var _currentVertices = {};

        this.defaultAttrs({
            itemSelector: '.vertex-item'
        });

        this.stateForVertex = function(vertex) {
            return {
                inGraph: true,
                inMap: !!(vertex.location || (vertex.locations && vertex.locations.length))
            };
        };

        this.loadCurrentVertices = function() {
            var self = this;
            (registry.findInstanceInfoByNode($('#app')[0])[0].instance.workspaceData.data.vertices || []).forEach(function(v) {
                _currentVertices[v.id || v.graphVertexId] = self.stateForVertex(v);
            });
        };

        this.after('initialize', function() {
            this.loadCurrentVertices();

            var vertices = this.attr.vertices.map(function(v) {
                v = $.extend({ }, v, v.properties || {});
                delete v.properties;

                // Bad attempt to merge artifact results and entity results
                v.graphVertexId = v.id || v.graphVertexId;
                v.title = v.title || v.subject;
                v.url = v.url || '#';

                // Check if this vertex is in the graph/map
                var classes = ['gId' + encodeURIComponent(v.graphVertexId)];
                var vertexState = _currentVertices[v.graphVertexId];
                if (vertexState) {
                    if ( vertexState.inGraph ) classes.push('graph-displayed');
                    if ( vertexState.inMap ) classes.push('map-displayed');
                }
                if (v._subType === 'video' || v._subType === 'image' || v._glyphIcon) {
                    classes.push('has_preview');
                }

                v.className = classes.join(' ');
                return v;
            });

            this.$node
                .addClass('vertex-list')
                .html(template({ vertices: vertices }));

            this.attachEvents();

            this.loadVisibleResultPreviews = _.debounce(this.loadVisibleResultPreviews.bind(this), 1000);
            this.loadVisibleResultPreviews();

            this.setupDraggables();
        });

        this.after('teardown', function() {
            this.$node.off('mouseenter mouseleave');
            this.scrollNode.off('scroll.vertexList');
            this.$node.empty();
        });

        this.attachEvents = function() {
            this.scrollNode = this.$node.parents().filter(function(i, n) { 
                    return $(n).css('overflow') === 'auto'; 
                }).eq(0)
                  .on('scroll.vertexList', this.onResultsScroll.bind(this));

            this.$node.on('mouseenter mouseleave', '.vertex-item', this.onHoverItem.bind(this));

            this.on(document, 'verticesAdded', this.onVerticesUpdated);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'switchWorkspace', this.onWorkspaceClear);
            this.on(document, 'workspaceDeleted', this.onWorkspaceClear);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
        };

        this.setupDraggables = function() {
            this.applyDraggable(this.$node.find('a'));
            this.$node.droppable({ accept:'a' });
        };

        this.onHoverItem = function(evt) {
            if (this.disableHover === 'defocused') {
                return;
            } else if (this.disableHover) {
                this.disableHover = 'defocused';
                return this.trigger(document, 'defocusVertices', { vertexIds:[] });
            }

            var info = $(evt.target).closest('.vertex-item').data('info');
            if (evt.type == 'mouseenter' && info && info.graphVertexId) {
                this.trigger(document, 'focusVertices', { vertexIds:[info.graphVertexId] });
            } else {
                this.trigger(document, 'defocusVertices', { vertexIds:[] });
            }
        };

        this.onResultsScroll = function(e) {
            if (!this.disableHover) {
                this.disableHover = true;
            }

            this.loadVisibleResultPreviews();
        };

        this.loadVisibleResultPreviews = function() {
            var self = this;

            this.disableHover = false;
            if ( !this.previewQueue ) {
                this.previewQueue = previews.createQueue('vertexList', { maxConcurrent: 1 });
            }

            var lisVisible = this.$node.find('.nav-list').children('li');
            if (this.scrollNode.length) {
                lisVisible = lisVisible.withinScrollable(this.scrollNode);
            }
            
            lisVisible.each(function() {
                var li = $(this),
                    info = li.data('info'),
                    _rowKey = info._rowKey;

                if ((info._subType === 'video' || info._subType === 'image' || info._glyphIcon) && !li.data('preview-loaded')) {
                    if (li.data('previewloaded')) return;

                    li.data('previewloaded', true).addClass('preview-loading');

                    if (info._glyphIcon) {
                        li.removeClass('preview-loading')
                            .data('preview-loaded', true)
                            .find('.preview').html("<img src='" + info._glyphIcon + "' />");
                    } else {
                        previews.generatePreview(_rowKey, null, function(poster, frames) {
                            li.removeClass('preview-loading')
                              .data('preview-loaded', true);

                            if(info._subType === 'video') {
                                VideoScrubber.attachTo(li.find('.preview'), {
                                    posterFrameUrl: poster,
                                    videoPreviewImageUrl: frames
                                });
                            } else if(info._subType === 'image') {
                                li.find('.preview').html("<img src='" + poster + "' />");
                            }
                        });
                    }
                }
            });
        };

        this.applyDraggable = function(el) {
            var self = this;

            el.draggable({
                helper:'clone',
                appendTo: 'body',
                revert: 'invalid',
                revertDuration: 250,
                scroll: false,
                zIndex: 100,
                distance: 10,
                multi: true,
                otherDraggablesClass: 'search-result-dragging',
                start: function(ev, ui) {
                    $(ui.helper).addClass('search-result-dragging');
                },
                otherDraggables: function(ev, ui){

                    ui.otherDraggables.each(function(){
                        var info = this.data('original').parent().data('info'),
                            offset = this.offset(),
                            dropPosition = { x:offset.left, y:offset.top };

                        self.trigger(document, 'addVertices', {
                            vertices: [{
                                title: info.title,
                                graphVertexId: info.graphVertexId,
                                _rowKey: info._rowKey,
                                _subType: info._subType,
                                _type: info._type,
                                dropPosition: dropPosition
                            }]
                        });
                    });
                },
                selection: function(ev, ui) {
                    var selected = ui.selected,
                        info = selected.map(function() {
                            return $(this).data('info');
                        }).toArray();

                    self.trigger(document, 'defocusVertices', { vertexIds:[] });
                    self.trigger('searchResultSelected', [info]);
                }
            });
        };

        this.onWorkspaceLoaded = function(evt, workspace) {
            this.onVerticesUpdated(evt, workspace.data || {});
        };

        // Track changes to vertices so we display the "Displayed in Graph" icon
        // in search results
        this.toggleSearchResultIcon = function(graphVertexId, inGraph, inMap) {
            this.$node
                .find('li.gId' + encodeURIComponent(graphVertexId))
                .toggleClass('graph-displayed', inGraph)
                .toggleClass('map-displayed', inMap);
        };

        // Switching workspaces should clear the icon state and vertices
        this.onWorkspaceClear = function() {
            this.$node.find('li.graph-displayed').removeClass('graph-displayed');
            this.$node.find('li.map-displayed').removeClass('map-displayed');
            _currentVertices = {};
        };

        this.onVerticesUpdated = function(event, data) {
            var self = this;
            (data.vertices || []).forEach(function(vertex) {
                // Only care about vertex search results and location updates
                if ( (vertex._type && vertex._subType) || vertex.location || vertex.locations ) {
                    _currentVertices[vertex.graphVertexId] = self.stateForVertex(vertex);
                    self.toggleSearchResultIcon(vertex.graphVertexId, _currentVertices[vertex.graphVertexId]);
                }
            });
        };

        this.onVerticesDeleted = function(event, data) {
            var self = this;
            (data.vertices || []).forEach(function(vertex) {
                delete _currentVertices[vertex.graphVertexId];
                self.toggleSearchResultIcon(vertex.graphVertexId, { inGraph:false, inMap:false });
            });
        };

    }
});
