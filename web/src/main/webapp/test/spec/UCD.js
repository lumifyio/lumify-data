define(['ucd/ucd'], function(UCD) {

    describe('UCD', function() {

        it('should exist', function() {
            expect(UCD).to.be.a('function');
        });

		it('should have some defaults', function () {
			var ucd = new UCD({});
			assert.isFalse(ucd.options.jsonp);
			assert.equal(ucd.options.serviceBaseUrl,"/");
		});
		
		it('should have some functions', function () {
			var ucd = new UCD({});
			expect(ucd.artifactSearch).to.be.a("function");
			expect(ucd.entitySearch).to.be.a("function");
			expect(ucd.getArtifactById).to.be.a("function");
			expect(ucd.getEntityById).to.be.a("function");
			assert.equal(ucd._resolveUrl("entity/search"),"/entity/search");
		})

    });

});
