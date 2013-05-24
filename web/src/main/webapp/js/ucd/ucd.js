define(function () {
        
			'use strict';
		
			function UCD (options) {
				
				//define deafault options
				var defaults = {
					
					//the base url to find the UCD service
					serviceBaseUrl: "/",
					
					//the context of the UCD service
					serviceContext: "red-dawn-web/",
					
					//an overall error handler, maybe break this out later?
					errorHandler: this._defaultErrorHandler,
					
					//to use jsonp, or not to use jsonp
					jsonp: false
				};
				
				this.options = $.extend({},defaults, options);
				
				return (this);
			}
			
			UCD.prototype = {
				
				artifactSearch: function (query, callback) {
					this._search("artifact",query,callback);
				},
				
				getArtifactById: function (id, callback) {
					this._get("artifact",id,callback);
				},
				
				getRawArtifactById: function (id, callback) {
					//maybe it's an object for future options stuff?
					var i = typeof id == "object" ? id.id : id; 
					
					$.ajax({
						url: this._resolveUrl("artifact/" + i + "/raw"),
						dataType: this._resolveDataType(),
						success: callback
					});
				},
				
				artifactRelationships: function (id, options,callback) {
					_relationships("artifact",id,options,callback);
				},
				
				entitySearch: function (query, callback) {
					this._search("entity",query,callback);
				},
				
				getEntityById: function (id, callback) {
					this._get("entity",id,callback);
				},
				
				getSpecificEntityRelationship: function (e1, e2, callback) {
					$.ajax({
						url: this._resolveUrl("entity/relationship"),
						dataType: this._resolveDataType(),
						data: {
							entity1: e1,
							entity2: e2
						},
						success: callback
					})
				},
				
				entityRelationships: function (id, options,callback) {
					_relationships("entity",id,options,callback);
				},
				
				_get: function (resource, id, callback) {
					//maybe it's an object for future options stuff?
					var i = typeof id == "object" ? id.id : id; 
					
					$.ajax({
						url: this._resolveUrl(resource + "/" + i),
						dataType: this._resolveDataType(),
						success: callback
					});
				},
				
				_relationship: function (resource, id, options, callback) {
					var data = {};
					var success = callback;
					if (callback && $.isFunction(callback)) {
						data = options;
					} else if (options && isFunction(options)) {
						success = options;
					}
					
					$.ajax({
						url: this._resolveUrl(resource + id + "/relationships"),
						dataType: this._resolveDataType(),
						data: data,
						success: success
					});
				},
				
				_resolveUrl: function (urlSuffix) {
					return this.options.serviceBaseUrl + this.options.serviceContext + urlSuffix;
				},
				
				_resolveDataType: function () {
					return this.options.jsonp ? "jsonp" : "json";
				},
				
				_search: function (resource, query, callback) {
					//maybe it's an object for future options stuff?
					var q = typeof query == "object" ? query.q : q;
					
					$.ajax({
						url: this._resolveUrl(resource + "/search"),
						dataType: this._resolveDataType(),
						data: {
							'q' : q
						},
						success: callback
					});
				},
				
				_defaultErrorHandler: function (request,statusText,err) {
					var message = err ? statusText + " - " + err : statusText;
					console.error(message);
				}
				
			}
		

        	return UCD;
    }
)