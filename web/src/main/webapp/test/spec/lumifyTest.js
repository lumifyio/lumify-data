
define(['app'], function(app) {

    // Disable debug mode
    require(['flight/tools/debug/debug'], function(d) { d.enable(false); });

    describe('app', function() {

        it('should exist', function() {
            expect(app).to.be.a('function');
        });

    });
});
