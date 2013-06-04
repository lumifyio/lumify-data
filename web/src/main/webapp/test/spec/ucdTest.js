

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
			expect(ucd.entitySearch).to.be.a("function");
			expect(ucd.getArtifactById).to.be.a("function");
			expect(ucd.getEntityById).to.be.a("function");
            expect(ucd._resolveUrl("entity/search")).to.be.equal("/entity/search");
		});

    });

});
