
define([
    './nodeShapes'
], function(nodeShapes) {
    'use strict';

    var movieStrip = Object.create(nodeShapes.square);

    movieStrip.drawBackground = function(context, nodeX, nodeY, fitW, fitH) {
        var padding = 2,
            total = 5,
            height = Math.floor(fitH / total);

        context.fillRect(nodeX - fitW / 2 - 10, nodeY - fitH / 2 - 1, fitW + 20, fitH + 2);
        context.fillStyle = 'white';
        for (var j = 0; j < 2; j++) {
            for (var i = 0; i < total; i++) {
                context.fillRect( 
                        nodeX - fitW / 2 - 10 + padding + j * (fitW + 11), 
                        nodeY - fitH / 2 + padding + (i * height), 
                        5, height - padding * 2);
            }
        }
    };

    nodeShapes.movieStrip = movieStrip;

    return movieStrip;
});
