

define(['service/ucd'], function(UCD) {

    describe('UCD', function() {

        it('should exist', function() {
            expect(UCD).to.be.a('function');
        });

		it('should have some defaults', function () {
			var ucd = new UCD({});
            expect(ucd.options.jsonp).to.equal(false);
            expect(ucd.options.serviceBaseUrl).to.be.equal('/');
		});

		it('should have some functions', function () {
			var ucd = new UCD({});
			expect(ucd.artifactSearch).to.be.a("function");
			expect(ucd.graphVertexSearch).to.be.a("function");
			expect(ucd.getArtifactById).to.be.a("function");
			expect(ucd.getGraphVertexById).to.be.a("function");
            expect(ucd._resolveUrl("graph/vertex/search")).to.be.equal("/graph/vertex/search");
		});

    });

});
