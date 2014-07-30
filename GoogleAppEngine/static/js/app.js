var App = angular.module('app', ['ngMaterial', 'ngRoute'])

.config([
  '$routeProvider',
function($routeProvider) {
  $routeProvider.when('/', {
    templateUrl: '/static/template/home.tmpl.html'
  });

  $routeProvider.otherwise('/');

}])

.controller('Ctrl', [
  '$scope',
function($scope) {
  
}])

.controller('HomeCtrl', [
  '$scope',
  '$rootScope',
function($scope, $rootScope) {
  
}])

;
