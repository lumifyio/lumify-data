

define([
    'flight/lib/component',
    'cytoscape',
    'tpl!./graph'
], function(defineComponent, cytoscape, template) {
    'use strict';

    return defineComponent(Graph);

    function Graph() {
        var cy = null;

        this.defaultAttrs({
            cytoscapeContainerSelector: '.cytoscape-container',
            emptyGraphSelector: '.empty-graph'
        });

        this.addNode = function(title, info, position) {
            var node = {
                group:'nodes',
                position: position,
            };

            node.data = info;
            node.data.id = info.rowKey;

            if (title.length > 10) {
                title = title.substring(0, 10) + "...";
            }
            node.data.title = title;

            cy.add(node);

            this.select('emptyGraphSelector').hide();
        };

        this.onAddToGraph = function(event, data) {
            var el = $(event.target),
                p = el.offset(),
                c = this.$node.offset(),
                position = {
                    x: p.left - c.left + el.width() / 2.0, 
                    y: p.top - c.top + el.height() / 2.0
                };

            this.addNode(data.text, data.info, position); 
        };

        this.graphSelect = function(event) {
            // TODO: multiple selection is two different events
            this.trigger(document, 'searchResultSelected', event.cyTarget.data());
        };
        this.graphUnselect = function(event) {
            // TODO: send empty event? needs detail to support
        };

        this.graphDrag = function(event) { };

        this.graphGrab = function(event) {
            var p = event.cyTarget.position();
            this.grabPosition = {x:p.x, y:p.y};
        };

        this.graphFree = function(event) {
            var p = event.cyTarget.position(),
                dx = p.x - this.grabPosition.x,
                dy = p.y - this.grabPosition.y,
                distance = Math.sqrt(dx * dx + dy * dy);

            // If the user didn't drag more than a few pixels, select the
            // object, it could be an accidental mouse move
            if (distance < 5) {
                event.cyTarget.select();
            }
        };

        this.after('initialize', function() {
            this.$node.html(template({}));

            this.$node.droppable({
                drop: function( event, ui ) {
                    var draggable = ui.draggable,
                        droppableOffset = $(event.target).offset(),
                        text = draggable.text();

                    this.addNode(text, draggable.parents('li').data('info'), {
                        x: event.clientX - droppableOffset.left,
                        y: event.clientY - droppableOffset.top
                    });
                }.bind(this)
            });

            var $this = this;
            cytoscape({
                showOverlay: false,
                minZoom: 0.5,
                maxZoom: 2,
                container: this.select('cytoscapeContainerSelector').css({height:'100%'})[0],
                style: cytoscape.stylesheet()
                  .selector('node')
                    .css({
                      'content': 'data(title)',
                      'font-family': 'helvetica',
                      'font-size': 14,
                      'text-outline-width': 1,
                      'text-outline-color': 'white',
                      'text-valign': 'bottom',
                      'color': '#999'
                    })
                  .selector(':selected')
                    .css({
                      'background-color': '#0088cc',
                      'line-color': '#000',
                      'color': '#0088cc'
                    })
                  .selector('edge')
                    .css({
                      'width': 2,
                      'target-arrow-shape': 'triangle'
                    }),

                ready: function(){
                    cy = this;

                    // Fix render bug that clears canvas on resize
                    $(document.body).on( "resize", function() { cy.zoom(cy.zoom()); }); 

                    $this.on(document, 'addToGraph', $this.onAddToGraph);

                    cy.on({
                        select: $this.graphSelect.bind($this),
                        unselect: $this.graphUnselect.bind($this),
                        grab: $this.graphGrab.bind($this),
                        free: $this.graphFree.bind($this),
                        drag: $this.graphDrag.bind($this)
                    });
                }
            });
        });
    }

});

