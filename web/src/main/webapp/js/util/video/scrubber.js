

define([
    'flight/lib/component'
], function(defineComponent) {
    'use strict';

    // TODO: get this from the server
    var NUMBER_FRAMES = 21,
        POSTER = 1,
        FRAMES = 2;

    return defineComponent(VideoScrubber);

    function VideoScrubber() {

        this.showing = 0;

        this.defaultAttrs();

        this.showFrames = function(index) {
            var width = this.$node.width();

            this.$node.css({
                backgroundSize: (width * NUMBER_FRAMES) + 'px auto',
                backgroundPosition: (width * (index||0) * -1) + 'px center',
                backgroundImage: this.showing !== FRAMES ? 'url(' + this.attr.frames + ')' : undefined
            });

            this.showing = FRAMES;
        };

        this.showPoster = function() {
            this.$node.css({
                backgroundSize: '100%',
                backgroundPosition: 'left center',
                backgroundImage: this.showing !== POSTER ? 'url(' + this.attr.poster + ')' : undefined
            });

            this.showing = POSTER;
        };

        this.after('initialize', function() {
            var self = this;

            this.showPoster();

            var image = new Image();
            image.src = this.attr.frames;

            this.$node
                .on('mousemove', function(e) {
                    var index = Math.round(e.offsetX / this.offsetWidth * NUMBER_FRAMES);

                    self.showFrames(index);
                })
                .on('mouseout', function(e) {
                    self.showPoster();
                });

        });
    }

    return VideoScrubber;
});
