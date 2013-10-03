
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
            deleteTagSelector: '.detected-object-tag .delete-tag'
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
            this.videoTranscript = vertex.artifact.videoTranscript;
            this.videoDuration = vertex.artifact.videoDuration;

            this.$node.html(template({
                artifact: vertex.artifact,
                vertex: vertex,
                highlightButton: this.highlightButton(),
                fullscreenButton: this.fullscreenButton([vertex.id])
            }));

            this.updateEntityAndArtifactDraggables();

            Properties.attachTo(this.select('propertiesSelector'), { data: vertex });

            if (this[vertex.artifact.type + 'Setup']) {
                this[vertex.artifact.type + 'Setup'](vertex);
            }
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

            this.trigger('DetectedObjectEdit', info);
            this.showForm(info, this.attr.data, $target);
        };

        this.onCoordsChanged = function(event, data) {
            var self = this,
                vertex = appData.vertex(this.attr.data.id),
                detectedObject = $.extend(true, {}, _.find(vertex.artifact.detectedObjects, function(obj) {
                    return (obj.info && obj.info._rowKey) === data.id;
                }));

            detectedObject.info = detectedObject.info || {};
            detectedObject.info.coords = data.coords;
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

        this.onSelectImage = function (coords, artifactInfo, dataInfo, $targetImage){
            var aspectHeight = $targetImage.height()/$targetImage[0].naturalHeight;
            var aspectWidth = $targetImage.width()/$targetImage[0].naturalWidth;

            if (!dataInfo || $('.focused').length === 0) {
                dataInfo = {
                    info: {}
                };
            }

            dataInfo.info.coords = {
                    x1: (coords.x / aspectWidth),
                    x2: (coords.x2 / aspectWidth),
                    y1: (coords.y / aspectHeight),
                    y2: (coords.y2 / aspectHeight)
            };

            this.showForm(dataInfo, artifactInfo, $targetImage);
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

            var existing = false;
            if (dataInfo.graphVertexId){
                existing = true;
            }

            TermForm.attachTo (root, {
                artifactData: artifactInfo,
                coords: dataInfo.info.coords,
                detectedObjectRowKey: dataInfo.info._rowKey,
                graphVertexId: dataInfo.graphVertexId,
                resolvedVertex: resolvedVertex,
                model: dataInfo.info.model,
                existing: existing,
                detectedObject: true
            });
        };
     }
});
