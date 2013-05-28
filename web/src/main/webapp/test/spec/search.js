
describeComponent('search/search', function(Search) {

    beforeEach(function() {
        setupComponent();
    });

    describe('#onFormSearch', function() {

        it("should trigger 'search'", function() {

            var triggerSpy = sinon.spy( this.component, 'trigger'),
                evt = new sinon.Event();

            this.component.select('searchQuerySelector').val('query');

            this.component.onFormSearch(evt);

            expect(evt.defaultPrevented).to.equal(true);

            triggerSpy.should.have.been.calledWith('search');
        });

        // TODO: mock UCD and require.config.paths with mock

    });

});
