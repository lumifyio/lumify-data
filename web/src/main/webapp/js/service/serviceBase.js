
define(
    function() {
        function ServiceBase(options) {
            options = options || {};

            //define deafault options
            var defaults = {

                // the base url to find the service
                serviceBaseUrl: "/",

                //the context of the service
                serviceContext: "",

                //to use jsonp, or not to use jsonp
                jsonp: false
            };

            this.options = $.extend({},defaults, options);

            return this;
        }

        ServiceBase.prototype._ajaxPost = function(options, callback) {
            options.type = options.type || "POST";
            return this._ajaxGet(options, callback);
        };

        ServiceBase.prototype._ajaxGet = function(options, callback) {
            options.type = options.type || "GET";
            options.dataType = options.dataType || this._resolveDataType();
            options.resolvedUrl = options.resolvedUrl || this._resolveUrl(options.url);

            options.success = options.success || function(results) {
                return callback(null, results);
            };

            options.error = options.error || function(xhr, textStatus, errorThrown) {
                var err = new Error("Failed in request: " + url);
                err.xhr = xhr;
                err.textStatus = textStatus;
                err.errorThrown = err.errorThrown;
                console.error(err);
                return callback(err);
            };

            return $.ajax(options);
        };

        ServiceBase.prototype._resolveUrl = function (urlSuffix) {
            return this.options.serviceBaseUrl + this.options.serviceContext + urlSuffix;
        };

        ServiceBase.prototype._resolveDataType = function () {
            return this.options.jsonp ? "jsonp" : "json";
        };

        return ServiceBase;
    }
);