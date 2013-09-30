
define([
    'flight/lib/component',
    'util/video/scrubber',
    './image/image',
    '../withTypeContent',
    '../withHighlighting',
    'detail/dropdowns/objectDetectionForm/objectDetectionForm',
    'detail/properties',
    'tpl!./artifact',
    'tpl!./transcriptEntry',
    'service/ontology',
    'service/entity',
    'data'
], function(
    defineComponent,
    VideoScrubber, Image,
    withTypeContent, withHighlighting,
    ObjectDetectionForm,
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
            artifactSelector: '.artifact',
            propertiesSelector: '.properties',
            titleSelector: '.artifact-title',
            deleteTagSelector: '.detected-object-tag .delete-tag',
            detectedObjectTagSelector: '.detected-object-labels'
        });

        this.after('initialize', function() {
            var self = this;

            this.on('click', {
                detectedObjectSelector: this.onDetectedObjectClicked,
                deleteTagSelector: this.onDeleteTagClicked
            });

            this.on(document, 'scrubberFrameChange', this.onScrubberFrameChange);
            this.on(document, 'videoTimeUpdate', this.onVideoTimeUpdate);
            this.on(document, 'verticesUpdated', this.onVerticesUpdated);

            this.$node.on('mouseenter', '.image-preview', this.onImageEnter.bind(this));
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
            var self = this;
            var tagInfo = $(event.target).data('info');
            $(event.target).parent().addClass('focused');
            if ($(event.target).hasClass('resolved')){
                var imageInfo = $('.artifact .image');
                var aspectHeight = imageInfo.height()/imageInfo[0].naturalHeight;
                var aspectWidth = imageInfo.width()/imageInfo[0].naturalWidth;
                var coords = {
                    x: (tagInfo.info.coords.x1 * aspectWidth),
                    y: (tagInfo.info.coords.y1 * aspectHeight),
                    x2: (tagInfo.info.coords.x2 * aspectWidth),
                    y2: (tagInfo.info.coords.y2 * aspectHeight)
                };

                $('.image-preview').unbind("mouseenter");

                $(this.select('artifactSelector')).Jcrop({
                    setSelect: [coords.x, coords.y, coords.x2, coords.y2],
                    onSelect: function (x) {self.onSelectImage(x, self.attr.data, tagInfo); },
                    onRelease: function () {
                       self.onSelectImageRelease();
                    }
                });
            } else {
                this.showForm(tagInfo, this.attr.data);
            };
        };

        this.onDeleteTagClicked = function (event) {
            var self = this;
            var $detectedObjectTag = $(event.target).siblings();
            var info = { objectInfo: JSON.stringify($detectedObjectTag.data('info')) };
            var $loading = $("<span>")
                .addClass("badge")
                .addClass("loading");

            $(event.target).addClass('focused').replaceWith($loading).removeClass('focused');
            $detectedObjectTag.bind('click', false);

            $.when(this.entityService.deleteDetectedObject(info)).then(function(data) {
                var resolvedVertex = {
                    id: data.id,
                    _subType: data.properties._subType,
                    _type: data.properties._type
                };

                $detectedObjectTag.parent().remove();
                self.trigger(document, 'DetectedObjectLeave', $detectedObjectTag.data('info'));

                if (data.remove){
                    self.trigger(document, 'deleteVertices', { vertices: [resolvedVertex] });
                } else {
                    self.trigger(document, 'updateVertices', { vertices: [resolvedVertex] });
                    self.trigger(document, 'deleteEdge', { edgeId: data.edgeId });
                }
            });
        };

        this.onDetectedObjectHover = function(event) {
            if (event.type == 'mouseenter') {
                this.trigger(document, 'DetectedObjectEnter', $(event.currentTarget).find('.label-info').data('info'));
            } else {
                this.trigger(document, 'DetectedObjectLeave', $(event.currentTarget).find('.label-info').data('info'));
            }
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
            var data = {
                src: vertex.artifact.rawUrl,
                id: vertex.id
            };
            Image.attachTo(this.select('imagePreviewSelector'), { data: data });
        };

        this.onImageEnter = function(event){
            var self = this;
            var dataInfo = $('.focused .label-info').data('info');

            $(self.select('artifactSelector')).Jcrop({
                onSelect: function (x) {
                    $('.detected-object-tag').unbind ('mouseenter mouseleave');
                    self.onSelectImage(x, self.attr.data, dataInfo);
                },
                onRelease: function () {
                    self.onSelectImageRelease();
                }
            });
        };

        this.onSelectImage = function (coords, artifactInfo, dataInfo){
            var imageInfo = $('.artifact .image');
            var aspectHeight = imageInfo.height()/imageInfo[0].naturalHeight;
            var aspectWidth = imageInfo.width()/imageInfo[0].naturalWidth;

            if (!dataInfo || $('.focused').length == 0) {
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

            this.showForm(dataInfo, artifactInfo);
        };

        this.showForm = function (dataInfo, artifactInfo){
            if ($('.detected-object-labels .underneath').length === 0) {
                ObjectDetectionForm.teardownAll ();
            }
            var root = $('<div class="underneath">').insertAfter('.detected-object-labels');
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

            ObjectDetectionForm.attachTo (root, {
                artifactData: artifactInfo,
                coords: dataInfo.info.coords,
                detectedObjectRowKey: dataInfo.info._rowKey,
                graphVertexId: dataInfo.graphVertexId,
                resolvedVertex: resolvedVertex,
                model: dataInfo.info.model,
                existing: existing
            });
        };

        this.onSelectImageRelease = function (){
            if ($('.detected-object-labels .underneath').length === 0) {
                ObjectDetectionForm.teardownAll ();
                $('.focused').removeClass('focused');
                $('.detected-object-tag').bind('mouseenter mouseleave');
                $('.image-preview').bind("mouseenter");
            }
        };
     }
});
