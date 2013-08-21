
define([
    'flight/lib/component',
    'tpl!./image'
], function(defineComponent, template) {

    'use strict';

    // Limit previews to 1MB since it's a dataUri
    var MAX_PREVIEW_FILE_SIZE = 1024 * 1024; 

    return defineComponent(Image);

    function Image() {

        this.defaultAttrs({
            canvasSelector: 'canvas',
            acceptedTypesRegex: /image\/[jpe?g|png]/i 
        });

        this.after('initialize', function() {
            var self = this;

            this.on({
                fileprogress: this.onUpdateProgress.bind(this),
                filecomplete: this.onUploadComplete.bind(this),
                iconUpdated: this.onUpdateIcon.bind(this)
            });

            this.$node.css({
                backgroundImage: 'url(' + (self.attr.data._glyphIcon || this.attr.defaultIconSrc) + ')'
            });
            this.$node.html(template({}));

            if (/entity/i.test(this.attr.data._type)) {
                this.node.ondragover = function () { $(this).addClass('file-hover'); return false; };
                this.node.ondragenter = function () { $(this).addClass('file-hover'); return false; };
                this.node.ondragleave = function() { $(this).removeClass('file-hover'); return false; };
                this.node.ondrop = function (e) {
                    $(this).removeClass('file-hover');
                    e.preventDefault();
                    if (e.dataTransfer.files.length === 1) {
                        var file = e.dataTransfer.files[0];

                        if (self.attr.acceptedTypesRegex.test(file.type)) {
                            return self.handleFileDrop(file);
                        }
                    }

                    $(this).addClass('shake');
                    setTimeout(function() {
                        $(this).removeClass('shake');
                    }, 1000);
                };
            }
        });

        this.onUpdateIcon = function(e, data) {
            if (data.src !== this.attr.data._glyphIcon) {
                this.$node.css({
                    backgroundImage: 'url(' + data.src + ')'
                });
            }
        };

        this.previewFile = function(file) {
            var self = this, 
                reader = new FileReader();

            reader.onload = function (event) {
                if (file.size < MAX_PREVIEW_FILE_SIZE) {
                    self.$node.css({
                        backgroundImage: 'url(' + event.target.result + ')'
                    });
                }
                self.draw(0);
            };

            reader.readAsDataURL(file);
        };

        this.uploadFile = function(file) {
            var self = this,
                formData = new FormData();

            formData.append('file', file);

            // TODO: move to entityService
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/graph/node/' + this.attr.data.graphNodeId + '/uploadImage');
            xhr.onload = function(event) {
                var result = JSON.parse(xhr.responseText);
                self.trigger('fileprogress', { complete: 1.0 });
                self.trigger('filecomplete', { node:result });
            };
            xhr.onerror = function() {
                self.draw(1);
                console.error(arguments);
            };

            xhr.upload.onprogress = function (event) {
                if (event.lengthComputable) {
                    var complete = (event.loaded / event.total || 0);
                    if (complete < 1.0) {
                        self.trigger('fileprogress', { complete: complete });
                    }
                }
            };

            this.manualAnimation = false;
            xhr.send(formData);
        };

        this.handleFileDrop = function(file) {
            this.previewFile(file);
            this.firstProgressUpdate = true;
            this.uploadFile(file);
        };

        this.draw = function(complete) {
            if (!this.ctx) {
                this.canvas = this.select('canvasSelector');
                this.ctx = this.canvas[0].getContext('2d');
            }

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
                }, 250);
            }
        };

        this.onUploadComplete = function(event, data) {

            this.$node.css({
                backgroundImage: 'url(' + data.node.properties._glyphIcon + ')'
            });
            
            // FIXME: this should be necessary, convert all workspace code to
            // new id, properties:{} format
            var node = data.node;
            if (data.node.properties) {
                node = data.node.properties;
                node.graphNodeId = data.node.id;
            }
            this.trigger(document, 'updateNodes', { nodes:[node] });
        };

        this.onUpdateProgress = function(event, data) {
            var self = this;

            if (this.manualAnimation) return;

            if (this.firstProgressUpdate && data.complete >= 1.0) {
                this.manualAnimation = true;
                var startedUpload = Date.now();

                // Animate manually, fast upload
                requestAnimationFrame(function draw() {
                    var now = Date.now();
                    var complete = (now - startedUpload) / 500;
                    self.draw(complete);
                    if (complete <= 1) {
                        requestAnimationFrame(draw);
                    }
                });
                return;
            }
            this.firstProgressUpdate = false;

            this.draw(data.complete);
        };
    }
});
