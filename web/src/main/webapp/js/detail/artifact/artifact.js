
define([
    'flight/lib/component',
    'util/video/scrubber',
    './image/image',
    '../withTypeContent',
    '../withHighlighting',
    'tpl!./artifact'
], function(defineComponent, VideoScrubber, Image, withTypeContent, withHighlighting, template) {

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
            this.$node.on('mouseenter mouseleave', '.detected-object', this.onDetectedObjectHover.bind(this));

            this.loadArtifact();
        });

        this.loadArtifact = function() {
            var self = this;

            this.handleCancelling(this.ucdService.getArtifactById(this.attr.data.rowKey, function(err, artifact) {

                if(err) {
                    console.error('Error', err);
                    return self.trigger(document, 'error', { message: err.toString() });
                }

                self.$node.html(template({ artifact: self.setupContentHtml(artifact), highlightButton:self.highlightButton() }));

                if (self[artifact.type + 'Setup']) {
                    self[artifact.type + 'Setup'](artifact);
                }

                if (self.attr.data.entityOfInterest) {
                    // TODO: add some extra highlighting and scroll to this entity row key => self.attr.data.entityOfInterest);

                    /*
                    var n = self.$node.find('.entity')
                        .filter(function() { 
                            // Different escaping is breaking this
                            return $(this).data('info').rowKey === self.attr.data.entityOfInterest;
                        });
                    */
                }
            }));
        };

        this.onDetectedObjectClicked = function(event) {
            console.log('Clicked', event);
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
            Image.attachTo(this.select('imagePreviewSelector'), {
                src: artifact.rawUrl
            });
        };

    }
});
