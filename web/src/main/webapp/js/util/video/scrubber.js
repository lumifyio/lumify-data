

define([
    'flight/lib/component',
    'videojs',
    'tpl!./scrubber',
    'tpl!./video'
], function(defineComponent, videojs, template, videoTemplate) {
    'use strict';

    // TODO: get this from the server
    var NUMBER_FRAMES = 20,
        POSTER = 1,
        FRAMES = 2;

    videojs.options.flash.swf = "/libs/video.js/video-js.swf";

    return defineComponent(VideoScrubber);

    function VideoScrubber() {

        this.showing = 0;
        this.currentFrame = -1;

        this.defaultAttrs({
            allowPlayback: false,
            scrubbingLineSelector: '.scrubbing-line',
            videoSelector: 'video'
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
                css.backgroundImage = 'url(' + this.attr.videoPreviewImageUrl + ')';
            }

            this.$node.css(css);
            this.showing = FRAMES;
            this.currentFrame = index;

            this.trigger(document, 'scrubberFrameChange', {
               index: index,
               numberOfFrames: NUMBER_FRAMES
            });
        };

        this.showPoster = function() {
            this.select('scrubbingLineSelector').hide();

            var css = {
                backgroundSize: '100%',
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'left center'
            };
            
            if ( this.showing !== POSTER ) {
                css.backgroundImage = 'url(' + this.attr.posterFrameUrl + ')';
            }

            this.$node.css(css);
            this.showing = POSTER;
            this.currentFrame = -1;

            this.trigger(document, 'scrubberFrameChange', {
               index: 0,
               numberOfFrames: NUMBER_FRAMES
            });
        };

        this.onClick = function(event) {
            if (this.attr.allowPlayback !== true || this.select('videoSelector').length) {
                return;
            }

            var self = this;
            var userClickedPlayButton = $(event.target).is('.scrubbing-play-button'),
                players = videojs.players,
                video = $(videoTemplate(this.attr));

            this.$node.html(video);
            Object.keys(players).forEach(function(player) {
                if (players[player]) {
                    players[player].dispose();
                    delete players[player];
                }
            });

            var scrubPercent = this.scrubPercent;
            _.defer(videojs, video[0], { autoplay:true }, function() {
                var player = this;
                if (!userClickedPlayButton) {
                    player.on("durationchange", durationchange);
                    player.on("loadedmetadata", durationchange);
                }
                player.on("timeupdate", timeupdate);

                function timeupdate(event) {
                    self.trigger(document, 'videoTimeUpdate', {
                        currentTime: player.currentTime(),
                        duration: player.duration()
                    });
                }

                function durationchange(event) {
                    var duration = player.duration();
                    if (duration > 0.0 && scrubPercent > 0.0) {
                        player.off('durationchange', durationchange);
                        player.off("loadedmetadata", durationchange);
                        player.currentTime(Math.max(0.0, duration * scrubPercent - 1.0));
                    }
                }
            });
        };

        this.after('initialize', function() {
            var self = this;

            this.$node.toggleClass('allowPlayback', this.attr.allowPlayback)
                      .html(template({}));

            this.showPoster();

            var image = new Image();
            image.src = this.attr.videoPreviewImageUrl;

            this.on('mousemove', {
                scrubbingLineSelector: function(e) { e.stopPropagation(); }
            });
            this.$node
                .on('mousemove', function(e) {
                    var target = $(e.target);
                    if (target.is('.preview')) {
                        var percent = e.offsetX / this.offsetWidth,
                            index = Math.round(percent * NUMBER_FRAMES);

                        self.scrubPercent = index / NUMBER_FRAMES;
                        self.showFrames(index);
                    } else if (target.is('.scrubbing-play-button')) {
                        self.showPoster();
                    }
                })
                .on('mouseleave', function(e) {
                    self.showPoster();
                })
                .on('click', self.onClick.bind(self));
        });
    }

    return VideoScrubber;
});
