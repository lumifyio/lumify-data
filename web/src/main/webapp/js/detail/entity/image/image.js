
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
                fileerror: this.onUploadError.bind(this),
                iconUpdated: this.onUpdateIcon.bind(this)
            });

            this.$node.css({
                backgroundImage: 'url(' + (this.attr.data._glyphIcon || this.attr.defaultIconSrc) + ')'
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
                self.$node.css({
                    backgroundImage: 'url(' + event.target.result + ')'
                });
            };

            if (file.size < MAX_PREVIEW_FILE_SIZE) {
                reader.readAsDataURL(file);
            }

            this.draw(0);
        };

        this.uploadFile = function(file) {
            var self = this,
                formData = new FormData();

            formData.append('file', file);

            // TODO: move to entityService
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/graph/vertex/' + this.attr.data.graphVertexId + '/uploadImage');
            xhr.onload = function(event) {
                if (xhr.status === 200) {
                    var result = JSON.parse(xhr.responseText);
                    self.trigger('filecomplete', { vertex:result });
                } else {
                    self.trigger('fileerror', { status:xhr.status, response:xhr.responseText });
                }
            };
            xhr.onerror = function() {
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
            this.firstProgressUpdate = true;
            xhr.send(formData);
        };

        this.handleFileDrop = function(file) {
            this.previewFile(file);
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
            c.arc(centerX, centerY, radius, - Math.PI / 2, 2 * Math.PI * Math.min(1.0, complete) - (Math.PI / 2), false);
            c.fillStyle = 'rgba(255,255,255,0.8)';
            c.fill();

            if (complete >= 1.0) {
                setTimeout(function() {
                    c.clearRect(0, 0, canvas.width, canvas.height);
                }, 250);
            }
        };

        this.onUploadError = function() {
            this.ctx.clearRect(0,0,this.canvas[0].width, this.canvas[0].height);
            this.$node.css({
                backgroundImage: 'url(' + (this.attr.data._glyphIcon || this.attr.defaultIconSrc) + ')'
            });
        };

        this.onUploadComplete = function(event, data) {
            var self = this;

            if (!this.animateManuallyIfNecessary(1.0)) {
                this.draw(1.0);
            }

            this.$node.css({
                backgroundImage: 'url(' + data.vertex.properties._glyphIcon + ')'
            });
            
            // FIXME: this should be necessary, convert all workspace code to
            // new id, properties:{} format
            var vertex = data.vertex;
            if (data.vertex.properties) {
                vertex = data.vertex.properties;
                vertex.graphVertexId = data.vertex.id;
            }
            this.trigger(document, 'updateVertices', { vertices:[vertex] });
        };

        this.onUpdateProgress = function(event, data) {
            var self = this;

            if (this.animateManuallyIfNecessary(data.complete)) {
                return;
            }

            this.draw(data.complete);
        };

        this.animateManuallyIfNecessary = function(complete) {
            var self = this;

            if (this.manualAnimation) return true;

            if (this.firstProgressUpdate && complete >= 1.0) {
                this.manualAnimation = true;
                var startedUpload = Date.now();

                // Animate manually, fast upload
                requestAnimationFrame(function draw() {
                    var now = Date.now();
                    var complete = (now - startedUpload) / 500;
                    self.draw(complete);
                    if (complete <= 1) {
                        requestAnimationFrame(draw);
                    } else self.draw(1.0);
                });
                return true;
            }
            this.firstProgressUpdate = false;

            return false;
        };
    }
});
