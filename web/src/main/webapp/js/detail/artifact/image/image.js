
define([
    'flight/lib/component',
    'tpl!./image',
    'tpl!util/blur/blur-svg'
], function(defineComponent, template, blur, Jcrop) {
    'use strict';

    return defineComponent(Image);

    function Image() {

        this.defaultAttrs({
            imageSelector: 'img',
            boxSelector: '.facebox',
            svgPrefix: 'detail-pane'
        });

        this.after('initialize', function() {
            var html = template({ data: this.attr.data });

            this.$node.css({
                backgroundImage: this.attr.src
            }).html(html);

            // TODO: make local events for full-screen support
            this.$node.closest('.detail-pane').on('DetectedObjectEnter', this.onHover.bind(this));
            this.$node.closest('.detail-pane').on('DetectedObjectLeave', this.onHoverLeave.bind(this));
            this.before('teardown',function () {
                this.$node.closest('.detail-pane').off('DetectedObjectEnter');
                this.$node.closest('.detail-pane').off('DetectedObjectLeave');
            });
        });

        this.onHover = function(event, data) {
            var box = this.select('boxSelector');
            var image = this.select('imageSelector');
            var width = image.width(),
                height = image.height(),
                aspectWidth = width / image[0].naturalWidth,
                aspectHeight = height / image[0].naturalHeight,
                c = data.info.coords,
                w = (c.x2 - c.x1) * aspectWidth,
                h = (c.y2 - c.y1) * aspectHeight,
                x = c.x1 * aspectWidth,
                y = c.y1 * aspectHeight;
            box.hide().css({
                width: w,
                height: h,
                left: x,
                top: y
            }).show();
        };

        this.onHoverLeave = function(event, data) {
            this.select('boxSelector').hide();
        };
    }
});
