'use strict';

var App = angular.module('app', ['ngMaterial', 'ngRoute', 'ngProgress']);

App.config([
    '$routeProvider',
    function ($routeProvider) {
        $routeProvider.when('/', {
            templateUrl: '/static/template/home.tmpl.html',
            controller: 'HomeCtrl'
        }).otherwise('/');
    }
]);
