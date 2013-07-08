
define([
    'service/ucd',
    'html2canvas',
    'tpl!./previews'
], 
/**
 * Generate preview screenshots of artifact rendering (with highlighting)
 */
function(UCD, html2canvas, template) {

    var PREVIEW_CACHE = {};

    function Preview(rowKey, options, callback) {
        this.options = options || {};
        this.rowKey = rowKey;
        this.callback = callback;
        this.isCancelled = false;
    }

    Preview.prototype.start = function() {
        if (this.isCancelled) return;

        new UCD().getArtifactById(this.rowKey, function(err, artifact) {
            if (this.isCancelled) return;

            if (err) {
                console.error(err);
                callback();
                this.finished();
            } else {
                if (artifact.type == 'video') {
                    this.callback(artifact.posterFrameUrl, artifact.videoPreviewImageUrl);
                    PREVIEW_CACHE[this.rowKey] = [artifact.posterFrameUrl, artifact.videoPreviewImageUrl];
                    this.finished();
                } else {

                    // TODO: extract from detail pane and this to common function
                    var html = artifact.Content.highlighted_text || artifact.Content.doc_extracted_text;

                    this.callbackForContent(
                        artifact.Generic_Metadata.subject, 
                        html.replace(/[\n]+/g, "<br><br>\n")
                    );
                }
            }
        }.bind(this));
    };

    Preview.prototype.callbackForContent = function(title, html) {
        if (this.isCancelled) return;

        var self = this,
            width = this.options.width,
            previewDiv = $(template({
                width: width,
                title: title,
                html: html
            })).appendTo(document.body);

        function finish(url) {
            self.callback(url);
            PREVIEW_CACHE[self.rowKey] = [url];
            previewDiv.remove();
            self.finished();
        }

        var times = 0;
        function generate() {
            if (++times > 2) {
                return finish();
            }

            html2canvas(previewDiv[0], {
                onrendered: function(canvas) {
                    var dataUrl = canvas.toDataURL();

                    if (dataUrl.length < 5000) {
                        return generate();
                    }
                    finish(dataUrl);
                }
            });

        }

        generate();
    };

    Preview.prototype.finished = function() {
        if (this.taskFinished) {
            this.taskFinished();
        }
    };

    function PreviewQueue(name, opts) {
        this.name = name;
        this.items = [];
        this.executing = [];
        this.options = $.extend({
            maxConcurrent: 10
        }, opts);
    }
    PreviewQueue.prototype.addTask = function(task) {
        var cache = PREVIEW_CACHE[task.rowKey];
        if (cache) {
            console.log('PREVIEW using cache', task.rowKey);
            task.callback.apply(null, cache);
        } else {
            console.log('PREVIEW no cache', task.rowKey);
            this.items.push( task );
            this.take();
        }
    };
    PreviewQueue.prototype.take = function() {
        if (this.items.length === 0) return;

        if (this.executing.length < this.options.maxConcurrent) {
            var task = this.items.shift();
            this.executing.push( task );

            task.taskFinished = function() {
                this.executing.splice(this.executing.indexOf(task), 1);
                this.take();
            }.bind(this);

            task.start();
        }
    };
    PreviewQueue.prototype.cancel = function() {
        this.executing.forEach(function(task) {
            task.cancel();
        });
        this.executing.length = 0;
        this.items.length = 0;
    };

    var queues = {},
        defaultQueue = queues['default'] = new PreviewQueue('default', { maxConcurrent: 1 });

    return {

        cancelQueue: function(queueName) {
            queues[queueName || 'default'].cancel();
        },

        /**
         * Create a queue for preview processing
         *
         * @param queueName Name of the queue
         * @param options Options for configuring the queue
         * @param options.maxConcurrent Maximum concurrent operations on the queue
         */
        createQueue: function(queueName, options) {
            var queue = queues[queueName] = new PreviewQueue(queueName, options);
            return queue;
        },

        /**
         * Add a preview generation task to the queue
         *
         * @param rowKey The artifact rowKey
         * @param opts Options for preview generation
         * @param opts.width Width of the preview image preferred
         * @param opts.queueName Optional queue name to use
         * @param callback Task completion notification callback
         */
        generatePreview: function(rowKey, opts, callback) {
            var options = $.extend({
                width: 200,
                queueName: 'default'
            }, opts || {});

            var queue = queues[options.queueName];
            if ( !queue) {
                queue = queues[options.queueName] = new PreviewQueue(queueName, options.queueOptions);
            }

            delete options.queueOptions;
            delete options.queueName;

            queue.addTask( new Preview(rowKey, options, callback) );
        }
    };
});
