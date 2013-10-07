
define(['atmosphere'],
    function() {
        'use strict';

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

        ServiceBase.prototype.subscribe = function (config) {
            var req = {
                url: "/messaging/",
                transport: 'websocket',
                fallbackTransport: 'long-polling',
                contentType: "application/json",
                trackMessageSize: true,
                shared: false,
                logLevel: 'debug',
                onOpen: function(response) {
                    if (config.onOpen) config.onOpen.apply(null, arguments);
                },
                onMessage: function (response) {
                    var data = JSON.parse(response.responseBody);
                    if(data && data.sourceId == document.subSocketId) {
                        return;
                    }

                    if (config.onMessage) config.onMessage(null, data);
                },
                onError: function (response) {
                    console.error('subscribe error:', response);
                    if (config.onMessage) config.onMessage(response.error, null);
                }
            };
            document.$subSocket = this.getSocket().subscribe(req);
            document.subSocketId = Date.now();
        };

        ServiceBase.prototype._ajaxPost = function(options) {
            options.type = options.type || "POST";
            return this._ajaxGet(options);
        };

        ServiceBase.prototype._ajaxDelete = function(options) {
            options.type = options.type || "DELETE";
            return this._ajaxGet(options);
        };

        ServiceBase.prototype._ajaxGet = function(options) {
            options.type = options.type || "GET";
            options.dataType = options.dataType || this._resolveDataType();
            options.resolvedUrl = options.resolvedUrl || this._resolveUrl(options.url);

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
