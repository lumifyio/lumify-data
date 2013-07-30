
define([
    'flight/lib/component',
    'tpl!./image'
], function(defineComponent, template) {

    return defineComponent(Image);

    function Image() {

        this.defaultAttrs({
            imageSelector: 'img'
        });

        this.after('initialize', function() {
            var html = template({ src: this.attr.src });

            this.$node.css({
                backgroundImage: this.attr.src
            }).html(html);


            this.on(document, 'DetectedObjectEnter', this.onHover);
            this.on(document, 'DetectedObjectLeave', this.onHoverLeave);
        });


        this.onHover = function(event, data) {
            var image = this.select('imageSelector'),
                aspectWidth = image.width() / image[0].naturalWidth,
                aspectHeight = image.height() / image[0].naturalHeight,
                c = data.coords,
                w = (c.x2 - c.x1) * aspectWidth,
                h = (c.y2 - c.y1) * aspectHeight,
                x = c.x1 * aspectWidth + w / 2,
                y = c.y1 * aspectHeight + h / 2;

            image.css({
                '-webkit-mask-box-image': '-webkit-radial-gradient(' + x+'px ' + y+'px, transparent 0%, black ' + Math.max(w,h) * 1.3 + 'px)',
                '-webkit-filter': "blur(5px) saturate(30%)"
            });
        };
        this.onHoverLeave = function(event, data) {
            var image = this.select('imageSelector');
                
            image.css({ '-webkit-filter': "none" });
        };
    }
});
