'use strict';

App.controller('HomeCtrl', [
    '$scope',
    function ($scope) {

    }
]);

App.controller('GooglePlusCtrl', [
    '$scope',
    '$window',
    'GooglePlusFactory',
    'ngProgress',
    function ($scope, $window, GooglePlusFactory, ngProgress) {
        ngProgress.color('#ffff00');
        ngProgress.start();

        // Used to keep sign in button in a fixed position
        $scope.width = $window.innerWidth;
        $(window).resize(function(){
            $scope.$apply(function(){
               $scope.width = $window.innerWidth;
            });
        });

        $scope.signedIn = false;

        $scope.signInCallback = function (authResult) {
            console.log(authResult);
            if (authResult['status']['signed_in']) {
                $scope.signedIn = true;
            } else {
                $scope.signedIn = false;
                // Update the app to reflect a signed out user
                // Possible error values:
                //   "user_signed_out" - User is signed-out
                //   "access_denied" - User denied access to your app
                //   "immediate_failed" - Could not automatically log in the user
                console.log('Sign-in state: ' + authResult['error']);
            }
            ngProgress.complete();
        };

        $scope.signOut = GooglePlusFactory.signOut;
        GooglePlusFactory.renderSignInButton($scope);
    }
]);