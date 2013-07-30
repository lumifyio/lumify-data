
define([
    'flight/lib/component',
    'tpl!./image',
    'tpl!util/blur/blur-svg'
], function(defineComponent, template, blur) {

    return defineComponent(Image);

    function Image() {

        this.defaultAttrs({
            imageSelector: 'img',
            svgPrefix: 'detail-pane'
        });

        this.after('initialize', function() {
            var html = template({ src: this.attr.src });

            if ($(this.prefixed('mask')).length === 0) {
                $(blur({prefix:this.attr.svgPrefix})).appendTo(document.body);
            }

            this.$node.css({
                backgroundImage: this.attr.src
            }).html(html);


            this.on(document, 'DetectedObjectEnter', this.onHover);
            this.on(document, 'DetectedObjectLeave', this.onHoverLeave);
        });

        this.prefixed = function(val) {
            return '#' + this.attr.svgPrefix + '-' + val;
        };


        this.onHover = function(event, data) {
            var image = this.select('imageSelector'),
                width = image.width(),
                height = image.height(),
                aspectWidth = width / image[0].naturalWidth,
                aspectHeight = height / image[0].naturalHeight,
                c = data.coords,
                w = (c.x2 - c.x1) * aspectWidth,
                h = (c.y2 - c.y1) * aspectHeight,
                x = c.x1 * aspectWidth + w / 2,
                y = c.y1 * aspectHeight + h / 2;

            // Firefox
            $(this.prefixed('gradient')).attr({ 
                cx: String( x / width ),
                cy: String( y / height ), 
                r: w / width 
            });

            image.css({
                // Webkit
                '-webkit-mask-box-image': '-webkit-radial-gradient(' + x+'px ' + y+'px, transparent 0%, black ' + Math.max(w,h) * 1.3 + 'px)',
                '-webkit-filter': "blur(5px) saturate(30%)",
                // Firefox
                'filter': "url(" + this.prefixed('blur') + ")",
                'mask': "url(" + this.prefixed('mask') + ")"
            });

        };
        this.onHoverLeave = function(event, data) {
            var image = this.select('imageSelector');
                
            image.css({ 
                '-webkit-filter': "none",
                'filter': "none",
                'mask': "none"
            });
        };
    }
});
