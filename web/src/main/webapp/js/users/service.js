define(function () {
    'use strict';

    function Service (options) {

        //define deafault options
        var defaults = {

            //the base url to find the UCD service
            serviceBaseUrl: "/",

            //the context of the UCD service
            serviceContext: "",

            //an overall error handler, maybe break this out later?
            errorHandler: this._defaultErrorHandler,

            //to use jsonp, or not to use jsonp
            jsonp: false
        };

        this.options = $.extend({},defaults, options);

        return (this);
    }

    Service.prototype = {
        _resolveUrl: function (urlSuffix) {
            return this.options.serviceBaseUrl + this.options.serviceContext + urlSuffix;
        },

        _resolveDataType: function () {
            return this.options.jsonp ? "jsonp" : "json";
        },

        createChat: function (userId, callback) {
            var url = this._resolveUrl("chat/new");

            $.ajax({
                type: "POST",
                url: url,
                data: {
                    userId: userId
                },
                dataType: this._resolveDataType(),
                success: function(results) {
                    return callback(null, results);
                },
                error: function(xhr, textStatus, errorThrown) {
                    var err = new Error("Failed in request: " + url);
                    err.xhr = xhr;
                    err.textStatus = textStatus;
                    err.errorThrown = err.errorThrown;
                    return callback(err);
                }
            });
        },

        sendChatMessage: function(chatId, message, callback) {
            var url = this._resolveUrl('chat/' + chatId + '/post');

            console.log('sending chat message: chatId:', chatId, 'message:', message);
            $.ajax({
                type: "POST",
                url: url,
                data: {
                    message: message
                },
                dataType: this._resolveDataType(),
                success: function(results) {
                    return callback(null, results);
                },
                error: function(xhr, textStatus, errorThrown) {
                    var err = new Error("Failed in request: " + url);
                    err.xhr = xhr;
                    err.textStatus = textStatus;
                    err.errorThrown = err.errorThrown;
                    return callback(err);
                }
            });
        },

        getOnline: function (callback) {
            var url = this._resolveUrl("user/messages");

            $.ajax({
                url: url,
                dataType: this._resolveDataType(),
                success: function(results) {
                    return callback(null, results);
                },
                error: function(xhr, textStatus, errorThrown) {
                    var err = new Error("Failed in request: " + url);
                    err.xhr = xhr;
                    err.textStatus = textStatus;
                    err.errorThrown = err.errorThrown;
                    return callback(err);
                }
            });
        },

        _defaultErrorHandler: function (request,statusText,err) {
            var message = err ? statusText + " - " + err : statusText;
            console.error(message);
        }

    };

    return Service;
});
