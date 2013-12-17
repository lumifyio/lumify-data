
define([
    'flight/lib/component',
    'flight/lib/registry',
    'data',
    'tpl!./list',
    'tpl!./item',
    'util/previews',
    'util/video/scrubber',
    'util/jquery.ui.draggable.multiselect'
], function(defineComponent, registry, appData, template, vertexTemplate, previews, VideoScrubber) {
    'use strict';

    return defineComponent(List);

    function List() {

        this.defaultAttrs({
            itemSelector: '.vertex-item',
            infiniteScrolling: false
        });

        this.stateForVertex = function(vertex) {
            var inWorkspace = appData.inWorkspace(vertex);
            return {
                inGraph: inWorkspace,
                inMap: inWorkspace && !!(
                        vertex.properties.geoLocation || 
                        (vertex.location || (vertex.locations && vertex.locations.length)) ||
                        (vertex.properties.latitude && vertex.properties.longitude)
                )
            };
        };

        this.classNameMapForVertices = function(vertices) {
            var self = this,
                classNamesForVertex = {};

            vertices.forEach(function(v) {

                // Check if this vertex is in the graph/map
                var classes = ['gId' + encodeURIComponent(v.id)];
                var vertexState = self.stateForVertex(v);
                if ( vertexState.inGraph ) classes.push('graph-displayed');
                if ( vertexState.inMap ) classes.push('map-displayed');

                if (v.properties._subType === 'video' || v.properties._subType === 'image' || v.properties._glyphIcon) {
                    classes.push('has_preview');
                }

                classNamesForVertex[v.id] = classes.join(' ');
            });

            return classNamesForVertex;
        };

        this.after('initialize', function() {
            var self = this;

            this.$node
                .addClass('vertex-list')
                .html(template({ 
                    vertices:this.attr.vertices,
                    infiniteScrolling: this.attr.infiniteScrolling && this.attr.total !== this.attr.vertices.length,
                    classNamesForVertex: this.classNameMapForVertices(this.attr.vertices)
                }));

            this.attachEvents();


            this.loadVisibleResultPreviews = _.debounce(this.loadVisibleResultPreviews.bind(this), 1000);
            this.loadVisibleResultPreviews();

            this.triggerInfiniteScrollRequest = _.debounce(this.triggerInfiniteScrollRequest.bind(this), 1000);
            this.triggerInfiniteScrollRequest();

            this.setupDraggables();

            this.onObjectsSelected(null, { edges:[], vertices:appData.selectedVertices});

            this.on('selectAll', this.onSelectAll);
            this.on('down', this.move);
            this.on('up', this.move);
        });

        this.move = function(e, data) {
            var previousSelected = this.$node.find('.active')[e.type === 'up' ? 'first' : 'last'](),
                moveTo = previousSelected[e.type === 'up' ? 'prev' : 'next']('.vertex-item');

            if (moveTo.length) {

                var selected = [];

                if (data.shiftKey) {
                    selected = selected.concat(appData.selectedVertices);
                    selected.push(appData.vertex(moveTo.data('vertexId')));
                } else {
                    selected.push(appData.vertex(moveTo.data('vertexId')));
                }

                this.trigger(document, 'defocusVertices');
                this.trigger('selectObjects', { vertices:selected });
            }
        };

        this.onSelectAll = function(e) {
            e.stopPropagation();

            var items = this.$node.find('.vertex-item').addClass('active');
            this.selectItems(items);
        };

        this.after('teardown', function() {
            this.$node.off('mouseenter mouseleave');
            this.scrollNode.off('scroll.vertexList');
            this.$node.empty();
        });

        this.attachEvents = function() {
            this.scrollNode = this.$node;
            while (this.scrollNode.length && this.scrollNode.css('overflow') !== 'auto') {
                this.scrollNode = this.scrollNode.parent();
            }
            this.scrollNode.on('scroll.vertexList', this.onResultsScroll.bind(this));

            this.$node.on('mouseenter mouseleave', '.vertex-item', this.onHoverItem.bind(this));

            this.on(document, 'verticesAdded', this.onVerticesUpdated);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'verticesDeleted', this.onVerticesDeleted);
            this.on(document, 'objectsSelected', this.onObjectsSelected);
            this.on(document, 'switchWorkspace', this.onWorkspaceClear);
            this.on(document, 'workspaceDeleted', this.onWorkspaceClear);
            this.on(document, 'workspaceLoaded', this.onWorkspaceLoaded);
            this.on('addInfiniteVertices', this.onAddInfiniteVertices);
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

            if (this.attr.infiniteScrolling) {
                this.triggerInfiniteScrollRequest();
            }
        };

        this.triggerInfiniteScrollRequest = function() {
            if (!this.attr.infiniteScrolling) return;

            var loadingListElement = this.$node.find('.infinite-loading');

            if (this.scrollNode.length) {
                loadingListElement = loadingListElement.withinScrollable(this.scrollNode);
            }

            if (loadingListElement.length) {
                var data = _.pick(this.attr, 'verticesType', 'verticesSubType');
                if (!this.offset) this.offset = this.attr.vertices.length;
                data.paging = {
                    offset: this.offset
                };
                this.trigger('infiniteScrollRequest', data);
            }
        };

        this.onAddInfiniteVertices = function(evt, data) {
            var loading = this.$node.find('.infinite-loading');

            if (data.vertices.length === 0) {
                loading.remove();
                this.attr.infiniteScrolling = false;
            } else {
                this.offset += data.vertices.length;
                var clsMap = this.classNameMapForVertices(data.vertices),
                    added = data.vertices.map(function(vertex) {
                        return vertexTemplate({
                            vertex:vertex,
                            classNamesForVertex: clsMap
                        });
                    });

                var lastItem = loading.prev();

                loading.before(added);

                var total = data.total || this.attr.total || 0;
                if (total === this.$node.find('.vertex-item').length) {
                    loading.remove();
                    this.attr.infiniteScrolling = false;
                } else {
                    this.triggerInfiniteScrollRequest();
                }

                this.loadVisibleResultPreviews();

                this.applyDraggable(this.$node.find('a'));
            }
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
                
                if (!vertex) return;

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
                            previews.generatePreview(vertex.properties._rowKey, { width: 200 }, function(poster, frames) {
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
                selection: function(ev, ui) {
                    self.selectItems(ui.selected);
                }
            });
        };

        this.selectItems = function(items) {
            var vertices = appData.vertices(items.map(function() {
                    return $(this).data('vertexId');
                }).toArray());

            if (vertices.length > 1) {
                vertices.forEach (function (vertex) {
                    vertex.workspace = {
                        selected: true
                    };
                });
            }
            this.trigger(document, 'defocusVertices');
            this.trigger('selectObjects', { vertices:vertices });
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
        };

        this.onVerticesUpdated = function(event, data) {
            var self = this;
            (data.vertices || []).forEach(function(vertex) {
                self.toggleItemIcons(vertex.id, self.stateForVertex(vertex));
                var currentAnchor = self.$node.find('li.gId' + encodeURIComponent(vertex.id)).children('a'),
                    newAnchor = $(vertexTemplate({
                        vertex: vertex,
                        classNamesForVertex: self.classNameMapForVertices([vertex]),
                    })).children('a'),
                    currentHtml = currentAnchor.html(),
                    hasPreview = false;

                if (vertex.properties._type === 'artifact') {
                    newAnchor.find('.preview').replaceWith(currentAnchor.find('.preview').clone());
                    hasPreview = true;
                } else {
                    if (vertex.properties._glyphIcon) {
                        $('<img/>').attr('src', vertex.properties._glyphIcon).appendTo(newAnchor.find('.preview'));
                    }
                    hasPreview = !!vertex.properties._glyphIcon;
                }

                var newHtml = newAnchor.html();
                if (currentAnchor.length && newHtml !== currentHtml) {
                    currentAnchor.html(newHtml).closest('.vertex-item').toggleClass('has_preview', hasPreview);
                }
            });
        };

        this.onVerticesDeleted = function(event, data) {
            var self = this;
            (data.vertices || []).forEach(function(vertex) {
                self.toggleItemIcons(vertex.id, { inGraph:false, inMap:false });
            });
        };

        this.onObjectsSelected = function(event, data) {
            this.$node.find('.active').removeClass('active');

            var ids = _.chain(data.vertices)
                .map(function(v) { return '.gId' + v.id; })
                .value().join(',');

            $(ids).addClass('active');
        };
    }
});
