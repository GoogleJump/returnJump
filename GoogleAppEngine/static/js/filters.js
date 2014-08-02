'use strict';

App.filter('truncate', [
    function() {
        return function(input, length) {
            return (input.length <= length) ? input : input.substring(0, length-3) + '...';
        };
    }
]);

App.filter('getDaysGood', [
    function() {
        return function(expiryDate) {
            var today = moment();
            var expiry = moment(expiryDate);

            return expiry.diff(today, 'days');
        };
    }
]);

App.filter('getExpirationNumber', [
    function() {
        return function(timeGood) {
            if (timeGood >= 365) {
                timeGood = timeGood / 365;
            } else if (timeGood >= 28) {
                timeGood = timeGood / 28;
            } else if (timeGood >= 7) {
                timeGood = timeGood / 7;
            }

            return Math.floor(timeGood);
        };
    }
]);

App.filter('getExpirationUnit', [
    function() {
        return function(timeGood) {
            var unit;

            if (timeGood >= 365) {
                timeGood = Math.floor(timeGood / 365);
                unit = "year";
            } else if (timeGood >= 28) {
                timeGood = Math.floor(timeGood / 28);
                unit = "month";
            } else if (timeGood >= 7) {
                timeGood = Math.floor(timeGood / 7);
                unit = "week";
            } else {
                unit = "day";
            }

            unit += (timeGood === 1) ? '' :'s';

            return unit;
        };
    }
]);
