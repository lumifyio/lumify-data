
define([
    'flight/lib/component',
    'util/video/scrubber',
    'util/audio/scrubber',
    'util/privileges',
    './image/image',
    '../withTypeContent',
    '../withHighlighting',
    'detail/dropdowns/termForm/termForm',
    'detail/properties/properties',
    'tpl!./artifact',
    'tpl!./transcriptEntry',
    'hbs!./transcriptEntries',
    'hbs!./text',
    'tpl!util/alert',
    'util/range',
    'util/vertex/formatters',
    'service/ontology',
    'service/vertex',
    'data'
], function(
    defineComponent,
    VideoScrubber,
    AudioScrubber,
    Privileges,
    Image,
    withTypeContent, withHighlighting,
    TermForm,
    Properties,
    template,
    transcriptEntryTemplate,
    transcriptEntriesTemplate,
    textTemplate,
    alertTemplate,
    rangeUtils,
    F,
    OntologyService,
    VertexService,
    appData) {
    'use strict';

    var BITS_FOR_INDEX = 12;

    return defineComponent(Artifact, withTypeContent, withHighlighting);

    function Artifact() {

        this.ontologyService = new OntologyService();
        this.vertexService = new VertexService();

        this.defaultAttrs({
            previewSelector: '.preview',
            audioPreviewSelector: '.audio-preview',
            currentTranscriptSelector: '.currentTranscript',
            imagePreviewSelector: '.image-preview',
            detectedObjectLabelsSelector: '.detected-object-labels',
            detectedObjectSelector: '.detected-object',
            detectedObjectTagSelector: '.detected-object-tag',
            artifactSelector: '.artifact-image',
            propertiesSelector: '.properties',
            titleSelector: '.artifact-title',
            textContainerSelector: '.texts',
            textContainerHeaderSelector: '.texts .text-section h1',
            timestampAnchorSelector: '.av-times a'
        });

        this.after('initialize', function() {
            var self = this;

            this.on('click', {
                detectedObjectSelector: this.onDetectedObjectClicked,
                textContainerHeaderSelector: this.onTextHeaderClicked,
                timestampAnchorSelector: this.onTimestampClicked
            });
            this.on('copy cut', {
                textContainerSelector: this.onCopyText
            });
            this.on('scrubberFrameChange', this.onScrubberFrameChange);
            this.on('playerTimeUpdate', this.onPlayerTimeUpdate);
            this.on('DetectedObjectCoordsChange', this.onCoordsChanged);
            this.on('termCreated', this.onTeardownDropdowns);
            this.on('dropdownClosed', this.onTeardownDropdowns);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.on(document, 'textUpdated', this.onTextUpdated);
            this.after('tearDownDropdowns', this.onTeardownDropdowns);

            this.$node.on('mouseenter.detectedObject mouseleave.detectedObject',
                          this.attr.detectedObjectTagSelector,
                          this.onDetectedObjectHover.bind(this));
            this.before('teardown', function() {
                self.$node.off('.detectedObject');
            })

            this.loadArtifact();
        });

        this.before('teardown', function() {
            this.select('propertiesSelector').teardownComponent(Properties);
        });

        this.offsetsForText = function(input, parentSelector, offsetTransform) {
            var offsets = [];
            input.forEach(function(node) {
                var parentInfo = node.el.closest('.entity').data('info'),
                    offset = 0;

                if (parentInfo) {
                    offset = offsetTransform(parentInfo.start);
                } else {
                    var previousEntity = node.el.prevAll('.entity').first(),
                    previousInfo = previousEntity.data('info'),
                    dom = previousInfo ?
                        previousEntity.get(0) :
                        node.el.closest(parentSelector)[0].childNodes[0],
                    el = node.el.get(0);

                    if (previousInfo) {
                        offset = offsetTransform(previousInfo.end);
                        dom = dom.nextSibling;
                    }

                    while (dom && dom !== el) {
                        if (dom.nodeType === 3) {
                            offset += dom.length;
                        } else {
                            offset += dom.textContent.length;
                        }
                        dom = dom.nextSibling;
                    }
                }

                offsets.push(offset + node.offset);
            });

            return _.sortBy(offsets, function(a, b) {
                return a - b
            });
        };

        this.bitMaskedValue = function(index, offset) {
            return (offset << BITS_FOR_INDEX) | index;
        };

        this.valuesForBitMaskOffset = function(value) {
            var indexMask = (1 << BITS_FOR_INDEX) - 1;
            return {
                index: value & indexMask,
                offset: value >> BITS_FOR_INDEX
            };
        };

        this.offsetsForTranscript = function(input) {
            var self = this,
                index = input[0].el.closest('dd').data('index'),
                endIndex = input[1].el.closest('dd').data('index');

            if (index !== endIndex) {
                return console.warn('Unable to select across timestamps');
            }

            var rawOffsets = this.offsetsForText(input, 'dd', function(offset) {
                    return self.valuesForBitMaskOffset(offset).offset;
                }),
                bitMaskedOffset = _.map(rawOffsets, _.bind(this.bitMaskedValue, this, index));

            return bitMaskedOffset;
        };

        this.onCopyText = function(event) {
            var selection = getSelection(),
                target = event.target;

            if (!selection.isCollapsed && selection.rangeCount === 1) {

                var $anchor = $(selection.anchorNode),
                    $focus = $(selection.focusNode),
                    isTranscript = $anchor.closest('.av-times').length,
                    offsetsFunction = isTranscript ?
                        'offsetsForTranscript' :
                        'offsetsForText',
                    offsets = this[offsetsFunction]([
                        {el: $anchor, offset: selection.anchorOffset},
                        {el: $focus, offset: selection.focusOffset}
                    ], '.text', _.identity),
                    range = selection.getRangeAt(0),
                    output = {},
                    contextRange = rangeUtils.expandRangeByWords(range, 4, output),
                    context = contextRange.toString(),
                    contextHighlight =
                        '...' +
                        output.before +
                        '<span class="selection">' + selection.toString() + '</span>' +
                        output.after +
                        '...';

                if (offsets) {
                    this.trigger('copydocumenttext', {
                        startOffset: offsets[0],
                        endOffset: offsets[1],
                        snippet: contextHighlight,
                        vertexId: this.attr.data.id,
                        textPropertyKey: $anchor.closest('.text-section').data('key'),
                        text: selection.toString(),
                        vertexTitle: F.vertex.title(this.attr.data)
                    });
                }
            }
        };

        this.onVerticesUpdated = function(event, data) {
            var matching = _.findWhere(data.vertices, { id: this.attr.data.id });

            if (matching) {
                this.select('titleSelector').html(
                    F.vertex.title(matching)
                );
            }
        };

        this.loadArtifact = function() {
            var self = this,
                vertex = self.attr.data;

            this.handleCancelling(appData.refresh(vertex))
                .done(this.handleVertexLoaded.bind(this));
        };

        this.handleVertexLoaded = function(vertex) {
            var self = this,
                properties = vertex && vertex.properties;

            this.attr.data = vertex;

            if (properties) {
                this.videoTranscript = ('http://lumify.io#videoTranscript' in properties) ?
                    properties['http://lumify.io#videoTranscript'].value : {};
                this.videoDuration = ('http://lumify.io#videoDuration' in properties) ?
                    properties['http://lumify.io#videoDuration'].value : 0;
            }

            vertex.detectedObjects = vertex.detectedObjects.sort(function(a, b) {
                var aX = a.x1, bX = b.x1;
                return aX - bX;
            });

            this.$node.html(template({
                vertex: vertex,
                fullscreenButton: this.fullscreenButton([vertex.id]),
                auditsButton: this.auditsButton(),
                F: F
            }));

            this.select('detectedObjectLabelsSelector').toggle(vertex.detectedObjects.length > 0);

            Properties.attachTo(this.select('propertiesSelector'), { data: vertex });

            this.updateText();

            var displayType = this.attr.data.concept.displayType;
            if (this[displayType + 'Setup']) {
                this[displayType + 'Setup'](this.attr.data);
            }
        };

        this.onTextUpdated = function(event, data) {
            if (data.vertexId === this.attr.data.id) {
                this.updateText();
            }
        };

        this.updateText = function() {
            var self = this,
                scrollParent = this.$node.scrollParent(),
                scrollTop = scrollParent.scrollTop(),
                expandedKey = this.$node.find('.text-section.expanded').data('key'),
                textProperties = _.filter(this.attr.data.properties, function(p) {
                    return p.name === 'http://lumify.io#videoTranscript' ||
                        p.name === 'http://lumify.io#text'
                });

            this.select('textContainerSelector').html(
                _.map(textProperties, function(p) {
                    return textTemplate({
                        description: p['http://lumify.io#textDescription'] || p.key,
                        key: p.key,
                        cls: F.className.to(p.key)
                    })
                })
            );

            if (this.attr.focus) {
                this.openText(this.attr.focus.textPropertyKey)
                    .done(function() {
                        var $text = self.$node.find('.' + F.className.to(self.attr.focus.textPropertyKey) + ' .text'),
                            $transcript = $text.find('.av-times'),
                            focusOffsets = self.attr.focus.offsets;

                        if ($transcript.length) {
                            var start = self.valuesForBitMaskOffset(focusOffsets[0]),
                                end = self.valuesForBitMaskOffset(focusOffsets[1]),
                                $container = $transcript.find('dd').eq(start.index);

                            rangeUtils.highlightOffsets($container.get(0), [start.offset, end.offset]);
                        } else {
                            rangeUtils.highlightOffsets($text.get(0), focusOffsets);
                        }
                        self.attr.focus = null;
                    });
            } else if (expandedKey || textProperties.length === 1) {
                this.openText(expandedKey || textProperties[0].key)
                    .done(function() {
                        scrollParent.scrollTop(scrollTop);
                    });
            }
        };

        this.onPlayerTimeUpdate = function(evt, data) {
            var time = data.currentTime * 1000;
            this.updateCurrentTranscript(time);
        };

        this.onScrubberFrameChange = function(evt, data) {
            var frameIndex = data.index,
                numberOfFrames = data.numberOfFrames,
                time = (this.videoDuration / numberOfFrames) * frameIndex;

            this.updateCurrentTranscript(time);
        };

        this.updateCurrentTranscript = function(time) {
            var transcriptEntry = this.findTranscriptEntryForTime(time),
                html = '';

            if (transcriptEntry) {
                html = transcriptEntryTemplate({
                    transcriptEntry: transcriptEntry,
                    formatTimeOffset: this.formatTimeOffset
                });
            }
            this.select('currentTranscriptSelector').html(html);
        };

        this.findTranscriptEntryForTime = function(time) {
            if (!this.videoTranscript || !this.videoTranscript.entries) {
                return null;
            }
            var bestMatch = this.videoTranscript.entries[0];
            for (var i = 0; i < this.videoTranscript.entries.length; i++) {
                if (this.videoTranscript.entries[i].start <= time) {
                    bestMatch = this.videoTranscript.entries[i];
                }
            }
            return bestMatch;
        };

        this.formatTimeOffset = function(time) {
            return sf('{0:h:mm:ss}', new sf.TimeSpan(time));
        };

        this.openText = function(propertyKey) {
            var self = this,
                $section = this.$node.find('.' + F.className.to(propertyKey))
                    .siblings('.loading').removeClass('loading').end()
                    .addClass('loading');

            return this.handleCancelling(
                this.vertexService.getArtifactHighlightedTextById(this.attr.data.id, propertyKey)
            ).done(function(artifactText) {
                var textElement = $section.find('.text');
                self.processArtifactText(artifactText, textElement);

                $section.addClass('expanded').removeClass('loading');

                self.updateEntityAndArtifactDraggables();

                self.scrollToRevealSection($section);
            });
        };

        this.scrollToRevealSection = function($section) {
            var scrollIfWithinPixelsFromBottom = 150,
                y = $section.offset().top,
                scrollParent = $section.scrollParent(),
                scrollTop = scrollParent.scrollTop(),
                scrollHeight = scrollParent[0].scrollHeight,
                height = scrollParent.outerHeight(),
                maxScroll = height * 0.5,
                fromBottom = height - y;

            if (fromBottom < scrollIfWithinPixelsFromBottom) {
                scrollParent.animate({
                    scrollTop: Math.min(scrollHeight - scrollTop, maxScroll)
                }, 'fast');
            }
        };

        this.onTextHeaderClicked = function(event) {
            var $section = $(event.target)
                    .closest('.text-section')
                    .siblings('.expanded').removeClass('expanded')
                    .end(),
                propertyKey = $section.data('key');

            if ($section.hasClass('expanded')) {
                $section.removeClass('expanded');
            } else {
                this.openText(propertyKey);
            }
        };

        this.onTimestampClicked = function(event) {
            var millis = $(event.target).data('millis');

            this.trigger(
                this.select('audioPreviewSelector').add(this.select('previewSelector')),
                'seekToTime', {
                seekTo: millis
            });
        };

        this.onDetectedObjectClicked = function(event) {
            if (Privileges.missingEDIT) {
                return;
            }
            event.preventDefault();
            var self = this,
                $target = $(event.target),
                info = $target.closest('.label-info').data('info');
            this.$node.find('.focused').removeClass('focused')
            $target.closest('.label-info').parent().addClass('focused');

            require(['util/actionbar/actionbar'], function(ActionBar) {
                self.ActionBar = ActionBar;
                ActionBar.teardownAll();
                self.off('.actionbar');

                if ($target.hasClass('resolved')) {

                    ActionBar.attachTo($target, {
                        alignTo: 'node',
                        actions: $.extend({
                            Open: 'open.actionbar'
                        }, Privileges.canEDIT ? {
                            Unresolve: 'unresolve.actionbar'
                        } : {})
                    });

                    self.on('open.actionbar', function() {

                        self.trigger('selectObjects', {
                            vertices: [
                                {
                                    id: $target.data('info').graphVertexId
                                }
                            ]
                        });
                    });
                    self.on('unresolve.actionbar', function() {
                        _.defer(self.showForm.bind(self), info, this.attr.data, $target);
                    });

                } else if (Privileges.canEDIT) {

                    ActionBar.attachTo($target, {
                        alignTo: 'node',
                        actions: {
                            Resolve: 'resolve.actionbar'
                        }
                    });

                    self.on('resolve.actionbar', function() {
                        _.defer(self.showForm.bind(self), info, this.attr.data, $target);
                    })
                }
            });

            this.trigger(this.select('imagePreviewSelector'), 'DetectedObjectEdit', info);
        };

        this.onCoordsChanged = function(event, data) {
            var self = this,
                vertex = appData.vertex(this.attr.data.id),
                detectedObject,
                width = parseFloat(data.x2) - parseFloat(data.x1),
                height = parseFloat(data.y2) - parseFloat(data.y1);

            if (vertex.detectedObjects) {
                detectedObject = $.extend(true, {}, _.find(vertex.detectedObjects, function(obj) {
                    if (obj.entityVertex) {
                        return obj.entityVertex.id === data.id;
                    }

                    return obj['http://lumify.io#rowKey'] === data.id;
                }));
            }

            if (width < 5 || height < 5) {
                this.$node.find('.underneath').teardownComponent(TermForm)
                return;
            }

            detectedObject = detectedObject || {};
            if (data.id === 'NEW') {
                detectedObject.isNew = true;
            }
            detectedObject.x1 = data.x1;
            detectedObject.y1 = data.y1;
            detectedObject.x2 = data.x2;
            detectedObject.y2 = data.y2;
            this.showForm(detectedObject, this.attr.data, this.$node);
            this.trigger(this.select('imagePreviewSelector'), 'DetectedObjectEdit', detectedObject);
            this.select('detectedObjectLabelsSelector').show();
            this.$node.find('.detected-object-labels .detected-object').each(function() {
                if ($(this).data('info')['http://lumify.io#rowKey'] === data.id) {
                    $(this).closest('span').addClass('focused')
                }
            });
        };

        this.onTeardownDropdowns = function() {
            this.$node.find('.detected-object-labels .focused').removeClass('focused')
            this.trigger(this.select('imagePreviewSelector'), 'DetectedObjectDoneEditing');
        };

        this.onDetectedObjectHover = function(event) {
            var $target = $(event.target),
                tag = $target.closest('.detected-object-tag'),
                badge = tag.find('.label-info'),
                info = badge.data('info');

            this.trigger(
                this.select('imagePreviewSelector'),
                event.type === 'mouseenter' ? 'DetectedObjectEnter' : 'DetectedObjectLeave',
                info
            );
        };

        this.processArtifactText = function(text, element) {
            var self = this,
                warningText = 'No Text Available';

            // Looks like JSON ?
            if (/^\s*{/.test(text)) {
                var json;
                try {
                    json = JSON.parse(text);
                } catch(e) { }

                if (json && !_.isEmpty(json.entries)) {
                    return element.html(transcriptEntriesTemplate({
                        entries: _.map(json.entries, function(e) {
                            return {
                                millis: e.start || e.end,
                                time: (_.isUndefined(e.start) ? '' : self.formatTimeOffset(e.start)) +
                                        ' - ' +
                                      (_.isUndefined(e.end) ? '' : self.formatTimeOffset(e.end)),
                                text: e.text
                            };
                        })
                    }));
                } else if (json) {
                    text = null;
                    warningText = 'No Transcripts Available';
                }
            }

            element.html(
                !text ?
                    alertTemplate({ warning: warningText }) :
                    text.replace(/(\n+)/g, '<br><br>$1')
            );
        }

        this.audioSetup = function(vertex) {
            AudioScrubber.attachTo(this.select('audioPreviewSelector'), {
                rawUrl: vertex.imageRawSrc
            })
        };

        this.videoSetup = function(vertex) {
            VideoScrubber.attachTo(this.select('previewSelector'), {
                rawUrl: vertex.imageRawSrc,
                posterFrameUrl: vertex.imageSrc,
                videoPreviewImageUrl: vertex.imageFramesSrc,
                allowPlayback: true
            });
        };

        this.imageSetup = function(vertex) {
            var self = this,
                data = {
                    src: vertex.imageDetailSrc,
                    id: vertex.id
                };
            Image.attachTo(this.select('imagePreviewSelector'), { data: data });
            this.before('teardown', function() {
                self.select('imagePreviewSelector').teardownComponent(Image);
            });
        };

        this.showForm = function(dataInfo, artifactInfo, $target) {
            this.$node.find('.underneath').teardownComponent(TermForm)
            var root = $('<div class="underneath">')
                .insertAfter($target.closest('.type-content').find('.detected-object-labels'));

            TermForm.attachTo (root, {
                artifactData: artifactInfo,
                dataInfo: dataInfo,
                existing: !!dataInfo.graphVertexId,
                detectedObject: true
            });
        };
     }
});
