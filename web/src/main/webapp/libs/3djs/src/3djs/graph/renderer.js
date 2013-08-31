
define([
    'three-trackball',
    './layout/force-directed'
], function( THREE, ForceDirectedLayout ) {

    var requestAnimationFrame = window.requestAnimationFrame ||
        window.mozRequestAnimationFrame ||
        window.webkitRequestAnimationFrame || 
        window.msRequestAnimationFrame;

    function GraphRenderer( domElement, options ) {
        this.domElement = domElement;
        this.options = options || {};
    }

    GraphRenderer.prototype = {
        addEventListener: THREE.EventDispatcher.prototype.addEventListener,
        hasEventListener: THREE.EventDispatcher.prototype.hasEventListener,
        removeEventListener: THREE.EventDispatcher.prototype.removeEventListener,
        dispatchEvent: THREE.EventDispatcher.prototype.dispatchEvent
    };

    GraphRenderer.prototype.renderGraph = function(graph) {
        this.graph = graph;

        this._init();
        this._setupScene();
        this._setupLayout();
    };

    GraphRenderer.prototype.teardown = function() {
        this.teardownEvents();
    };

    GraphRenderer.prototype._init = function () {
        var width = this.domElement.offsetWidth,
            height = this.domElement.offsetHeight,
            camera = new THREE.PerspectiveCamera( 50, width / height, 100, 10000 ),
            renderer = new THREE.WebGLRenderer({ antialias:true }),
            controls = new THREE.TrackballControls( camera, renderer.domElement ),
            projector = new THREE.Projector(),
            mouse = { };

        renderer.setSize( width, height);
        renderer.sortObjects = false;
        renderer.setClearColor(0xffffff);

        this._renderer = renderer;
        this._camera = camera;
        this._controls = controls;

        this._pickingTexture = new THREE.WebGLRenderTarget( width, height );
        this._pickingTexture.generateMipmaps = false;
        this._pickingData = [];

        this._mouse = mouse;

        controls.rotateSpeed = 1.0;
        controls.zoomSpeed = 0.8;
        controls.panSpeed = 0.8;
        controls.noZoom = false;
        controls.noPan = false;
        controls.staticMoving = true;
        controls.dynamicDampingFactor = 0.3;

        var self = this;

        this.domElement.appendChild(renderer.domElement);

        renderer.domElement.addEventListener( 'mousemove', moveHandler);
        renderer.domElement.addEventListener( 'mousedown', downHandler);
        renderer.domElement.addEventListener( 'mouseup', upHandler);
        renderer.domElement.addEventListener( 'click', clickHandler);
        window.addEventListener( 'resize', windowResizeHandler, false );

        self.teardownEvents = function() {
            renderer.domElement.removeEventListener('mousemove', moveHandler);
            renderer.domElement.removeEventListener('mousedown', downHandler);
            renderer.domElement.removeEventListener('mouseup', upHandler);
            renderer.domElement.removeEventListener('click', clickHandler);
            window.removeEventListener('resize', windowResizeHandler);
            controls.teardown();
        };

        function moveHandler(e) {
            mouse.x = e.pageX - self.domElement.offsetLeft;
            mouse.y = e.pageY - self.domElement.offsetTop;

            var dragging = self.dragging;

            if ( dragging ) {

                var x = ( e.clientX / width ) * 2 - 1;
                var y = -( e.clientY / height ) * 2 + 1;

                var vector = new THREE.Vector3( x, y, 0.5 );
                projector.unprojectVector( vector, camera );

                var dir = vector.sub( camera.position ).normalize();
                var ray = new THREE.Raycaster( camera.position, dir );
                var distance = - camera.position.z / dir.z;
                var pos = camera.position.clone().add( dir.multiplyScalar( distance ) );
            
                dragging.layout = {};
                dragging.position.x = pos.x;
                dragging.position.y = pos.y;

                self._layout.recalculate();
            }
        }

        function downHandler(e) { 
            if (self.currentNodeId) {
                // Disable dragging nodes
                //self.dragging = self.graph.node(self.currentNodeId);
                //controls.noZoom = controls.noRotate = controls.noZoom = true;
            }
            self.mousedown = true;

            self.dispatchEvent( { type: 'node_mousedown', content: self.currentNodeId } );
        }
        function upHandler(e) { 
            if (self.dragging) {
                self.graph.removeNode(self.dragging);
                self.dragging = undefined;
            }
            controls.noZoom = controls.noRotate = controls.noZoom = false;
            self.mousedown = false;
            self.dispatchEvent( { type: 'node_mouseup', content: self.currentNodeId } );
        }
        function clickHandler(e) { 
            controls.noZoom = controls.noRotate = controls.noZoom = false;
            self.mousedown = false;
            self.dispatchEvent( { type: 'node_click', content: self.currentNodeId } );
        }
        function windowResizeHandler() {
            width = self.domElement.offsetWidth;
            height = self.domElement.offsetHeight;

            camera.aspect = width / height;
            camera.updateProjectionMatrix();

            self._pickingTexture = new THREE.WebGLRenderTarget( width, height );
            self._pickingTexture.generateMipmaps = false;

            renderer.setSize( width, height );
        }
    };

    

    GraphRenderer.prototype._updateGeometry = function () {
        //this.geometry.verticesNeedUpdate = true;
        //this._sprites[0].verticesNeedUpdate = true;
        this.pickingGeometry.verticesNeedUpdate = true;
        this.lineGeometry.verticesNeedUpdate = true;
    };


    
    GraphRenderer.prototype.addToRenderLoop = function () {
        var renderer = this._renderer,
            scene = this._scene,
            camera = this._camera,
            controls = this._controls,
            mouse = this._mouse,
            pickedId = null,
            self = this;

        function pick() {
            if ( ! mouse.x || ! mouse.y ) {
                return;
            }
            renderer.render( self._pickingScene, camera, self._pickingTexture );

            var gl = renderer.getContext();

            //read the pixel under the mouse from the texture
            var pixelBuffer = new Uint8Array( 4 );
            gl.readPixels( mouse.x, self._pickingTexture.height - mouse.y, 1, 1, gl.RGBA, gl.UNSIGNED_BYTE, pixelBuffer );

            //interpret the pixel as an ID
            var id = ( pixelBuffer[0] << 16 ) | ( pixelBuffer[1] << 8 ) | ( pixelBuffer[2] );
            if (id !== pickedId ) {
                pickedId = id;
                var nodeId = self._pickingData[ id ];
                if (nodeId) {
                    self.currentNodeId = nodeId;
                    self.dispatchEvent( { type: 'node_hover', content: nodeId } );
                } else {
                    self.currentNodeId = undefined;
                    self.dispatchEvent( { type: 'node_hover' } );
                }
            }
        }

        function render() {
            requestAnimationFrame(render);

            var needsUpdateGeometry = false;
            if ( !self._layout.finished ) {
                self._layout.generate();
                needsUpdateGeometry = true;
            }

            if (self.dragging) {
                needsUpdateGeometry = true;
                self.dragging.layout = {};
            }

            controls.update();

            if (needsUpdateGeometry) {
                self._updateGeometry();
            }

            if (!self.mousedown) {
                pick();
            }

            renderer.render(self._scene, camera);

            if (self.stats) {
                self.stats.update();
            }
        }
        render();
    };




    GraphRenderer.prototype._setupScene = function () {

        this._scene = new THREE.Scene();
        this._pickingScene = new THREE.Scene();

        var nodes = this._createNodes();
        var lines = this._createLines();

        this._scene.add( lines );
        //this._scene.add( nodes );
        for (var i = 0; i < nodes.length; i++) {
            this._scene.add(nodes[i]);
        }

        this._camera.position.z = 2400;
    };

    GraphRenderer.prototype.updateGraph = function () {
        var self = this;

        clearTimeout(this._layouttimer);
        this._setupScene();
        this._layout.stop_calculating();

        self._layouttimer = setTimeout(function() {
            self._setupLayout();
        }, 500);
    };


    GraphRenderer.prototype._createNodes = function () {

        var nodes = this.graph.nodes,
            len = nodes.length,
            geometry = new THREE.Geometry(),
            material = new THREE.ParticleBasicMaterial({ 
                size: 120,
                vertexColors: true,
                transparent: true 
            }),
            pickingGeometry = new THREE.Geometry(),
            pickingMaterial = new THREE.ParticleBasicMaterial({ 
                size: 100,
                vertexColors: true
            }),
            pickingData = this._pickingData,
            colors = [],
            pickingColors = [],
            sprites = [];


        for (var i = 0; i < len; i++) {

            var vertex = nodes[i].position;
            if ( !vertex ) {
                vertex = new THREE.Vector3();

                vertex.x = 500 * Math.random() - 250;
                vertex.y = 500 * Math.random() - 250;
                vertex.z = 500 * Math.random() - 250;

                nodes[i].position = vertex;
            }

            var texture = THREE.ImageUtils.loadTexture( nodes[i].data.icon );
            texture.needsUpdate = true;

            var spriteMaterial = new THREE.SpriteMaterial({
                map: texture,
                useScreenCoordinates:false,
                alignment: THREE.SpriteAlignment.center,
                color: 0xffffff  
            });
            var sprite = new THREE.Sprite(spriteMaterial);
            sprite.scale.set( nodes[i].data.iconWidth, nodes[i].data.iconHeight, 1.0 ); // imageWidth, imageHeight
            sprite.position = nodes[i].position;


            var canvas = document.createElement('canvas');
            canvas.width = 400;
            canvas.height = nodes[i].data.iconHeight + 125;
            var context = canvas.getContext('2d');
            var fontsize = 40;
            context.font = fontsize + "px Helvetica";
            var metrics = context.measureText( nodes[i].data.label );
            var textWidth = metrics.width;
            context.fillStyle = "rgba(128, 128, 128, 1.0)";
            context.fillText(nodes[i].data.label, canvas.width / 2 - textWidth / 2, canvas.height - fontsize * 0.5);

            

            // canvas contents will be used for a texture
            var textTexture = new THREE.Texture(canvas); 
            textTexture.needsUpdate = true;

            var textSpriteMaterial = new THREE.SpriteMaterial({
                map: textTexture,
                useScreenCoordinates:false,
                alignment: THREE.SpriteAlignment.center,
                color: 0xffffff  
            });
            var textSprite = new THREE.Sprite(textSpriteMaterial);
            textSprite.scale.set( canvas.width, canvas.height, 1.0 );
            textSprite.position = new THREE.Vector3(0, 0, 0);
            sprite.add(textSprite);

            sprites.push(sprite);

            //sprites.push(textSprite);




            //geometry.vertices.push( vertex );

            //colors[ i ] = new THREE.Color( 0xffffff );
            //colors[ i ].setHSL( ( vertex.x + 250 ) / len, 1, 0.5 );

            pickingGeometry.vertices.push( vertex );

            pickingColors[ i ] = new THREE.Color( i );
            pickingData[i] = nodes[i].id;
        }
        //geometry.colors = colors;
        pickingGeometry.colors = pickingColors;

        this._pickingScene.add( 
            new THREE.ParticleSystem( pickingGeometry, pickingMaterial )
        );

        //this._scene.add(
            //new THREE.ParticleSystem( pickingGeometry, pickingMaterial )
        //);

        this._sprites = sprites;

        //material.color.setHSL( 1.0, 0.2, 0.7 );

        //this.geometry = geometry;
        this.pickingGeometry = pickingGeometry;

        //return new THREE.ParticleSystem( geometry, material );

        return sprites;
    };



    GraphRenderer.prototype._createLines = function () {

        this.graph.calculateEdges();

        var edges = this.graph.edges,
            len = edges.length,
            geometry = new THREE.Geometry(),
            material = new THREE.LineBasicMaterial( { color: 0x000000 } );

        for (var i = 0; i < len; i++) {
            var edge = edges[i];

            geometry.vertices.push( edge.source.position );
            geometry.vertices.push( edge.target.position );
        }

        this.lineGeometry = geometry;
    
        return new THREE.Line( geometry, material );
    };
    


    GraphRenderer.prototype._setupLayout = function () {
        this._layout = new ForceDirectedLayout( this.graph, {
            iterations: 5000,
            attraction: 5,
            repulsion: 10,
            width: this.domElement.offsetWidth * 0.1,
            height: this.domElement.offsetHeight * 0.1,
            layout: '3d'
        });

        this._layout.init();
    };



    GraphRenderer.prototype.showStats = function () {
        var self = this;

        require( ['three-stats'], function( Stats ) {

          var stats = new Stats();
          stats.domElement.style.position = 'absolute';
          stats.domElement.style.top = '0px';
          self.domElement.appendChild( stats.domElement );
          self.stats = stats;
        });
    };


    return GraphRenderer;
});
