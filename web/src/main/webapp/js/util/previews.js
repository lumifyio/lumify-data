
define([
    'service/ucd',
    'html2canvas',
    'tpl!./previews'
], 
/**
 * Generate preview screenshots of artifact rendering (with highlighting)
 */
function(UCD, html2canvas, template) {

    var MAX_CONCURRENT = 1;

    function Preview(rowKey, options, callback) {
        this.options = options || {};
        this.rowKey = rowKey;
        this.callback = callback;
    }
    Preview.prototype.start = function() {

        new UCD().getArtifactById(this.rowKey, function(err, artifact) {
            if (err) {
                callback();
            } else {
                if (artifact.type == 'video') {
                    this.callback(artifact.posterFrameUrl);
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
        var self = this,
            width = this.options.width,
            previewDiv = $(template({
                width: width,
                title: title,
                html: html
            })).appendTo(document.body);

        //previewDiv.css({position:'absolute',top:100,left:200,right:'auto',zIndex:9999});
        
        function finish(url) {
            self.callback(url);
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

    var queue = [],
        executing = [];
        workQueue = function() {
            if (queue.length === 0) return;

            if (executing.length < MAX_CONCURRENT) {
                var task = queue.shift();
                executing.push( task );

                task.taskFinished = function() {
                    executing.splice(executing.indexOf(task), 1);
                    workQueue();
                };
                task.start();
            }
        };

    return {
        generatePreview: function(rowKey, options, callback) {
            if ( ! options || ! options.width ) {
                throw new Error("Width option required for previews");
            }

            queue.push( new Preview(rowKey, options, callback) );
            workQueue();
        }
    };
});
