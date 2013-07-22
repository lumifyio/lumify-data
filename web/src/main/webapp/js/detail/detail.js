
define([
    'flight/lib/component',
    'service/ucd',
    'util/video/scrubber',
    'tpl!./artifactDetails',
    'tpl!./entityDetails',
    'tpl!./entityToEntityRelationshipDetails',
    'tpl!./entityToEntityRelationshipExcerpts',
    'tpl!./multipleSelection',
    'tpl!./entityDetailsMentions',
    'tpl!./style'
], function(
    defineComponent,
    UCD,
    VideoScrubber,
    artifactDetailsTemplate,
    entityDetailsTemplate,
    entityToEntityRelationshipDetailsTemplate,
    entityToEntityRelationshipExcerptsTemplate,
    multipleSelectionTemplate,
    entityDetailsMentionsTemplate,
    styleTemplate) {
    'use strict';


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
            entityToEntityRelationshipSelector: '.entity-to-entity-relationship a.relationship-summary',
            entityDetailsMentionsSelector: '.entity-mentions'
        });

        this.after('initialize', function() {
            this.on('click', {
                mapCoordinatesSelector: this.onMapCoordinatesClicked,
                highlightTypeSelector: this.onHighlightTypeClicked,
                moreMentionsSelector: this.onRequestMoreMentions,
                mentionArtifactSelector: this.onMentionArtifactSelected,
                entityToEntityRelationshipSelector: this.onEntityToEntityRelationshipClicked
            });

            this.on('scrollstop', this.updateEntityAndArtifactDraggables);
            this.on(document, 'searchResultSelected', this.onSearchResultSelected);
            this.on(document, 'loadRelatedSelected', this.onLoadRelatedSelected);
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
            evt.preventDefault();
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

       this.onLoadRelatedSelected = function (evt, data){
            if ($.isArray (data) && data.length == 1){
                data = data [0];
            }
            // TO DO: Handle loading related artifacts
            if (!data || data.length == 0){
                this.$node.empty ();
                this.currentRowKey = null;
            } else if (data.type == 'entity') {
                this.onLoadRelatedEntitySelected (evt, data);
            } else if (data.type == 'artifact'){
                this.onLoadRelatedArtifactSelected (evt, data);
            } else {
                console.error ('Unhandled type: ' + data.type);
                return this.trigger (document, 'error', { message: message });
            }
       };

        this.onLoadRelatedEntitySelected = function (evt, data){
           this.openUnlessAlreadyOpen (data, function (finished){
                var self = this;
                new UCD ().getEntityById (data.rowKey, function (err, entity){
                    finished (!err);
                    if (err){
                        console.error ('Error', err);
                        return self.trigger (document, 'error', { message: err.toString () });
                    }

                    self.getLoadRelatedEntities (data.rowKey, function (relatedEntities){
                        var entityData = {};
                        entityData.key = entity.key;
                        entityData.relatedEntities = relatedEntities;
                        var xOffset = 100, yOffset = 100;
                        var x = data.originalPosition.x;
                        var y = data.originalPosition.y;
                        self.trigger(document, 'addNodes', {
                            nodes: entityData.relatedEntities.map(function(relatedEntity, index) {
                                if (index % 10 === 0) {
                                    y += yOffset;
                                }
                                return {
                                    title: relatedEntity.title,
                                    rowKey: relatedEntity.rowKey,
                                    subType: relatedEntity.subType,
                                    type: relatedEntity.type,
                                    graphPosition: {
                                        x: x + xOffset * (index % 10 + 1),
                                        y: y
                                    },
                                    selected: true
                                };
                            })
                        });
                    });
                });
           });
        };

        this.onLoadRelatedArtifactSelected = function (evt, data){
            this.openUnlessAlreadyOpen (data, function (finished){
                var self = this;
                new UCD ().getArtifactById (data.rowKey, function (err, artifact){
                    finished (!err);
                    if (err){
                        console.error ('Error', err);
                        return self.trigger (document, 'error', { message: err.toString () });
                    }

                    self.getLoadRelatedTerms (data.rowKey, function (relatedTerms){
                        var termData = {};
                        termData.key = artifact.key;
                        termData.relatedTerms = relatedTerms;
                        var xOffset = 100, yOffset = 100;
                        var x = data.originalPosition.x;
                        var y = data.originalPosition.y;
                        self.trigger(document, 'addNodes', {
                            nodes: termData.relatedTerms.map(function(relatedTerm, index) {
                                if (index % 10 === 0) {
                                    y += yOffset;
                                }
                                return {
                                    title: relatedTerm.title,
                                    rowKey: relatedTerm.rowKey,
                                    subType: relatedTerm.subType,
                                    type: relatedTerm.type,
                                    graphPosition: {
                                        x: x + xOffset * (index % 10 + 1),
                                        y: y
                                    },
                                    selected: true
                                };
                            })
                        });
                    });
                });
            });
        };

        this.getLoadRelatedEntities = function (key, callback){
            var self = this;
            new UCD().getRelatedEntitiesBySubject (key, function (err, relatedEntities){
                if (err){
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }
                console.log ('Related Entities', relatedEntities);
                callback (relatedEntities);
            });
        };

        this.getLoadRelatedTerms = function (key, callback){
            var self = this;
            new UCD().getRelatedTermsFromArtifact (key, function (err, relatedTerms){
                if (err){
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString() });
                }
                console.log ('Related Terms', relatedTerms);
                callback (relatedTerms);
            });
        }

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
                    this.trigger(document, 'searchResultSelected', {
                        type: 'artifact',
                        rowKey: data.source,
                        entityOfInterest: data.target
                    });
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
                    var url = 'entity/' + encodeURIComponent(data.rowKey) + '/mentions?offset=' + offset + '&limit=' + limit;
                    var dataInfo = JSON.stringify({
                        'rowKey': entity.key.value,
                        'type': 'entity',
                        'subType': entity.key.conceptLabel
                    });

                    self.getMoreMentions(url, entity.key, dataInfo, function(mentionsHtml) {
                        var entityDetailsData = {};
                        entityDetailsData.key = entity.key;

                        self.getRelationships(data.rowKey, function(relationships) {
                            entityDetailsData.relationships = relationships.statements;
                            console.log('Showing entity:', entityDetailsData);
                            entityDetailsData.styleHtml = self.getStyleHtml();
                            self.$node.html(entityDetailsTemplate(entityDetailsData));
                            $('.entity-mentions', self.$node).html(mentionsHtml);
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
                url: $target.attr("href")
            };

            var dataInfo = JSON.stringify({
                'rowKey': data.key.value,
                'type': 'entity',
                'subType': data.key.conceptLabel
            });

            this.getMoreMentions(data.url, data.key, dataInfo, function(mentionsHtml){
                $('.entity-mentions', self.$node).html(mentionsHtml);
                self.applyHighlightStyle();
                self.updateEntityAndArtifactDraggables();
            });
            evt.preventDefault();
        };

        this.getMoreMentions = function(url, key, dataInfo, callback) {
            new UCD().getEntityMentionsByRange(url, function(err, mentions){
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                mentions.mentions.forEach(function(termMention) {
                    var originalSentenceText = termMention['atc:sentenceText'];
                    var originalSentenceTextParts = {};

                    var artifactTermMention = JSON.parse(termMention.mention);

                    var termMentionStart = artifactTermMention.start - parseInt(termMention['atc:sentenceOffset'], 10);
                    var termMentionEnd = artifactTermMention.end - parseInt(termMention['atc:sentenceOffset'], 10);

                    termMention.sentenceTextParts = {
                        before: originalSentenceText.substring(0, termMentionStart),
                        term: originalSentenceText.substring(termMentionStart, termMentionEnd),
                        after: originalSentenceText.substring(termMentionEnd)
                    }
                });
                var html = entityDetailsMentionsTemplate({
                    mentions: mentions.mentions,
                    limit: mentions.limit,
                    offset: mentions.offset,
                    key: key,
                    dataInfo: dataInfo
                });
                console.log("Mentions: ", mentions);
                callback(html);
            });
        };

        this.getRelationships = function(rowKey, callback) {
            var self = this;
            new UCD().getEntityRelationshipsBySubject(encodeURIComponent(rowKey), function(err, relationships) {
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

            // Only create draggables for items in the visible scroll area
            entities.add(artifacts).withinScrollable(this.$node).draggable({
                helper:'clone',
                revert: 'invalid',
                revertDuration: 250,
                scroll: false,
                zIndex: 100
            });
        };

        this.setupVideo = function(artifact) {
            VideoScrubber.attachTo(this.select('previewSelector'), {
                rawUrl: artifact.rawUrl,
                posterFrameUrl: artifact.posterFrameUrl,
                videoPreviewImageUrl: artifact.videoPreviewImageUrl,
                allowPlayback: true
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
