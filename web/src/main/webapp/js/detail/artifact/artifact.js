
define([
    'flight/lib/component',
    'util/video/scrubber',
    './image/image',
    '../withTypeContent',
    '../withHighlighting',
    'detail/dropdowns/termForm/termForm',
    'detail/properties',
    'tpl!./artifact',
    'tpl!./transcriptEntry',
    'service/ontology',
    'service/entity',
    'data'
], function(
    defineComponent,
    VideoScrubber,
    Image,
    withTypeContent, withHighlighting,
    TermForm,
    Properties,
    template,
    transcriptEntryTemplate,
    OntologyService,
    EntityService,
    appData) {
    'use strict';

    return defineComponent(Artifact, withTypeContent, withHighlighting);

    function Artifact() {

        this.ontologyService = new OntologyService();
        this.entityService = new EntityService();

        this.defaultAttrs({
            previewSelector: '.preview',
            currentTranscriptSelector: '.currentTranscript',
            imagePreviewSelector: '.image-preview',
            detectedObjectSelector: '.detected-object',
            artifactSelector: '.artifact-image',
            propertiesSelector: '.properties',
            titleSelector: '.artifact-title',
            deleteTagSelector: '.detected-object-tag .delete-tag',
            textSelector: '.text'
        });

        this.after('initialize', function() {
            var self = this;

            this.on('click', {
                detectedObjectSelector: this.onDetectedObjectClicked,
                deleteTagSelector: this.onDeleteTagClicked
            });

            this.on('scrubberFrameChange', this.onScrubberFrameChange);
            this.on('videoTimeUpdate', this.onVideoTimeUpdate);
            this.on('DetectedObjectCoordsChange', this.onCoordsChanged);
            this.on('termCreated', this.onTeardownDropdowns);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);
            this.after('tearDownDropdowns', this.onTeardownDropdowns);

            this.$node.on('mouseenter mouseleave', '.detected-object-tag', this.onDetectedObjectHover.bind(this));

            this.loadArtifact();
        });

        this.onVerticesUpdated = function(event, data) {
            var matching = _.findWhere(data.vertices, { id: this.attr.data.id });

            if (matching) {
                this.select('titleSelector').html( matching.properties.title );
            }
        };

        this.loadArtifact = function() {
            var self = this,
                vertex = self.attr.data;

            this.handleCancelling(appData.refresh(vertex))
                .done(this.handleVertexLoaded.bind(this));
        };

        this.handleVertexLoaded = function(vertex) {
            var self = this;
            this.videoTranscript = vertex.artifact.videoTranscript;
            this.videoDuration = vertex.artifact.videoDuration;

            if (vertex.properties._detectedObjects) {
                vertex.properties._detectedObjects = JSON.parse(vertex.properties._detectedObjects).sort(function(a, b){
                    var aX = a.x1, bX = b.x1;
                    return aX - bX;
                });
            }

            this.$node.html(template({
                vertex: vertex,
                highlightButton: this.highlightButton(),
                fullscreenButton: this.fullscreenButton([vertex.id])
            }));

            Properties.attachTo(this.select('propertiesSelector'), { data: vertex });

            this.ucdService.getArtifactHighlightedTextById(vertex.id).done(function(artifactText) {
                self.select('textSelector').html(artifactText);
                self.updateEntityAndArtifactDraggables();

                if (self[vertex.properties._subType + 'Setup']) {
                    self[vertex.properties._subType + 'Setup'](vertex);
                }
            });
        };

        this.onVideoTimeUpdate = function(evt, data) {
            var time = data.currentTime * 1000;
            this.updateCurrentTranscript(time);
        };

        this.onScrubberFrameChange = function(evt, data) {
            var frameIndex = data.index;
            var numberOfFrames = data.numberOfFrames;
            var time = (this.videoDuration / numberOfFrames) * frameIndex;
            this.updateCurrentTranscript(time);
        };

        this.updateCurrentTranscript = function(time) {
            var transcriptEntry = this.findTranscriptEntryForTime(time);
            var html = '';
            if(transcriptEntry) {
                html = transcriptEntryTemplate({
                    transcriptEntry: transcriptEntry,
                    formatTimeOffset: this.formatTimeOffset
                });
            }
            this.select('currentTranscriptSelector').html(html);
        };

        this.findTranscriptEntryForTime = function(time) {
            if(!this.videoTranscript || !this.videoTranscript.entries) {
                return null;
            }
            var bestMatch = this.videoTranscript.entries[0];
            for(var i = 0; i < this.videoTranscript.entries.length; i++) {
                if(this.videoTranscript.entries[i].start <= time) {
                    bestMatch = this.videoTranscript.entries[i];
                }
            }
            return bestMatch;
        };

        this.formatTimeOffset = function(time) {
            return sf('{0:h:mm:ss}', new sf.TimeSpan(time));
        };

        this.onDetectedObjectClicked = function(event) {
            event.preventDefault();
            var self = this,
                $target = $(event.target),
                info = $target.closest('.label-info').data('info');

            $target.closest('.label-info').parent().addClass('focused');
            info.existing = true;
            this.trigger('DetectedObjectEdit', info);
            this.showForm(info, this.attr.data, $target);
        };

        this.onCoordsChanged = function(event, data) {
            var self = this,
                vertex = appData.vertex(this.attr.data.id);
            var detectedObject = $.extend(true, {}, _.find(vertex.properties._detectedObjects, function(obj) {
                    return (obj && obj.graphVertexId) === data.id;
                })),
                width = parseFloat(data.x2)-parseFloat(data.x1),
                height = parseFloat(data.y2)-parseFloat(data.y1);

            if (width < 5 || height < 5) {
                return TermForm.teardownAll();
            }

            detectedObject = detectedObject || {};
            detectedObject.x1 = data.x1;
            detectedObject.y1 = data.y1;
            detectedObject.x2 = data.x2;
            detectedObject.y2 = data.y2;
            this.showForm(detectedObject, this.attr.data, this.$node);
        };

        this.onTeardownDropdowns = function() {
            this.trigger('DetectedObjectDoneEditing');
        };

        this.onDeleteTagClicked = function (event) {
            var self = this;
            var $detectedObjectTag = $(event.target).siblings();
            var info = { objectInfo: JSON.stringify($detectedObjectTag.data('info')) };
            var $loading = $("<span>")
                .addClass("badge")
                .addClass("loading");

            $detectedObjectTag.closest('.detected-object-tag').addClass('loading');
            $(event.target).replaceWith($loading);

            this.entityService.deleteDetectedObject(info)
                .done(function(data) {
                    var resolvedVertex = {
                        id: data.id,
                        _subType: data.properties._subType,
                        _type: data.properties._type
                    };
                    $detectedObjectTag.parent().remove();
                    self.trigger('DetectedObjectLeave', $detectedObjectTag.data('info'));

                    if (data.remove){
                        self.trigger(document, 'deleteVertices', { vertices: [resolvedVertex] });
                    } else {
                        self.trigger(document, 'updateVertices', { vertices: [resolvedVertex] });
                        self.trigger(document, 'deleteEdge', { edgeId: data.edgeId });
                    }
                });
        };

        this.onDetectedObjectHover = function(event) {
            var $target = $(event.target),
                tag = $target.closest('.detected-object-tag'),
                badge = tag.find('.label-info'),
                info = badge.data('info');

            this.trigger(
                event.type === 'mouseenter' ? 'DetectedObjectEnter' : 'DetectedObjectLeave',
                info
            );
        };

        this.videoSetup = function(vertex) {
            VideoScrubber.attachTo(this.select('previewSelector'), {
                rawUrl: vertex.artifact.rawUrl,
                posterFrameUrl: vertex.artifact.posterFrameUrl,
                videoPreviewImageUrl: vertex.artifact.videoPreviewImageUrl,
                allowPlayback: true
            });
        };

        this.imageSetup = function(vertex) {
            var self = this;
            var data = {
                src: vertex.artifact.rawUrl,
                id: vertex.id
            };
            Image.attachTo(this.select('imagePreviewSelector'), { data: data });
            this.before('teardown', function (){
                self.select('imagePreviewSelector').teardownComponent(Image);
            });
        };

        this.showForm = function (dataInfo, artifactInfo, $target){
            if ($('.detected-object-labels .underneath').length === 0) {
                TermForm.teardownAll ();
            }
            var root = $('<div class="underneath">').insertAfter($target.closest('.type-content').find('.detected-object-labels'));
            var resolvedVertex = {
                graphVertexId: dataInfo.graphVertexId,
                _rowKey: dataInfo._rowKey,
                _subType: dataInfo._subType,
                title: dataInfo.title
            };

            TermForm.attachTo (root, {
                artifactData: artifactInfo,
                x1: dataInfo.x1,
                y1: dataInfo.y1,
                x2: dataInfo.x2,
                y2: dataInfo.y2,
                graphVertexId: dataInfo.graphVertexId,
                resolvedVertex: resolvedVertex,
                existing: dataInfo.existing,
                detectedObject: true
            });
        };
     }
});
