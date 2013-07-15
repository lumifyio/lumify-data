

define([
    'cytoscape'
], function(cytoscape) {
    'use strict';

    var CanvasRenderer = cytoscape.extension( 'renderer', 'canvas' );
    var nodeShapes = {};

    function Renderer( options ) {
        CanvasRenderer.call( this, options );
    }

    var drawInscribedImage = CanvasRenderer.prototype.drawInscribedImage;

    Renderer.prototype = CanvasRenderer.prototype;

    Renderer.prototype.getNodeShape = function(node)
	{
		// TODO only allow rectangle for a compound node?
//		if (node._private.style["width"].value == "auto" ||
//		    node._private.style["height"].value == "auto")
//		{
//			return "rectangle";
//		}

		var shape = node._private.style["shape"].value;

        if ( shape == 'none' ) return 'ellipse';

		if( node.isParent() ){
			if( shape === 'rectangle' || shape === 'roundrectangle' ){
				return shape;
			} else {
				return 'rectangle';
			}
		}

		return shape;
	};

	
    /**
     * Scale image to the size of the node
     */
	Renderer.prototype.drawInscribedImage = function(context, img, node) {
		var r = this;
		var zoom = this.data.cy._private.zoom;
		
		var nodeX = node._private.position.x;
		var nodeY = node._private.position.y;

		var nodeWidth = this.getNodeWidth(node);
		var nodeHeight = this.getNodeHeight(node);
		
		context.save();
			
        // Fit inside node preserving aspect ratio of original image
        var ratioImage = img.width / img.height,
            ratioNode = nodeWidth / nodeHeight,
            fitW, fitH;

        if (ratioNode > ratioImage) {
            fitW = img.width * nodeHeight / img.height;
            fitH = nodeHeight;
        } else {
            fitW = nodeWidth;
            fitH = img.height * nodeWidth / img.width;
        }
	
        // Draw outline and clip to it based on node shape css
        if ( node._private.style.shape.value !== 'none' ) {
            var shape = r.getNodeShape(node);
            nodeShapes[shape].drawPath( context, nodeX, nodeY, fitW, fitH);
            context.clip();
        }

		context.drawImage(img, 
				nodeX - fitW / 2,
				nodeY - fitH / 2,
				fitW,
				fitH);
		
		context.restore();

        if ( node._private.style.shape.value !== 'none' ) {
            context.stroke();
        }
	};


    // COPIED FROM CYTOSCAPE, defined as private variable so inaccesible

	var rendFunc = Renderer.prototype;
	var renderer = rendFunc;	

	// Generate polygon points
	var generateUnitNgonPoints = function(sides, rotationRadians) {
		
		var increment = 1.0 / sides * 2 * Math.PI;
		var startAngle = sides % 2 == 0 ? 
			Math.PI / 2.0 + increment / 2.0 : Math.PI / 2.0;
//		console.log(nodeShapes["square"]);
		startAngle += rotationRadians;
		
		var points = new Array(sides * 2);
		
		var currentAngle;
		for (var i = 0; i < sides; i++) {
			currentAngle = i * increment + startAngle;
			
			points[2 * i] = Math.cos(currentAngle);// * (1 + i/2);
			points[2 * i + 1] = Math.sin(-currentAngle);//  * (1 + i/2);
		}
		
		// The above generates points for a polygon inscribed in a radius 1 circle.
		// Stretch so that the maximum of the height and width becomes 2 so the resulting
		// scaled shape appears to be inscribed inside a rectangle with the given
		// width and height. The maximum of the width and height is used to preserve
		// the shape's aspect ratio.
		
		// Stretch width
		var maxAbsX = 0
		var maxAbsY = 0;
		for (var i = 0; i < points.length / 2; i++) {
			if (Math.abs(points[2 * i] > maxAbsX)) {
				maxAbsX = Math.abs(points[2 * i]);
			}
			
			if (Math.abs(points[2 * i + 1] > maxAbsY)) {
				maxAbsY = Math.abs(points[2 * i + 1]);
			}
		}
		
		var minScaleLimit = 0.0005;
		
		// Use the larger dimension to do the scale, in order to preserve the shape's
		// aspect ratio
		var maxDimension = Math.max(maxAbsX, maxAbsY);
		
		for (var i = 0; i < points.length / 2; i++) {
			if (maxDimension > minScaleLimit) {
				points[2 * i] *= (1 / maxDimension);
				points[2 * i + 1] *= (1 / maxDimension);
			}
		}
		
		return points;
	};


	nodeShapes["ellipse"] = {
		draw: function(context, centerX, centerY, width, height) {
			nodeShapes["ellipse"].drawPath(context, centerX, centerY, width, height);
			context.fill();
			
//			console.log("drawing ellipse");
//			console.log(arguments);
		},
		
		drawPath: function(context, centerX, centerY, width, height) {
			
			//context.save();
			
			context.beginPath();
			context.translate(centerX, centerY);
			context.scale(width / 2, height / 2);
			// At origin, radius 1, 0 to 2pi
			context.arc(0, 0, 1, 0, Math.PI * 2 * 0.999, false); // *0.999 b/c chrome rendering bug on full circle
			context.closePath();

			context.scale(2/width, 2/height);
			context.translate(-centerX, -centerY);
			//context.restore();
			
//			console.log("drawing ellipse");
//			console.log(arguments);
			
		},
		
		intersectLine: function(nodeX, nodeY, width, height, x, y, padding) {
			var intersect = rendFunc.intersectLineEllipse(
				x, y,
				nodeX,
				nodeY,
				width / 2 + padding,
				height / 2 + padding);
			
			return intersect;
		},
		
		intersectBox: function(
			x1, y1, x2, y2, width, height, centerX, centerY, padding) {
			
			return CanvasRenderer.prototype.boxIntersectEllipse(
				x1, y1, x2, y2, padding, width, height, centerX, centerY);
		},
		
		checkPointRough: function(
			x, y, padding, width, height, centerX, centerY) {
		
			return true;
		},
		
		checkPoint: function(
			x, y, padding, width, height, centerX, centerY) {
			
//			console.log(arguments);
			
			x -= centerX;
			y -= centerY;
			
			x /= (width + padding);
			y /= (height + padding);
			
			return (Math.pow(x, 2) + Math.pow(y, 2) <= 1);
		}
	}
	
	nodeShapes["triangle"] = {
		points: generateUnitNgonPoints(3, 0),
		
		draw: function(context, centerX, centerY, width, height) {
			renderer.drawPolygon(context,
				centerX, centerY,
				width, height,
				nodeShapes["triangle"].points);
		},
		
		drawPath: function(context, centerX, centerY, width, height) {
			renderer.drawPolygonPath(context,
				centerX, centerY,
				width, height,
				nodeShapes["triangle"].points);
		},
		
		intersectLine: function(nodeX, nodeY, width, height, x, y, padding) {
			return renderer.polygonIntersectLine(
				x, y,
				nodeShapes["triangle"].points,
				nodeX,
				nodeY,
				width / 2, height / 2,
				padding);
		
			/*
			polygonIntersectLine(x, y, basePoints, centerX, centerY, 
				width, height, padding);
			*/
			
			
			/*
			return renderer.polygonIntersectLine(
				node, width, height,
				x, y, nodeShapes["triangle"].points);
			*/
		},
		
		intersectBox: function(
			x1, y1, x2, y2, width, height, centerX, centerY, padding) {
			
			var points = nodeShapes["triangle"].points;
			
			return renderer.boxIntersectPolygon(
				x1, y1, x2, y2,
				points, width, height, centerX, centerY, [0, -1], padding);
		},
		
		checkPointRough: function(
			x, y, padding, width, height, centerX, centerY) {
		
			return renderer.checkInBoundingBox(
				x, y, nodeShapes["triangle"].points, // Triangle?
					padding, width, height, centerX, centerY);
		},
		
		checkPoint: function(
			x, y, padding, width, height, centerX, centerY) {
			
			return renderer.pointInsidePolygon(
				x, y, nodeShapes["triangle"].points,
				centerX, centerY, width, height,
				[0, -1], padding);
		}
	}
	
	nodeShapes["square"] = {
		points: generateUnitNgonPoints(4, 0),
		
		draw: function(context, centerX, centerY, width, height) {
			renderer.drawPolygon(context,
				centerX, centerY,
				width, height,
				nodeShapes["square"].points);
		},
		
		drawPath: function(context, centerX, centerY, width, height) {
			renderer.drawPolygonPath(context,
				centerX, centerY,
				width, height,
				nodeShapes["square"].points);
		},
		
		intersectLine: function(nodeX, nodeY, width, height, x, y, padding) {
			return renderer.polygonIntersectLine(
					x, y,
					nodeShapes["square"].points,
					nodeX,
					nodeY,
					width / 2, height / 2,
					padding);
		},
		
		intersectBox: function(
			x1, y1, x2, y2,
			width, height, centerX, 
			centerY, padding) {
			
			var points = nodeShapes["square"].points;
			
			return renderer.boxIntersectPolygon(
				x1, y1, x2, y2,
				points, width, height, centerX, 
				centerY, [0, -1], padding);
		},
		
		checkPointRough: function(
			x, y, padding, width, height,
			centerX, centerY) {
		
			return renderer.checkInBoundingBox(
				x, y, nodeShapes["square"].points, 
					padding, width, height, centerX, centerY);
		},
		
		checkPoint: function(
			x, y, padding, width, height, centerX, centerY) {
			
			return renderer.pointInsidePolygon(x, y, nodeShapes["square"].points,
				centerX, centerY, width, height, [0, -1], padding);
		}
	}
	
	nodeShapes["rectangle"] = nodeShapes["square"];
	
	nodeShapes["octogon"] = {};
	
	nodeShapes["roundrectangle"] = nodeShapes["square"];
	
	nodeShapes["roundrectangle2"] = {
		roundness: 4.99,
		
		draw: function(node, width, height) {
			if (width <= roundness * 2) {
				return;
			}
		
			renderer.drawPolygon(node._private.position.x,
				node._private.position.y, width, height, nodeSapes["roundrectangle2"].points);
		},

		intersectLine: function(node, width, height, x, y) {
			return renderer.findPolygonIntersection(
				node, width, height, x, y, nodeShapes["square"].points);
		},
		
		// TODO: Treat rectangle as sharp-cornered for now. This is a not-large approximation.
		intersectBox: function(x1, y1, x2, y2, width, height, centerX, centerY, padding) {
			var points = nodeShapes["square"].points;
			
			/*
			return renderer.boxIntersectPolygon(
				x1, y1, x2, y2,
				points, 
			*/
		}	
	}
	
	/*
	function PolygonNodeShape(points) {
		this.points = points;
		
		this.draw = function(context, node, width, height) {
			renderer.drawPolygon(context,
					node._private.position.x,
					node._private.position.y,
					width, height, nodeShapes["pentagon"].points);
		};
		
		this.drawPath = 
	}
	*/
	
	nodeShapes["pentagon"] = {
		points: generateUnitNgonPoints(5, 0),
		
		draw: function(context, centerX, centerY, width, height) {
			renderer.drawPolygon(context,
				centerX, centerY,
				width, height, nodeShapes["pentagon"].points);
		},
		
		drawPath: function(context, centerX, centerY, width, height) {
			renderer.drawPolygonPath(context,
				centerX, centerY,
				width, height, nodeShapes["pentagon"].points);
		},
		
		intersectLine: function(nodeX, nodeY, width, height, x, y, padding) {
			return renderer.polygonIntersectLine(
				x, y,
				nodeShapes["pentagon"].points,
				nodeX,
				nodeY,
				width / 2, height / 2,
				padding);
		},
		
		intersectBox: function(
			x1, y1, x2, y2, width, height, centerX, centerY, padding) {
			
			var points = nodeShapes["pentagon"].points;
			
			return renderer.boxIntersectPolygon(
				x1, y1, x2, y2,
				points, width, height, centerX, centerY, [0, -1], padding);
		},
		
		checkPointRough: function(
			x, y, padding, width, height, centerX, centerY) {
		
			return renderer.checkInBoundingBox(
				x, y, nodeShapes["pentagon"].points, 
					padding, width, height, centerX, centerY);
		},
		
		checkPoint: function(
			x, y, padding, width, height, centerX, centerY) {
			
			return renderer.pointInsidePolygon(x, y, nodeShapes["pentagon"].points,
				centerX, centerY, width, height, [0, -1], padding);
		}
	}
	
	nodeShapes["hexagon"] = {
		points: generateUnitNgonPoints(6, 0),
		
		draw: function(context, centerX, centerY, width, height) {
			renderer.drawPolygon(context,
				centerX, centerY,
				width, height,
				nodeShapes["hexagon"].points);
		},
		
		drawPath: function(context, centerX, centerY, width, height) {
			renderer.drawPolygonPath(context,
				centerX, centerY,
				width, height,
				nodeShapes["hexagon"].points);
		},
		
		intersectLine: function(nodeX, nodeY, width, height, x, y, padding) {
			return renderer.polygonIntersectLine(
				x, y,
				nodeShapes["hexagon"].points,
				nodeX,
				nodeY,
				width / 2, height / 2,
				padding);
		},
		
		intersectBox: function(
				x1, y1, x2, y2, width, height, centerX, centerY, padding) {
				
			var points = nodeShapes["hexagon"].points;
			
			return renderer.boxIntersectPolygon(
				x1, y1, x2, y2,
				points, width, height, centerX, centerY, [0, -1], padding);
		},
		
		checkPointRough: function(
			x, y, padding, width, height, centerX, centerY) {
		
			return renderer.checkInBoundingBox(
				x, y, nodeShapes["hexagon"].points, 
					padding, width, height, centerX, centerY);
		},
		
		checkPoint: function(
			x, y, padding, width, height, centerX, centerY) {
			
			return renderer.pointInsidePolygon(x, y, nodeShapes["hexagon"].points,
				centerX, centerY, width, height, [0, -1], padding);
		}
	}
	
	nodeShapes["heptagon"] = {
		points: generateUnitNgonPoints(7, 0),
		
		draw: function(context, centerX, centerY, width, height) {
			renderer.drawPolygon(context,
				centerX, centerY,
				width, height,
				nodeShapes["heptagon"].points);
		},
		
		drawPath: function(context, centerX, centerY, width, height) {
			renderer.drawPolygonPath(context,
				centerX, centerY,
				width, height,
				nodeShapes["heptagon"].points);
		},
		
		intersectLine: function(nodeX, nodeY, width, height, x, y, padding) {
			return renderer.polygonIntersectLine(
				x, y,
				nodeShapes["heptagon"].points,
				nodeX,
				nodeY,
				width / 2, height / 2,
				padding);
		},
		
		intersectBox: function(
				x1, y1, x2, y2, width, height, centerX, centerY, padding) {
			
			var points = nodeShapes["heptagon"].points;
			
			return renderer.boxIntersectPolygon(
				x1, y1, x2, y2,
				points, width, height, centerX, centerY, [0, -1], padding);
		},
		
		checkPointRough: function(
			x, y, padding, width, height, centerX, centerY) {
		
			return renderer.checkInBoundingBox(
				x, y, nodeShapes["heptagon"].points, 
					padding, width, height, centerX, centerY);
		},
		
		checkPoint: function(
			x, y, padding, width, height, centerX, centerY) {
			
			return renderer.pointInsidePolygon(x, y, nodeShapes["heptagon"].points,
				centerX, centerY, width, height, [0, -1], padding);
		}
	}
	
	nodeShapes["octagon"] = {
		points: generateUnitNgonPoints(8, 0),
		
		draw: function(context, centerX, centerY, width, height) {
			renderer.drawPolygon(context,
				centerX, centerY,
				width, height,
				nodeShapes["octagon"].points);
		},
		
		drawPath: function(context, centerX, centerY, width, height) {
			renderer.drawPolygonPath(context,
				centerX, centerY,
				width, height,
				nodeShapes["octagon"].points);
		},
		
		intersectLine: function(nodeX, nodeY, width, height, x, y, padding) {
			return renderer.polygonIntersectLine(
				x, y,
				nodeShapes["octagon"].points,
				nodeX,
				nodeY,
				width / 2, height / 2,
				padding);
		},
		
		intersectBox: function(
				x1, y1, x2, y2, width, height, centerX, centerY, padding) {
			
			var points = nodeShapes["octagon"].points;
			
			return renderer.boxIntersectPolygon(
					x1, y1, x2, y2,
					points, width, height, centerX, centerY, [0, -1], padding);
		},
		
		checkPointRough: function(
			x, y, padding, width, height, centerX, centerY) {
		
			return renderer.checkInBoundingBox(
				x, y, nodeShapes["octagon"].points, 
					padding, width, height, centerX, centerY);
		},
		
		checkPoint: function(
			x, y, padding, width, height, centerX, centerY) {
			
			return renderer.pointInsidePolygon(x, y, nodeShapes["octagon"].points,
				centerX, centerY, width, height, [0, -1], padding);
		}
	};
	
	var star5Points = new Array(20);
	{
		var outerPoints = generateUnitNgonPoints(5, 0);
		var innerPoints = generateUnitNgonPoints(5, Math.PI / 5);
		
//		console.log(outerPoints);
//		console.log(innerPoints);
		
		// Outer radius is 1; inner radius of star is smaller
		var innerRadius = 0.5 * (3 - Math.sqrt(5));
		innerRadius *= 1.57;
		
		for (var i=0;i<innerPoints.length/2;i++) {
			innerPoints[i*2] *= innerRadius;
			innerPoints[i*2+1] *= innerRadius;
		}
		
		for (var i=0;i<20/4;i++) {
			star5Points[i*4] = outerPoints[i*2];
			star5Points[i*4+1] = outerPoints[i*2+1];
			
			star5Points[i*4+2] = innerPoints[i*2];
			star5Points[i*4+3] = innerPoints[i*2+1];
		}
		
//		console.log(star5Points);
	}
	
	nodeShapes["star5"] = {
		points: star5Points,
		
		draw: function(context, centerX, centerY, width, height) {
			renderer.drawPolygon(context,
				centerX, centerY,
				width, height,
				nodeShapes["star5"].points);
		},
		
		drawPath: function(context, centerX, centerY, width, height) {
			renderer.drawPolygonPath(context,
				centerX, centerY,
				width, height,
				nodeShapes["star5"].points);
		},
		
		intersectLine: function(nodeX, nodeY, width, height, x, y, padding) {
			return renderer.polygonIntersectLine(
				x, y,
				nodeShapes["star5"].points,
				nodeX,
				nodeY,
				width / 2, height / 2,
				padding);
		},
		
		intersectBox: function(
				x1, y1, x2, y2, width, height, centerX, centerY, padding) {
			
			var points = nodeShapes["star5"].points;
			
			return renderer.boxIntersectPolygon(
					x1, y1, x2, y2,
					points, width, height, centerX, centerY, [0, -1], padding);
		},
		
		checkPointRough: function(
			x, y, padding, width, height, centerX, centerY) {
		
			return renderer.checkInBoundingBox(
				x, y, nodeShapes["star5"].points, 
					padding, width, height, centerX, centerY);
		},
		
		checkPoint: function(
			x, y, padding, width, height, centerX, centerY) {
			
			return renderer.pointInsidePolygon(x, y, nodeShapes["star5"].points,
				centerX, centerY, width, height, [0, -1], padding);
		}
	};
    return Renderer;
});
