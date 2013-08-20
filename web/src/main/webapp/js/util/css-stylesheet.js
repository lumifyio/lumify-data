
define([], function() {

    var sheet = (function() {
        var style = document.createElement("style");

        // WebKit hack :(
        style.appendChild(document.createTextNode(""));

        document.head.appendChild(style);
        return style.sheet;
    })();

    var index = 0;
    function addCSSRule(selector, rules) {
        if (sheet.insertRule) {
            sheet.insertRule(selector + "{" + rules + "}", index++);
        } else {
            sheet.addRule(selector, rules, index++);
        }
    }

    return {
        addRule: function(selector, definition) {
            addCSSRule(selector, definition);
        }
    };
});
