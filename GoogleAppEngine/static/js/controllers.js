'use strict';

App.controller('HomeCtrl', [
    '$scope',
    function ($scope) {

    }
]);

App.controller('GooglePlusCtrl', [
    '$scope',
    '$window',
    '$location',
    'GooglePlusFactory',
    'ngProgress',
    function ($scope, $window, $location, GooglePlusFactory, ngProgress) {
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
        $scope.signOut = GooglePlusFactory.signOut;

        $scope.signInCallback = function (authResult) {
            console.log(authResult);
            if (authResult['status']['signed_in']) {
                $scope.signedIn = true;
                $location.path('/fridge');
            } else {
                $scope.signedIn = false;
                $location.path('/');
                // Update the app to reflect a signed out user
                // Possible error values:
                //   "user_signed_out" - User is signed-out
                //   "access_denied" - User denied access to your app
                //   "immediate_failed" - Could not automatically log in the user
            }
            ngProgress.complete();
        };

        $scope.start = function () {
            GooglePlusFactory.renderSignInButton($scope);
        };

        $scope.start();
    }
]);

App.controller('FridgeCtrl', [
    '$scope',
    function ($scope) {
        $scope.fridgeItems = [
            { name: 'cupcake', expiryDate: '2014-10-10'},
            { name: 'doughnut', expiryDate: '2014-08-10'},
            { name: 'eclair', expiryDate: '2014-08-10'},
            { name: 'froyo', expiryDate: '2014-08-10'},
            { name: 'gingerbread', expiryDate: '2014-08-10'},
            { name: 'honeycomb', expiryDate: '2014-08-10'},
            { name: 'ice cream sandwich', expiryDate: '2014-08-10'},
            { name: 'jelly bean', expiryDate: '2014-08-10'},
            { name: 'kitkat', expiryDate: '2014-08-10'},
            { name: 'lollipop', expiryDate: '2014-08-10'}
        ];

        //$scope.isFridgeEmpty = $scope.fridgeItems.length === 0;
        $scope.isFridgeEmpty = true;
    }
]);
