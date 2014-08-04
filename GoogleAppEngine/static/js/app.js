'use strict';

var App = angular.module('app', ['ngMaterial', 'ngRoute', 'ngProgress']);

App.config([
    '$routeProvider',
    '$locationProvider',
    function ($routeProvider, $locationProvider) {
        // Routes must be added to the server (main.py) when using html5Mode
        $routeProvider.when('/', {
            templateUrl: '/static/template/home.tmpl.html',
            controller: 'HomeCtrl'
        }).when('/fridge', {
            templateUrl: '/static/template/fridge.tmpl.html',
            controller: 'FridgeCtrl'
        }).otherwise('/');

        $locationProvider.html5Mode(true);
    }
]);
