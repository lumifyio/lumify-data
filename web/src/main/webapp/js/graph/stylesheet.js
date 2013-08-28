
define([
    'cytoscape',
    'service/ontology',
    'util/retina'
], function(cytoscape, OntologyService, retina) {

    var ontologyService = new OntologyService(),
        style = cytoscape.stylesheet();

    return load;

    function apply(concept) {
        style.selector('node.concept-' + concept.id)
             .css({
                 'background-image': concept.glyphIconHref
             });

        if (concept.children) {
            concept.children.forEach(apply);
        }
    }

    function load(styleReady) {

        style.selector('node')
            .css({
                'width': 30 * retina.devicePixelRatio,
                'height': 30 * retina.devicePixelRatio,
                'content': 'data(title)',
                'font-family': 'helvetica',
                'font-size': 18 * retina.devicePixelRatio,
                'text-outline-width': 2,
                'text-outline-color': 'white',
                'text-valign': 'bottom',
                'color': '#999',
                'shape': 'roundrectangle'
            })
            .selector('node.artifact')
            .css({
                'shape': 'rectangle',
                'width': 45 * 1.3 * retina.devicePixelRatio,
                'height': 45 * retina.devicePixelRatio,
                'border-color': '#ccc',
                'border-width': 1
            })
            .selector('node.concept-document')
            .css({
                'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
                'width': 30 * retina.devicePixelRatio,
                'height': 30 * 1.2 * retina.devicePixelRatio
            })
            .selector('node.concept-video')
            .css({
                'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
                'shape': 'movieStrip'
            })
            .selector('node.concept-image')
            .css({
                'background-image': '/img/glyphicons/glyphicons_036_file@2x.png',
            })
            .selector('node.TermMention')
            .css({
                'width': 15 * retina.devicePixelRatio,
                'height': 15 * retina.devicePixelRatio,
                'text-outline-width': 1,
                'font-size': 9 * retina.devicePixelRatio
            })
            .selector('node.entity')
            .css({
                'font-weight': 'bold'
            })
            .selector('node.hasCustomGlyph')
            .css({
                'width': 60 * retina.devicePixelRatio,
                'height': 60 * retina.devicePixelRatio,
            })
            .selector(':selected')
            .css({
                'background-color': '#0088cc',
                'border-color': '#0088cc',
                'line-color': '#000',
                'color': '#0088cc'
            })
            .selector('edge')
            .css({
                'width': 2,
                'target-arrow-shape': 'triangle'
            })
            .selector('edge.label')
                .css({
                'content': 'data(label)',
                'font-size': 12 * retina.devicePixelRatio,
                'color': '#0088cc',
                'text-outline-color': 'white',
                'text-outline-width': 4,
            })
            .selector('edge.temp')
            .css({
                'width': 4,
                'line-color': '#0088cc',
                'line-style': 'dotted',
                'target-arrow-color': '#0088cc'
            });

        ontologyService.concepts(function(err, concepts) {
            concepts.entityConcept.children.forEach(apply);

            styleReady(style);
        });
    }

});
