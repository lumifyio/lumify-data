
define(['atmosphere'],
    function() {
        function ServiceBase(options) {
            options = options || {};

			if (!document.$socket) {
				document.$socket = $.atmosphere;
			}

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

		ServiceBase.prototype.getSocket = function () {
			return document.$socket;
		};

        ServiceBase.prototype.socketPush = function(data) {
            data.sourceId = document.subSocketId;
            return document.$subSocket.push(JSON.stringify(data));
        };

        ServiceBase.prototype.subscribe = function (userId, onmessage) {
            var req = {
                url: "/messaging/",
                transport: 'websocket',
                fallbackTransport: 'long-polling',
                contentType: "application/json",
                trackMessageSize: true,
                shared: true,
                logLevel: 'debug',
                onMessage: function (response) {
                    var data = JSON.parse(response.responseBody);
                    if(data && data.sourceId == document.subSocketId) {
                        return;
                    }
                    onmessage(null, data);
                },
                onError: function (response) {
                    console.error('subscribe error:', response);
                    onmessage(response.error, null);
                }
            };
            console.log('subscribe subscribe:', req);
            document.$subSocket = this.getSocket().subscribe(req);
            document.subSocketId = Date.now();
        };

        ServiceBase.prototype._ajaxPost = function(options, callback) {
            options.type = options.type || "POST";
            return this._ajaxGet(options, callback);
        };

        ServiceBase.prototype._ajaxDelete = function(options, callback) {
            options.type = options.type || "DELETE";
            return this._ajaxGet(options, callback);
        };

        ServiceBase.prototype._ajaxGet = function(options, callback) {
            options.type = options.type || "GET";
            options.dataType = options.dataType || this._resolveDataType();
            options.resolvedUrl = options.resolvedUrl || this._resolveUrl(options.url);
            callback = callback || function() {};

            options.success = options.success || function(results) {
                return callback(null, results);
            };

            options.error = options.error || function(xhr, textStatus, errorThrown) {
                if (textStatus === 'abort') return;
                var err = new Error("Failed in request: " + options.resolvedUrl);
                err.xhr = xhr;
                err.textStatus = textStatus;
                err.errorThrown = err.errorThrown;
                console.error(err);
                return callback(err);
            };

            return $.ajax(options);
        };

		ServiceBase.prototype._unsubscribe = function (url) {
			this.getSocket().unsubscribeUrl(url);
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
