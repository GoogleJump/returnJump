'use strict';

App.factory('GooglePlusFactory', [
    function () {
        var factory = {};

        factory.renderSignInButton = function (scope) {
            gapi.signin.render('myButton',
                {
                    'callback': scope.signInCallback,
                    'clientid': '849916535788-9t8ki2lmeiivrs2dtiou280gdup3c9et.apps.googleusercontent.com',
                    'scope': 'email',
                    'cookiepolicy': 'single_host_origin'
                }
            );
        };

        factory.signOut = function () {
            gapi.auth.signOut();
        };

        return factory;
    }
]);