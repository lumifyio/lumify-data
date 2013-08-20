
define([
    'flight/lib/component',
    'tpl!./image'
], function(defineComponent, template) {

    'use strict';

    return defineComponent(Image);

    function Image() {

        this.defaultAttrs({
            imageSelector: 'img'
        });

        this.after('initialize', function() {
            var self = this;

            this.on({
                filesdropped: this.uploadFiles.bind(this),
                fileprogress: this.updateProgress.bind(this)
            });

            this.$node.html(template({src:this.attr.defaultIconSrc}));

            if (/entity/i.test(this.attr.data._type)) {
                this.node.ondragover = function () { this.className = 'file-hover'; return false; };
                this.node.ondragenter = function () { this.className = 'file-hover'; return false; };
                this.node.ondragleave = function() { this.className = ''; return false; };
                this.node.ondrop = function (e) {
                    this.className = '';
                    e.preventDefault();
                    self.trigger('filesdropped', {files:e.dataTransfer.files});
                };
            }
        });

        this.uploadFiles = function(event, data) {
            
            var self = this,
                formData = new FormData(),
                files = data.files;

            for (var i = 0; i < files.length; i++) {
                formData.append('file', files[i]);
                // TODO: preview the image with progress overlay
                //this.previewfile(files[i]);
            }

            // TODO: move to entityService
            var xhr = new XMLHttpRequest();
            xhr.open('POST', '/graph/node/' + this.attr.data.graphNodeId + '/uploadImage');
            xhr.onload = function() {
                self.trigger('fileprogress', { complete: 100 });
            };
            xhr.onerror = function() {
                console.error(arguments);
            };

            xhr.upload.onprogress = function (event) {
                if (event.lengthComputable) {
                    var complete = (event.loaded / event.total * 100 || 0);
                    self.trigger('fileprogress', { complete: complete });
                }
            };

            xhr.send(formData);
        };

        this.updateProgress = function(event, data) {
            console.log(data.complete);
            if (data.complete >= 100) {
                console.log('FINISHED: set image tag');
            }
        };
    }
});
