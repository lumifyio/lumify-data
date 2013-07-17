
define([], function() {

    function Retina() {

        var self = this,
            getRatio = function() {
                return 'devicePixelRatio' in window ? devicePixelRatio : 1;
            },
            updateRatio = function(newRatio) {
                self.devicePixelRatio = newRatio;
                $(document).trigger('devicePixelRatioChanged', { devicePixelRatio:newRatio });
            },
            observeRatioChanges = function(callback) {
                if ('matchMedia' in window) {
                    matchMedia('(-webkit-device-pixel-ratio:1)').addListener(callback);
                }
            };


        this.devicePixelRatio = getRatio();
        this.onRatioChange = function() {
            updateRatio(getRatio());
        };
        this.pixelsToPoints = function(position) {
            return {
                x: position.x / self.devicePixelRatio,
                y: position.y / self.devicePixelRatio
            };
        };
        this.pointsToPixels = function(position) {
            return {
                x: position.x * self.devicePixelRatio,
                y: position.y * self.devicePixelRatio
            };
        };

        observeRatioChanges(this.onRatioChange.bind(this));
    }


    return new Retina();
});
