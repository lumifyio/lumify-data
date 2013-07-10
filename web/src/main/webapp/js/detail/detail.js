
define([
    'flight/lib/component',
    'service/ucd',
    'videojs',
    'util/video/scrubber',
    'tpl!util/video/video',
    'tpl!./artifactDetails',
    'tpl!./entityDetails',
    'tpl!./artifactToEntityRelationshipDetails',
    'tpl!./entityToEntityRelationshipDetails',
    'tpl!./multipleSelection',
    'tpl!./style'
], function(defineComponent, UCD, videojs, VideoScrubber, videoTemplate, artifactDetailsTemplate, entityDetailsTemplate, artifactToEntityRelationshipDetails, entityToEntityRelationshipDetails, multipleSelectionTemplate, styleTemplate) {
    'use strict';

    videojs.options.flash.swf = "/libs/video.js/video-js.swf";

    var HIGHLIGHT_STYLES = [
            { name: 'None' },
            { name: 'Subtle Icons', cls:'icons' },
            { name: 'Underline', cls:'underline' },
            { name: 'Colors', cls:'colors' }
        ],
        DEFAULT = 2;

    return defineComponent(Detail);

    function Detail() {

        this.useDefaultStyle = true;

        this.defaultAttrs({
            mapCoordinatesSelector: '.map-coordinates',
            highlightTypeSelector: '.highlight-options a',
            entitiesSelector: '.entity',
            previewSelector: '.preview',
            videoSelector: 'video'
        });

        this.after('initialize', function() {
            this.on('click', {
                mapCoordinatesSelector: this.onMapCoordinatesClicked,
                highlightTypeSelector: this.onHighlightTypeClicked,
                previewSelector: this.onPreviewClicked
            });

            this.on(document, 'searchResultSelected', this.onSearchResultSelected);
        });

        this.onMapCoordinatesClicked = function(evt, data) {
            var $target = $(evt.target);
            data = {
                latitude: $target.attr('latitude'),
                longitude: $target.attr('longitude')
            };
            this.trigger('mapCenter', data);
        };

        this.onHighlightTypeClicked = function(evt) {
            var target = $(evt.target),
                li = target.parents('li'),
                ul = li.parent('ul'),
                content = ul.parents('.content');

            ul.find('.checked').not(li).removeClass('checked');
            li.addClass('checked');

            this.removeHighlightClasses();
            
            var newClass = li.data('cls');
            if (newClass) {
                content.addClass('highlight-' + newClass);
            }

            this.useDefaultStyle = false;
        };

        this.onPreviewClicked = function(evt) {
            if (this.select('videoSelector').length) {
                return;
            }

            var self = this,
                players = videojs.players,
                video = $(videoTemplate({
                    mp4Url: self.currentArtifact.rawUrl + '?playback=true&type=video/mp4',
                    webmUrl: self.currentArtifact.rawUrl + '?playback=true&type=video/webm',
                    posterUrl: self.currentArtifact.posterFrameUrl
                }));


            this.select('previewSelector').html(video);
            Object.keys(players).forEach(function(player) {
                if (players[player]) {
                    players[player].dispose();
                    delete players[player];
                }
            });
            videojs(video[0], { autoplay:true }, function() { });
        };


        this.getActiveStyle = function() {
            if (this.useDefaultStyle) {
                return DEFAULT;
            }

            var content = this.$node,
                index = 0;
            $.each( content.attr('class').split(/\s+/), function(_, item) {
                var match = item.match(/^highlight-(.+)$/);
                if (match) {
                    return HIGHLIGHT_STYLES.forEach(function(style, i) {
                        if (style.cls === match[1]) {
                            index = i;
                            return false;
                        }
                    });
                }
            });

            return index;
        };

        this.removeHighlightClasses = function() {
            var content = this.$node;
            $.each( content.attr('class').split(/\s+/), function(index, item) {
                if (item.match(/^highlight-(.+)$/)) {
                    content.removeClass(item);
                }
            });
        };

        this.onSearchResultSelected = function(evt, data) {

            if ($.isArray(data) && data.length === 1) {
                data = data[0];
            }

            self.currentArtifact = null;

            if ( !data || data.length === 0 ) {
                this.$node.empty();
                this.currentRowKey = null;
            } else if($.isArray(data)) {
                this.$node.html(multipleSelectionTemplate({nodes:data}));
                this.currentRowKey = null;
            } else if(data.type == 'artifact') {
                this.onArtifactSelected(evt, data);
            } else if(data.type == 'entity') {
                this.onEntitySelected(evt, data);
            } else if(data.type == 'relationship') {
                this.onRelationshipSelected(evt, data);
            } else {
                var message = 'Unhandled type: ' + data.type;
                console.error(message);
                return this.trigger(document, 'error', { message: message });
            }
        };

        this.onRelationshipSelected = function(evt, data) {
            var self = this;
            this.$node.html("Loading...");
            // TODO show something more useful here.
            console.log('Showing relationship:', data);
            if(data.relationshipType == 'artifactToEntity') {
                self.$node.html(artifactToEntityRelationshipDetails(data));
            } else if(data.relationshipType == 'entityToEntity') {
                new UCD().getEntityToEntityRelationshipDetails(data.source, data.target, function(err, relationshipData) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    console.log(relationshipData);
                    relationshipData.styleHtml = self.getStyleHtml();
                    self.$node.html(entityToEntityRelationshipDetails(relationshipData));

                    self.applyHighlightStyle();
                    self.updateEntityDraggables();
                });
            } else {
                self.$node.html("Bad relationship type:" + data.relationshipType);
            }
        };

        this.onArtifactSelected = function(evt, data) {
            this.openUnlessAlreadyOpen(data, function(finished) {
                var self = this;
                new UCD().getArtifactById(data.rowKey, function(err, artifact) {
                    finished(!err);

                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    self.currentArtifact = artifact;
                    console.log('Showing artifact:', artifact);
                    artifact.contentHtml = artifact.Content.highlighted_text || artifact.Content.doc_extracted_text || "";
                    artifact.contentHtml = artifact.contentHtml.replace(/[\n]+/g, "<br><br>\n");
                    var styleHtml = self.getStyleHtml();
                    self.$node.html(artifactDetailsTemplate({ artifact: artifact, styleHtml: styleHtml }));

                    if (artifact.type == 'video') {
                        self.setupVideo(artifact);
                    }
                    self.applyHighlightStyle();
                    self.updateEntityDraggables();
                });
            });
        };

        this.getStyleHtml = function() {
            return styleTemplate({ styles: HIGHLIGHT_STYLES, activeStyle: this.getActiveStyle() });
        };

        this.onEntitySelected = function(evt, data) {
            this.openUnlessAlreadyOpen(data, function(finished) {
                var self = this;

                new UCD().getEntityById(data.rowKey, function(err, entity) {
                    finished(!err);

                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    console.log('Showing entity:', entity);
                    self.$node.html(entityDetailsTemplate(entity));
                });

            });
        };

        this.openUnlessAlreadyOpen = function(data, callback) {
            if (this.currentRowKey === data.rowKey) {
                return;
            }

            this.$node.html("Loading...");

            callback.call(this, function(success) {
                this.currentRowKey = success ? data.rowKey : null;
            }.bind(this));
        };


        this.updateEntityDraggables = function() {
            var entities = this.select('entitiesSelector');

            var $this = this;
            entities.draggable({
                helper:'clone',
                revert: 'invalid',
                revertDuration: 250,
                scroll: false,
                zIndex: 100
            });
        };

        this.setupVideo = function(artifact) {
            VideoScrubber.attachTo(this.select('previewSelector'), {
                poster: artifact.posterFrameUrl,
                frames: artifact.videoPreviewImageUrl
            });
        };

        this.applyHighlightStyle = function() {
            var newClass = HIGHLIGHT_STYLES[this.getActiveStyle()].cls;
            if (newClass) {
                this.removeHighlightClasses();
                this.$node.addClass('highlight-' + newClass);
            }
        };

    }
});
