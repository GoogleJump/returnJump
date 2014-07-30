var App = angular.module('app', ['ngMaterial', 'ngRoute']);

App.config([
    '$routeProvider',
    function ($routeProvider) {
        $routeProvider.when('/', {
            templateUrl: '/static/template/home.tmpl.html'
        }).otherwise('/');
    }
]);
