
describeComponent('search/search', function(Search) {

    beforeEach(function() {
        setupComponent();
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

        // TODO: mock UCD and require.config.paths with mock

    });

});
