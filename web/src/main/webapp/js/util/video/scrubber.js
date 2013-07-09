

define([
    'flight/lib/component',
    'tpl!./scrubber'
], function(defineComponent, template) {
    'use strict';

    // TODO: get this from the server
    var NUMBER_FRAMES = 21,
        POSTER = 1,
        FRAMES = 2;

    return defineComponent(VideoScrubber);

    function VideoScrubber() {

        this.showing = 0;
        this.currentFrame = -1;

        this.defaultAttrs({
            scrubbingLineSelector: '.scrubbing-line'
        });

        this.showFrames = function(index) {
            if (index == this.currentFrame) {
                return;
            }

            var width = this.$node.width();

            this.select('scrubbingLineSelector').css({
                left: (index / NUMBER_FRAMES) * width
            }).show();

            var css = {
                backgroundSize: (width * NUMBER_FRAMES) + 'px auto',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: (width * (index||0) * -1) + 'px center'
            };

            if (this.showing !== FRAMES) {
                css.backgroundImage = 'url(' + this.attr.frames + ')';
            }

            this.$node.css(css);
            this.showing = FRAMES;
            this.currentFrame = index;
        };

        this.showPoster = function() {
            this.select('scrubbingLineSelector').hide();

            var css = {
                backgroundSize: '100%',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'left center'
            };
            
            if ( this.showing !== POSTER ) {
                css.backgroundImage = 'url(' + this.attr.poster + ')';
            }

            this.$node.css(css);
            this.showing = POSTER;
            this.currentFrame = -1;
        };

        this.after('initialize', function() {
            var self = this;

            this.$node.html(template({}));

            this.showPoster();

            var image = new Image();
            image.src = this.attr.frames;

            this.on('mousemove', {
                scrubbingLineSelector: function(e) { e.stopPropagation(); }
            });
            this.$node
                .on('mousemove', function(e) {
                    if ($(e.target).is('.preview')) {
                        var index = Math.round(e.offsetX / this.offsetWidth * NUMBER_FRAMES);
                        self.showFrames(index);
                    }
                })
                .on('mouseleave', function(e) {
                    self.showPoster();
                });

        });
    }

    return VideoScrubber;
});
