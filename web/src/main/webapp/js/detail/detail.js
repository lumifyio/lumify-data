
define([
    'flight/lib/component',
    'service/ucd',
    'tpl!./artifactDetails',
    'tpl!./entityDetails',
    'tpl!./relationshipDetails'
], function(defineComponent, UCD, artifactDetailsTemplate, entityDetailsTemplate, relationshipDetailsTemplate) {
    'use strict';

    var HIGHLIGHT_STYLES = [
            { name: 'None' },
            { name: 'Subtle Icons', cls:'icons' },
            { name: 'Underline', cls:'underline' },
            { name: 'Colors', cls:'colors' },
            { name: 'Ugly Colors', cls:'uglycolors' }
        ],
        ACTIVE_STYLE = 2;

    return defineComponent(Detail);

    function Detail() {

        this.defaultAttrs({
            mapCoordinatesSelector: '.map-coordinates',
            highlightTypeSelector: '.highlight-options a',
            entitiesSelector: '.entity'
        });

        this.after('initialize', function() {
            this.on('click', {
                mapCoordinatesSelector: this.onMapCoordinatesClicked,
                highlightTypeSelector: this.onHighlightTypeClicked
            });
            this.on(document, 'searchResultSelected', this.onSearchResultSelected);

            this.fixTextSelection();
        });

        this.onMapCoordinatesClicked = function(evt, data) {
            var $target = $(evt.target);
            data = {
                latitude: $target.attr('latitude'),
                longitude: $target.attr('longitude')
            };
            this.trigger('mapCenter', data);
        };

        this.onHighlightTypeClicked = function(evt) {
            var target = $(evt.target),
                li = target.parents('li'),
                ul = li.parent('ul'),
                content = ul.parents('.content');

            ul.find('.checked').not(li).removeClass('checked');
            li.addClass('checked');

            $.each( content.attr('class').split(/\s+/), function(index, item) {
                if (item.match(/^highlight-(.+)$/)) {
                    content.removeClass(item);
                }
            });
            
            var newClass = li.data('cls');
            if (newClass) {
                content.addClass('highlight-' + newClass);
            }
        };

        this.onSearchResultSelected = function(evt, data) {
            if(data.type == 'artifacts') {
                this.onArtifactSelected(evt, data);
            } else if(data.type == 'entities') {
                this.onEntitySelected(evt, data);
            } else if(data.type == 'relationship') {
                this.onRelationshipSelected(evt, data);
            } else {
                var message = 'Unhandled type: ' + data.type;
                console.error(message);
                return this.trigger(document, 'error', { message: message });
            }
        };

        this.onRelationshipSelected = function(evt, data) {
            var self = this;
            this.$node.html("Loading...");
            // TODO show something more useful here.
            console.log('Showing relationship:', data);
            self.$node.html(relationshipDetailsTemplate(data));
        };

        this.onArtifactSelected = function(evt, data) {
            this.openUnlessAlreadyOpen(data, function(finished) {
                var self = this;
                new UCD().getArtifactById(data.rowKey, function(err, artifact) {
                    finished(!err);

                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    console.log('Showing artifact:', artifact);
                    artifact.contentHtml = artifact.Content.highlighted_text || artifact.Content.doc_extracted_text;
                    artifact.contentHtml = artifact.contentHtml.replace(/[\n]+/g, "<br><br>\n");
                    self.$node.html(artifactDetailsTemplate({ artifact: artifact, styles:HIGHLIGHT_STYLES, activeStyle:ACTIVE_STYLE }));
                    self.applyHighlightStyle();

                    self.updateEntityDraggables();
                });
            });
        };

        this.onEntitySelected = function(evt, data) {
            this.openUnlessAlreadyOpen(data, function(finished) {
                var self = this;

                new UCD().getEntityById(data.rowKey, function(err, entity) {
                    finished(!err);

                    if(err) {
                        console.error('Error', err);
                        return self.trigger(document, 'error', { message: err.toString() });
                    }
                    console.log('Showing entity:', entity);
                    self.$node.html(entityDetailsTemplate(entity));
                });

            });
        };

        this.openUnlessAlreadyOpen = function(data, callback) {
            if (this.currentRowKey === data.rowKey) {
                return;
            }

            this.$node.html("Loading...");

            callback.call(this, function(success) {
                this.currentRowKey = success ? data.rowKey : null;
            }.bind(this));
        };


        this.updateEntityDraggables = function() {
            var entities = this.select('entitiesSelector');

            var $this = this;
            entities.draggable({
                helper:'clone',
                revert: 'invalid',
                revertDuration: 250,
                scroll: false,
                zIndex: 100
            });
        };

        this.applyHighlightStyle = function() {
            var newClass = HIGHLIGHT_STYLES[ACTIVE_STYLE].cls;
            if (newClass) {
                this.$node.addClass('highlight-' + newClass);
            }
        };

        this.fixTextSelection = function() {
            var mouseDown = false,
                inside = false;

            // Since we are on top of the graph, don't let mouse events get to
            // the graph but fix case where dragging pane resizebar

            this.on('mousedown', function(evt) {

                // We want propagation if it's an entity in case of drag
                mouseDown = !$(evt.target).is('.entity');
            });

            this.on('mousemove', function(evt) {
                if ( mouseDown && inside ) {
                    evt.stopPropagation();
                }
            });

            this.on('mouseup', function() {
                mouseDown = false;
            });

            this.on('mouseleave', function() {
                inside = false;
                mouseDown = false;
            });

            this.on('mouseenter', function() {
                inside = true;
            });
        };
    }
});
