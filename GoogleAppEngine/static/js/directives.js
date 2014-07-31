'use strict';

App.directive('frijLogo', [
    function () {
        var directive = {};

        directive.template = 'frij';

        directive.link = function link(scope, element, attrs) {
            var style = 'font-family: \'Syncopate\', sans-serif;' + ((attrs.size === undefined) ? '' : ' font-size: ' + attrs.size + 'px;');
            element.attr('style', style)
        };

        return directive;
    }
]);