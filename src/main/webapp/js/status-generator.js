(function(){
    var app = angular.module('status-directives', []);

    app.directive("sensorDescription", function() {
      return {
        restrict: 'E',
        templateUrl: "sensor-description.html"
      };
    });
    app.directive("deviceDescription", function() {
      return {
        restrict: 'E',
        templateURL: "device-description.html"
      };
    });
  })();
