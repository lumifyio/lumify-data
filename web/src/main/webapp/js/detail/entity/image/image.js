
define([
    'flight/lib/component',
    'tpl!./image'
], function(defineComponent, template) {

    'use strict';

    return defineComponent(Image);

    function Image() {

        this.defaultAttrs({
            canvasSelector: 'canvas',
            acceptedTypesRegex: /image\/[jpe?g|png]/i 
        });

        this.after('initialize', function() {
            var self = this;

            this.on({
                filesdropped: this.uploadFiles.bind(this),
                fileprogress: this.updateProgress.bind(this)
            });

            this.$node.css({
                backgroundImage: 'url(' + this.attr.defaultIconSrc + ')'
            });
            this.$node.html(template({}));

            if (/entity/i.test(this.attr.data._type)) {
                // http://html5demos.com/dnd-upload#view-source
                this.node.ondragover = function () { $(this).addClass('file-hover'); return false; };
                this.node.ondragenter = function () { $(this).addClass('file-hover'); return false; };
                this.node.ondragleave = function() { $(this).removeClass('file-hover'); return false; };
                this.node.ondrop = function (e) {
                    $(this).removeClass('file-hover');
                    e.preventDefault();
                    
                    if (e.dataTransfer.files.length === 1) {
                        var file = e.dataTransfer.files[0];

                        if (self.attr.acceptedTypesRegex.test(file.type)) {
                            return self.trigger('filesdropped', {file:file});
                        }
                    }

                    // TODO: shake the image
                };
            }
        });

        this.previewFile = function(file) {
            var self = this, 
                reader = new FileReader();

            reader.onload = function (event) {
                self.$node.css({
                    backgroundImage: 'url(' + event.target.result + ')'
                });
            };

            reader.readAsDataURL(file);
        };

        this.uploadFiles = function(event, data) {

            var self = this,
                formData = new FormData(),
                file = data.file;

            formData.append('file', file);
            this.previewFile(file);

            // TODO: move to entityService
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/graph/node/' + this.attr.data.graphNodeId + '/uploadImage');
            xhr.onload = function() {
                self.trigger('fileprogress', { complete: 1.0 });
            };
            xhr.onerror = function() {
                console.error(arguments);
            };

            xhr.upload.onprogress = function (event) {
                if (event.lengthComputable) {
                    var complete = (event.loaded / event.total || 0);
                    self.trigger('fileprogress', { complete: complete });
                }
            };

            this.manualAnimation = false;
            this.startedUpload = Date.now();
            xhr.send(formData);
        };

        this.draw = function(complete) {
            var c = this.ctx, canvas = this.canvas[0];
            canvas.width = this.canvas.width();
            canvas.height = this.canvas.height();
            // TODO: make retina

            var centerX = canvas.width / 2;
            var centerY = canvas.height / 2;
            var radius = Math.min(canvas.width, canvas.height) / 2 * 0.75;

            c.beginPath();
            c.moveTo(centerX, centerY);
            c.arc(centerX, centerY, radius + 2, - Math.PI / 2, 2 * Math.PI - (Math.PI / 2), false);
            c.fillStyle = 'rgba(0,0,0,0.5)';
            c.fill();

            c.beginPath();
            c.moveTo(centerX, centerY);
            c.arc(centerX, centerY, radius, - Math.PI / 2, 2 * Math.PI * complete - (Math.PI / 2), false);
            c.fillStyle = 'rgba(255,255,255,0.8)';
            c.fill();

            if (complete >= 1.0) {
                setTimeout(function() {
                    c.clearRect(0,0,canvas.width, canvas.height);
                }, 500);
            }
        };

        this.updateProgress = function(event, data) {
            var self = this;

            if (this.manualAnimation) return;

            if (!this.ctx) {
                this.canvas = this.select('canvasSelector');
                this.ctx = this.canvas[0].getContext('2d');
                if (data.complete >= 1.0) {
                    this.manualAnimation = true;
                    // Animate manually, fast upload
                    requestAnimationFrame(function draw() {
                        var now = Date.now();
                        var complete = (now - self.startedUpload) / 500;
                        self.draw(complete);
                        if (complete <= 1) {
                            requestAnimationFrame(draw);
                        }
                    });
                    return;
                }
            }

            this.draw(data.complete);
        };
    }
});
