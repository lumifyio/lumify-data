require([
    'jquery',
    'https://igw-us.s3-us-gov-west-1.amazonaws.com/gateway-banner/gateway-banner.js'
], function($, Banner) {
    $('#app')
        .css({ top: '40px', 'border-top': '1px solid #ccc' })
        .before('<div class="gateway-banner"></div>');

    Banner.init({
        element: '.gateway-banner',
        gatewayUrl: 'https://www.igw.us'
    });
});
