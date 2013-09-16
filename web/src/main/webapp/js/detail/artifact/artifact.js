
define([
    'flight/lib/component',
    'util/video/scrubber',
    './image/image',
    '../withTypeContent',
    '../withHighlighting',
    'detail/dropdowns/objectDetectionForm/objectDetectionForm',
    'tpl!./artifact',
    'tpl!./transcriptEntry'
], function(defineComponent, VideoScrubber, Image, withTypeContent, withHighlighting, ObjectDetectionForm, template, transcriptEntryTemplate) {

    'use strict';

    return defineComponent(Artifact, withTypeContent, withHighlighting);

    function Artifact() {

        this.defaultAttrs({
            previewSelector: '.preview',
            currentTranscriptSelector: '.currentTranscript',
            imagePreviewSelector: '.image-preview',
            detectedObjectSelector: '.detected-object',
            artifactSelector: '.artifact'
        });

        this.after('initialize', function() {
            var self = this;

            this.on('click', {
                detectedObjectSelector: this.onDetectedObjectClicked
            });
            this.on(document, 'scrubberFrameChange', this.onScrubberFrameChange);
            this.on(document, 'videoTimeUpdate', this.onVideoTimeUpdate);

            this.$node.on('mouseenter', '.image-preview', this.onImageEnter.bind(this));

            this.$node.on('mouseenter mouseleave', '.detected-object', this.onDetectedObjectHover.bind(this));

            this.loadArtifact();
        });

        this.loadArtifact = function() {
            var self = this;

            $.when(
                this.handleCancelling(this.ucdService.getArtifactById(this.attr.data._rowKey)),
                this.handleCancelling(this.ucdService.getGraphVertexById(this.attr.data.graphVertexId))
            ).done(function(artifactResponse, vertexResponse) {
                var artifact = artifactResponse[0],
                    vertex = vertexResponse[0];
                    
                artifact.dataInfo = JSON.stringify({
                    _type: 'artifact',
                    _subType: artifact.type,
                    graphVertexId: artifact.Generic_Metadata['atc:graph_vertex_id'],
                    _rowKey: artifact.key.value
                });

                if(artifact.Content.video_transcript) {
                    self.videoTranscript = JSON.parse(artifact.Content.video_transcript);
                    self.videoDuration =  artifact.Content['atc:video_duration'];
                } else {
                    self.videoTranscript = null;
                    self.videoDuration =  null;
                }

                self.$node.html(template({
                    artifact: self.setupContentHtml(artifact), 
                    vertex: vertex,
                    highlightButton: self.highlightButton(),
                    fullscreenButton: self.fullscreenButton([artifact.Generic_Metadata['atc:graph_vertex_id']])
                }));

                if (self[artifact.type + 'Setup']) {
                    self[artifact.type + 'Setup'](artifact);
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
            var tagInfo = $(event.target).data('info');
            $(event.target).addClass('focused');
            this.showForm(tagInfo, this.attr.data);
        };


        this.onDetectedObjectHover = function(event) {
            if (event.type == 'mouseenter') {
                this.trigger(document, 'DetectedObjectEnter', $(event.target).data('info'));
            } else {
                this.trigger(document, 'DetectedObjectLeave', $(event.target).data('info'));
            }
        };

        this.setupContentHtml = function(artifact) {
            artifact.contentHtml = (artifact.Content.highlighted_text || artifact.Content.doc_extracted_text || "")
                    .replace(/[\n]+/g, "<br><br>\n");
            return artifact;
        };


        this.videoSetup = function(artifact) {
            VideoScrubber.attachTo(this.select('previewSelector'), {
                rawUrl: artifact.rawUrl,
                posterFrameUrl: artifact.posterFrameUrl,
                videoPreviewImageUrl: artifact.videoPreviewImageUrl,
                allowPlayback: true
            });
        };

        this.imageSetup = function(artifact) {
            var data = {
                src: artifact.rawUrl,
                id: artifact.Generic_Metadata['atc:graph_vertex_id']
            };
            Image.attachTo(this.select('imagePreviewSelector'), { data: data });
        };

        this.onImageEnter = function(event){
            var self = this;

            $(this.select('artifactSelector')).Jcrop({
                onSelect: function (x) { self.onSelectImage(x, self.attr.data); },
                onRelease: self.onSelectImageRelease
            });
        }

        this.onSelectImage = function (coords, artifactInfo){
            var imageInfo = $('.artifact .image');
            var aspectHeight = imageInfo.height()/imageInfo[0].naturalHeight;
            var aspectWidth = imageInfo.width()/imageInfo[0].naturalWidth;

            var dataInfo = {
                info : {
                    coords: {
                        x1: (coords.x / aspectWidth),
                        x2: (coords.x2 / aspectWidth),
                        y1: (coords.y / aspectHeight),
                        y2: (coords.y2 / aspectHeight)
                    }
                }
            };

            this.showForm(dataInfo, artifactInfo);
        }

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
        }

        this.onSelectImageRelease = function (){
            if ($('.detected-object-labels .underneath').length === 0) {
                ObjectDetectionForm.teardownAll ();
            }
        }
     }
});
