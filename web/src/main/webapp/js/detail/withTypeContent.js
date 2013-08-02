
define(['service/ucd'], function(UCD) {

    return withTypeContent;

    function withTypeContent() {

        this.ucdService = new UCD();
        this._xhrs = [];

        this.after('teardown', function() {
            this.cancel();
            this.$node.empty();
        });

        this.before('initialize', function() {
            this.$node.html('Loading...');
        });

        this.cancel = function() {
            this._xhrs.forEach(function(xhr) {
                if (xhr.state() !== 'complete') {
                    xhr.abort();
                }
            });
            this._xhrs.length = 0;
        };

        // Pass a started XHR request to automatically cancel if detail pane
        // changes
        this.handleCancelling = function(xhr) {
            this._xhrs.push(xhr);
        };
    }
});
