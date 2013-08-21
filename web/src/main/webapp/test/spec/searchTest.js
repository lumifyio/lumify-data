
describeComponent('search/search', function(Search) {

    beforeEach(function() {
        setupComponent();
        this.component.entityService.concepts = function(callback) {
            callback(undefined, {children:[]});
        };
    });

    describe('#onFormSearch', function() {

        it("should trigger 'search'", function(done) {

            var query = 'query';

            this.component.trigger = function(event, options) {
                expect(event).to.equal('search');
                expect(options.query).to.equal(query);

                done();
            };

            var evt = new sinon.Event();

            this.component.select('searchQuerySelector').val(query);
            this.component.onFormSearch(evt);

            expect(evt.defaultPrevented).to.equal(true);
        });

    });

    describe('#doSearch', function() {

        it("should call 'ucd.artifactSearch' and 'ucd.entitySearch'", function(done) {
            var query = { query: 'query' };
            var evt = new sinon.Event();
            var artifactSearchQuery = null;
            var entitySearchQuery = null;

            this.component.ucd.artifactSearch = function(query) {
                artifactSearchQuery = query;
            };
            this.component.ucd.graphVertexSearch = function(query) {
                entitySearchQuery = query;
            };

            this.component.entityService.concepts = function(callback) {

                callback(undefined, { children:[] });

                expect(artifactSearchQuery).not.to.be.null;
                expect(artifactSearchQuery.query).to.equal(query.query);
                expect(entitySearchQuery).not.to.be.null;
                expect(entitySearchQuery.query).to.equal(query.query);
                done();
            };

            this.component.doSearch(evt, query);

        });

    });
});
