
define([
    'flight/lib/component',
    'flight/lib/registry',
    'data',
    'tpl!./list',
    'util/previews',
    'util/video/scrubber',
    'util/withWorkspaceData'
], function(defineComponent, registry, appData, template, previews, VideoScrubber, withWorkspaceData) {

    return defineComponent(List, withWorkspaceData);

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
            this.getWorkspaceVertices().forEach(function(v) {
                _currentVertices[v.id || v.id] = self.stateForVertex(v);
            });
        };

        this.after('initialize', function() {
            this.loadCurrentVertices();

            var classNamesForVertex = {};
            this.attr.vertices.forEach(function(v) {

                // Check if this vertex is in the graph/map
                var classes = ['gId' + encodeURIComponent(v.id)];
                var vertexState = _currentVertices[v.id];
                if (vertexState) {
                    if ( vertexState.inGraph ) classes.push('graph-displayed');
                    if ( vertexState.inMap ) classes.push('map-displayed');
                }

                if (v.properties._subType === 'video' || v.properties._subType === 'image' || v.properties._glyphIcon) {
                    classes.push('has_preview');
                }

                classNamesForVertex[v.id] = classes.join(' ');
            });

            this.$node
                .addClass('vertex-list')
                .html(template({ 
                    vertices:this.attr.vertices,
                    classNamesForVertex: classNamesForVertex
                }));

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
            this.on(document, 'verticesSelected', this.onVerticesSelected);
            this.on(document, 'switchWorkspace', this.onWorkspaceClear);
            this.on(document, 'workspaceDeleted', this.onWorkspaceClear);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
        };

        this.setupDraggables = function() {
            this.applyDraggable(this.$node.find('a'));
            this.$node.droppable({ accept:'*', tolerance:'pointer' });
        };

        this.onHoverItem = function(evt) {
            if (this.disableHover === 'defocused') {
                return;
            } else if (this.disableHover) {
                this.disableHover = 'defocused';
                return this.trigger(document, 'defocusVertices');
            }

            var id = $(evt.target).closest('.vertex-item').data('vertexId');
            if (evt.type == 'mouseenter' && id) {
                this.trigger(document, 'focusVertices', { vertexIds:[id] });
            } else {
                this.trigger(document, 'defocusVertices');
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
                    vertex = appData.vertex(li.data('vertexId'));

                if ((vertex.properties._subType === 'video' || 
                     vertex.properties._subType === 'image' || 
                     vertex.properties._glyphIcon) && !li.data('preview-loaded')) {

                        if (li.data('previewloaded')) return;

                        li.data('previewloaded', true).addClass('preview-loading');

                        if (vertex.properties._glyphIcon) {
                            li.removeClass('preview-loading')
                                .data('preview-loaded', true)
                                .find('.preview').html("<img src='" + vertex.properties._glyphIcon + "' />");
                        } else {
                            previews.generatePreview(vertex.artifact._rowKey, { width: 200 }, function(poster, frames) {
                                li.removeClass('preview-loading')
                                  .data('preview-loaded', true);

                                if(vertex.properties._subType === 'video') {
                                    VideoScrubber.attachTo(li.find('.preview'), {
                                        posterFrameUrl: poster,
                                        videoPreviewImageUrl: frames
                                    });
                                } else if(vertex.properties._subType === 'image') {
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
                otherDraggablesClass: 'vertex-dragging',
                start: function(ev, ui) {
                    $(ui.helper).addClass('vertex-dragging');
                },
                otherDraggables: function(ev, ui){
                    self.trigger(document, 'addVertices', {
                        vertices: appData.vertices(ui.otherDraggables.map(function(){
                            return this.data('original').parent().data('vertexId');
                        }).toArray())
                    });
                },
                selection: function(ev, ui) {
                    var selected = ui.selected,
                        vertices = appData.vertices(selected.map(function() {
                            return $(this).data('vertexId');
                        }).toArray());

                    self.trigger(document, 'defocusVertices');
                    self.trigger('verticesSelected', [vertices]);
                }
            });
        };

        this.onWorkspaceLoaded = function(evt, workspace) {
            this.onVerticesUpdated(evt, workspace.data || {});
        };

        // Track changes to vertices so we display the "Displayed in Graph" icon
        // in search results
        this.toggleItemIcons = function(id, data) {
            this.$node
                .find('li.gId' + encodeURIComponent(id))
                .toggleClass('graph-displayed', data.inGraph)
                .toggleClass('map-displayed', data.inMap);
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
                    _currentVertices[vertex.id] = self.stateForVertex(vertex);
                    self.toggleItemIcons(vertex.id, _currentVertices[vertex.id]);
                }
            });
        };

        this.onVerticesDeleted = function(event, data) {
            var self = this;
            (data.vertices || []).forEach(function(vertex) {
                delete _currentVertices[vertex.id];
                self.toggleItemIcons(vertex.id, { inGraph:false, inMap:false });
            });
        };


        this.onVerticesSelected = function(event, data) {
            if (data && data.remoteEvent) {
                return;
            }
            this.$node.find('.active').removeClass('active');
            $((data||[]).map(function(v) {
                return '.gId' + v.id;
            }).join(',')).addClass('active');
        };
    }
});
