
define([
    'flight/lib/component',
    'underscore',
    'tpl!./detail'
], function( defineComponent, _, template) {

    'use strict';

    return defineComponent(Detail);

    function Detail() {


        this.defaultAttrs({
            mapCoordinatesSelector: '.map-coordinates',
            detailTypeContentSelector: '.type-content'
        });

        this.after('initialize', function() {
            this.on('click', {
                mapCoordinatesSelector: this.onMapCoordinatesClicked
            });

            this.on(document, 'searchResultSelected', this.onSearchResultSelected);
            this.on(document, 'loadRelatedSelected', this.onLoadRelatedSelected);
            
            this.$node.html(template({}));
        });


        this.onMapCoordinatesClicked = function(evt, data) {
            evt.preventDefault();
            var $target = $(evt.target);
            data = {
                latitude: $target.attr('latitude'),
                longitude: $target.attr('longitude')
            };
            this.trigger('mapCenter', data);
        };


        this.onSearchResultSelected = function(evt, data) {
            if ($.isArray(data) && data.length === 1) {
                data = data[0];
            }

            if (this.typeContentModule) {
                this.typeContentModule.teardownAll();
            }

            if ( !data || data.length === 0 ) {
                return;
            }

            var self = this,
                moduleName = $.isArray(data) ? 'multiple' : data.type;

            // Attach specific module based on the object selected
            require([
                'detail/' + moduleName + '/' + moduleName,
            ], function(Module) {
                var node = self.select('detailTypeContentSelector');
                (self.typeContentModule = Module).attachTo(node, { data:data });
            });
        };



       this.onLoadRelatedSelected = function (evt, data){
            if ($.isArray (data) && data.length == 1){
                data = data [0];
            }
            
            if (!data || data.length === 0){
                this.$node.empty ();
                this.currentRowKey = null;
            } else if (data.type == 'entity') {
                this.onLoadRelatedEntitySelected (evt, data);
            } else if (data.type == 'artifact'){
                this.onLoadRelatedArtifactSelected (evt, data);
            } else {
                var message = 'Unhandled type: ' + data.type;
                console.error (message);
                return this.trigger (document, 'error', { message: message });
            }
       };

        this.onLoadRelatedEntitySelected = function (evt, data){
            var self = this;
            new UCD ().getEntityById (data.rowKey, function (err, entity){
                if (err){
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }

                self.loadRelatedEntities (data.rowKey, function (relatedEntities){
                    var entityData = {};
                    entityData.key = entity.key;
                    entityData.relatedEntities = relatedEntities;
                    self.onLoadRelatedItems (data, entityData.relatedEntities);
                });
            });
        };

        this.onLoadRelatedArtifactSelected = function (evt, data){
            var self = this;
            new UCD ().getArtifactById (data.rowKey, function (err, artifact){
                if (err){
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }

                self.loadRelatedTerms (data.rowKey, function (relatedTerms){
                    var termData = {};
                    termData.key = artifact.key;
                    termData.relatedTerms = relatedTerms;
                    self.onLoadRelatedItems (data, termData.relatedTerms);
                });
            });
        };

        this.onLoadRelatedItems = function (originalData, nodes){
            var xOffset = 100, yOffset = 100;
            var x = originalData.originalPosition.x;
            var y = originalData.originalPosition.y;
            this.trigger (document, 'addNodes', {
                nodes: nodes.map (function (relatedItem, index){
                    if (index % 10 === 0) {
                        y += yOffset;
                    }
                    return {
                        title: relatedItem.title,
                        rowKey: relatedItem.rowKey,
                        subType: relatedItem.subType,
                        type: relatedItem.type,
                        graphPosition: {
                            x: x + xOffset * (index % 10 + 1),
                            y: y
                        },
                        selected: true
                    };
                })
            });
        };

        this.loadRelatedEntities = function (key, callback){
            var self = this;
            new UCD().getRelatedEntitiesBySubject (key, function (err, relatedEntities){
                if (err){
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString () });
                }
                console.log ('Related Entities', relatedEntities);
                callback (relatedEntities);
            });
        };

        this.loadRelatedTerms = function (key, callback){
            var self = this;
            new UCD().getRelatedTermsFromArtifact (key, function (err, relatedTerms){
                if (err){
                    console.error ('Error', err);
                    return self.trigger (document, 'error', { message: err.toString() });
                }
                console.log ('Related Terms', relatedTerms);
                callback (relatedTerms);
            });
        };

    }
});
