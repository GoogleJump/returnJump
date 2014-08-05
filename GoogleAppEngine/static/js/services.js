'use strict';

App.factory('GooglePlusFactory', [
    '$q',
    function ($q) {
        var factory = {},
            profile = null;

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
            profile = null;
            gapi.auth.signOut();
        };

        factory.setProfile = function () {
            var deferred = $q.defer();

            gapi.client.load('oauth2', 'v2', function (response) {
                gapi.client.oauth2.userinfo.get().execute(function (response) {
                    if (response.code === 401) {
                        deferred.reject();    
                    } else {
                        profile = response;
                        deferred.resolve();
                    }
                });
            });

            return deferred.promise;
        };

        factory.getEmail = function () {
            return (profile === null) ? null: profile.email ;
        };

        return factory;
    }
]);