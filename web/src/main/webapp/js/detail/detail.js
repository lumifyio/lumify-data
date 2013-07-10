
define([
    'flight/lib/component',
    'service/ucd',
    'videojs',
    'util/video/scrubber',
    'tpl!util/video/video',
    'tpl!./artifactDetails',
    'tpl!./entityDetails',
    'tpl!./entityToEntityRelationshipDetails',
    'tpl!./entityToEntityRelationshipExcerpts',
    'tpl!./multipleSelection',
    'tpl!./style'
], function(
    defineComponent,
    UCD,
    videojs,
    VideoScrubber,
    videoTemplate,
    artifactDetailsTemplate,
    entityDetailsTemplate,
    entityToEntityRelationshipDetailsTemplate,
    entityToEntityRelationshipExcerptsTemplate,
    multipleSelectionTemplate,
    styleTemplate) {
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
            artifactsSelector: '.artifact',
            moreMentionsSelector: '.mention-request',
            mentionArtifactSelector: '.mention-artifact',
            previewSelector: '.preview',
            videoSelector: 'video',
            entityToEntityRelationshipSelector: '.entity-to-entity-relationship a.relationship-summary'
        });

        this.after('initialize', function() {
            this.on('click', {
                mapCoordinatesSelector: this.onMapCoordinatesClicked,
                highlightTypeSelector: this.onHighlightTypeClicked,
                moreMentionsSelector: this.onRequestMoreMentions,
                mentionArtifactSelector: this.onMentionArtifactSelected,
                previewSelector: this.onPreviewClicked,
                entityToEntityRelationshipSelector: this.onEntityToEntityRelationshipClicked
            });

            this.on(document, 'searchResultSelected', this.onSearchResultSelected);
        });

        this.onEntityToEntityRelationshipClicked = function(evt, data) {
            var self = this;
            var $target = $(evt.target).parents('li');
            var statementRowKey = $target.data('statement-row-key');

            if($target.hasClass('expanded')) {
                $target.removeClass('expanded');
                $target.addClass('collapsed');
                $('.artifact-excerpts', $target).hide();
            } else {
                $target.addClass('expanded');
                $target.removeClass('collapsed');

                new UCD().getStatementByRowKey(statementRowKey, function(err, statement) {
                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }

                    var statementMentions = Object.keys(statement)
                        .filter(function(key) {
                            return key.indexOf('urn') == 0;
                        })
                        .map(function(key) {
                            return statement[key];
                        });
                        console.log(statement);
                        console.log(statementMentions);
                    var html = entityToEntityRelationshipExcerptsTemplate({
                        mentions: statementMentions
                    });
                    $('.artifact-excerpts', $target).html(html);
                    $('.artifact-excerpts', $target).show();
                    self.updateEntityAndArtifactDraggables();
                });
            }
        };

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

        this.onMentionArtifactSelected = function(evt, data) {
            var $target = $(evt.target).parents('a');

            this.trigger(document, 'searchResultSelected', {
                type: 'artifact',
                rowKey: $target.data("row-key")
            });
            evt.preventDefault();
        }

        this.onRelationshipSelected = function(evt, data) {
            this.openUnlessAlreadyOpen(data, function(finished) {
                var self = this;
                if(data.relationshipType == 'artifactToEntity') {
                    finished(true);
                    self.$node.html(artifactToEntityRelationshipDetailsTemplate(data));
                } else if(data.relationshipType == 'entityToEntity') {
                    new UCD().getEntityToEntityRelationshipDetails(data.source, data.target, function(err, relationshipData) {
                        finished(!err);

                        if(err) {
                            console.error('Error', err);
                            return self.trigger(document, 'error', { message: err.toString() });
                        }
                        console.log(relationshipData);
                        relationshipData.styleHtml = self.getStyleHtml();
                        self.$node.html(entityToEntityRelationshipDetailsTemplate(relationshipData));

                        self.applyHighlightStyle();
                        self.updateEntityAndArtifactDraggables();
                    });
                } else {
                    finished(false);
                    self.$node.html("Bad relationship type:" + data.relationshipType);
                }
            });
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

                    console.log('TODO: add some extra highlighting and scroll to this entity row key', data.entityOfInterest);

                    self.applyHighlightStyle();
                    self.updateEntityAndArtifactDraggables();
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

                    var offset = 0;
                    var limit = 2; // change later
                    var url = 'entity/' + data.rowKey + '/' + offset + '/' + limit;
                    self.getMoreMentions(url, function(mentions) {
                        var entityDetailsData = mentions;
                        entityDetailsData.key = entity.key;

                        self.getRelationships(data.rowKey, function(relationships) {
                            entityDetailsData.relationships = relationships.statements;
                            console.log('Showing entity:', entityDetailsData);
                            entityDetailsData.styleHtml = self.getStyleHtml();
                            self.$node.html(entityDetailsTemplate(entityDetailsData));
                            self.applyHighlightStyle();
                            self.updateEntityAndArtifactDraggables();
                        });
                    });

                });

            });
        };

        this.onRequestMoreMentions = function(evt, data) {
            var self = this;
            var $target = $(evt.target);
            data = {
                key: $target.data('key'),
                relationships: $target.data('relationships'),
                url: $target.attr("href")
            };

            this.getMoreMentions(data.url, function(mentions){
                var entityDetailsData = mentions;
                entityDetailsData.key = data.key;
                entityDetailsData.relationships = data.relationships;
                console.log('Showing entity:', entityDetailsData);
                entityDetailsData.styleHtml = self.getStyleHtml();
                self.$node.html(entityDetailsTemplate(entityDetailsData));
                self.applyHighlightStyle();
                self.updateEntityAndArtifactDraggables();
            });
            evt.preventDefault();
        };

        this.getMoreMentions = function(url, callback) {
            new UCD().getEntityMentionsByRange(url, function(err, mentions){
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }
                console.log("Mentions: ", mentions);
                callback(mentions);
            });
        };

        this.getRelationships = function(rowKey, callback) {
            var self = this;
            new UCD().getEntityRelationshipsBySubject(rowKey, function(err, relationships) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                console.log("Relationships: ", relationships);
                callback(relationships);
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


        this.updateEntityAndArtifactDraggables = function() {
            var entities = this.select('entitiesSelector');
            var artifacts = this.select('artifactsSelector');

            var $this = this;
            entities.draggable({
                helper:'clone',
                revert: 'invalid',
                revertDuration: 250,
                scroll: false,
                zIndex: 100
            });
            artifacts.draggable({
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
