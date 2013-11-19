
define(['./node'], function( Node ) {

    function Graph( options ) {
        this._options = options || {};
        this.nodeSet = {};
        this.nodes = [];
        this.edges = [];
    }

    Graph.Node = Node;

    Graph.prototype.node = function( idOrNode ) {
        var node = (typeof idOrNode === 'string' ) ? 
            this.nodeSet[ idOrNode ] : idOrNode;
        
        return node;
    };

    Graph.prototype.addNode = function( idOrNode ) {
        var node = (typeof idOrNode === 'string' || !idOrNode ) ? 
            new Node( idOrNode ) : idOrNode;

        if ( this.nodeSet[ node.id ] === undefined ) {

            this.nodeSet[ node.id ] = node;
            this.nodes.push(node);
        }

        return this;
    };

    Graph.prototype.calculateEdges = function( ) {

        var nodes = this.nodes,
            len = nodes.length,
            edges = this.edges,
            edgeSet = {};

        for (var i = 0; i < len; i++) {

            var node = nodes[i],
                connections = node.connections,
                connLength = connections.length;

            for (var c = 0; c < connLength; c++) {

                var destNode = connections[c],
                    key = [node.id, destNode.id].sort().join('|');

                if ( ! edgeSet[key] ) {

                    var edge = {
                        source: node,
                        target: destNode
                    };
                    edgeSet[key] = edge;
                    edges.push(edge);
                }
            }
        }
    };

    Graph.prototype.removeNode = function( node ) {
        
        for (var i = 0; i < this.edges.length; i++) {
            // TODO
            /*
            var edge = this.edges[i];
            if (edge.source.id === node.id) {

                edge.target.connections
            }
            if (edge.source.id === node.id || edge.target.id === node.id) {
            }
            */
        }
        
    };

    Graph.prototype.connect = function( node, nodeToConnect, options ) {
        node = this.node( node );
        nodeToConnect = this.node( nodeToConnect );

        if ( node && nodeToConnect ) {
            node.connect( nodeToConnect, options );

        } else throw new Error("Unable to connect nodes unless they both exist in the graph");

        return this;
    };

    return Graph;
});
