
define([
    'flight/lib/component',
    'util/video/scrubber',
    './image/image',
    '../withTypeContent',
    '../withHighlighting',
    'detail/dropdowns/objectDetectionForm/objectDetectionForm',
    'tpl!./artifact'
], function(defineComponent, VideoScrubber, Image, withTypeContent, withHighlighting, ObjectDetectionForm, template) {

    'use strict';

    return defineComponent(Artifact, withTypeContent, withHighlighting);

    function Artifact() {

        this.defaultAttrs({
            previewSelector: '.preview',
            imagePreviewSelector: '.image-preview',
            detectedObjectSelector: '.detected-object'
        });

        this.after('initialize', function() {
            var self = this;

            this.on('click', {
                detectedObjectSelector: this.onDetectedObjectClicked
            });

            this.$node.on('mouseenter', '.image-preview', this.onImageEnter.bind(this));

            this.$node.on('mouseenter mouseleave', '.detected-object', this.onDetectedObjectHover.bind(this));

            this.loadArtifact();
        });

        this.loadArtifact = function() {
            var self = this;

            this.handleCancelling(this.ucdService.getArtifactById(this.attr.data._rowKey || this.attr.data.rowKey, function(err, artifact) {
                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                artifact.dataInfo = JSON.stringify({
                    _type: 'artifact',
                    _subType: artifact.type,
                    graphVertexId: artifact.Generic_Metadata['atc:graph_vertex_id'],
                    _rowKey: artifact.key.value
                });

                self.$node.html(template({ 
                    artifact: self.setupContentHtml(artifact), 
                    highlightButton: self.highlightButton(),
                    fullscreenButton: self.fullscreenButton([artifact.Generic_Metadata['atc:graph_vertex_id']])
                }));

                if (self[artifact.type + 'Setup']) {
                    self[artifact.type + 'Setup'](artifact);
                }

            }));
        };

        this.onDetectedObjectClicked = function(event) {
            var tagInfo = $(event.target).data('info');
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

            $('.artifact').Jcrop({
                onSelect: function (x) { self.onSelectImage(x, $('.artifact .image')); },
                onRelease: self.onSelectImageRelease
            });
        }

        this.onSelectImage = function (coords, imageInfo){
            var aspectHeight = imageInfo.height()/imageInfo[0].naturalHeight;
            var aspectWidth = imageInfo.width()/imageInfo[0].naturalWidth;

            var dataInfo = {
                coords: {
                    x1: (coords.x / aspectWidth),
                    x2: (coords.x2 / aspectWidth),
                    y1: (coords.y / aspectHeight),
                    y2: (coords.y2 / aspectHeight)
                }
            };

            this.showForm(dataInfo, this.attr.data);
        }

        this.showForm = function (dataInfo, artifactInfo){
            if ($('.detected-object-labels .underneath').length === 0) {
                ObjectDetectionForm.teardownAll ();
            }
            var root = $('<div class="underneath">').insertAfter('.detected-object-labels');

            ObjectDetectionForm.attachTo (root, {
                artifactData: artifactInfo,
                coords: dataInfo.coords,
                detectedObjectRowKey: dataInfo._rowKey,
                graphVertexId: dataInfo.graphVertexId,
                model: dataInfo.model
            });
        }

        this.onSelectImageRelease = function (){
            if ($('.detected-object-labels .underneath').length === 0) {
                ObjectDetectionForm.teardownAll ();
            }
        }
     }
});
