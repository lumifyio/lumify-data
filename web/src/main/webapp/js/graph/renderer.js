

define([
    'cytoscape'
], function(cytoscape) {
    'use strict';

    var CanvasRenderer = cytoscape.extension( 'renderer', 'canvas' );

    function Renderer( options ) {
        CanvasRenderer.call( this, options );
    }

    var drawInscribedImage = CanvasRenderer.prototype.drawInscribedImage;

    Renderer.prototype = CanvasRenderer.prototype;
	
    /**
     * Scale image to the size of the node
     */
	Renderer.prototype.drawInscribedImage = function(context, img, node) {

        var drawImage = context.drawImage,
            nodeWidth = this.getNodeWidth(node),
            nodeHeight = this.getNodeHeight(node);

        // CanvasRenderer calls drawImage with original image dimensions, we
        // want to scale those to the nodes dimensions. 
        //
        // Hack to redefine drawImage just for this call
        context.drawImage = function(img, x, y, w, h) {
            x += img.width / 2 - nodeWidth / 2;
            y += img.height / 2 - nodeHeight / 2;
            drawImage.call(context, img, x, y, nodeWidth, nodeHeight);
        };

        drawInscribedImage.apply( this, arguments );

        // Restore the drawImage call
        context.drawImage = drawImage;
	};

    return Renderer;
});
