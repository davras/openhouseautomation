(function() {
  var app = angular.module('gAutoArd', ['device-directives']);

  app.controller('SensorDisplay', ['$http', function($http) {
      var sensorlist = this;
      sensorlist.sensors = [];
      $http.get('/status/display/sensors').success(function(data) {
        sensorlist.sensors = data;
      });
    }]);

})();