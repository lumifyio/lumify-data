require([
    'jquery'
], function($) {
    var bannerHeight = '25px';

    $('#app')
        .css({ top: bannerHeight, 'border-top': '1px solid #ccc', bottom: bannerHeight, 'border-bottom': '1px solid #ccc' })
        .before('<div class="classification" id="classification-banner"></div>')
        .after('<div class="classification" id="classification-footer"></div>');

    // TODO: run when displayed data changes
    updateAggregateClassification();

    function updateAggregateClassification() {
        var classifications = getDisplayedClassifications();

        // TODO: call webservice
        var bannerText = classifications[0],
            foregroundColor = 'white',
            backgroundColor = 'pink';

        updateBanners(bannerText, foregroundColor, backgroundColor);
    }

    function getDisplayedClassifications() {
        // TODO: collect visibilities of displayed data
        var classifications = ["SYSTEM//HIGH", "SYSTEM//LOW", ""];

        return classifications;
    }

    function updateBanners(bannerText, foregroundColor, backgroundColor) {
        $('#classification-banner')
            .css({ color: foregroundColor, 'background-color': backgroundColor, height: bannerHeight, 'line-height': bannerHeight })
            .html(bannerText);
        $('#classification-footer')
            .css({ color: foregroundColor, 'background-color': backgroundColor, height: bannerHeight, 'line-height': bannerHeight })
            .html(bannerText);
    }
});
