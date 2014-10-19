(function() {
  var app = angular.module('gAutoArd',[]);

  app.controller('SensorController', ['$http', function($http) {
      var sensors = this;
      sensors.data = [];
      $http.get('/status/display/sensors').success(function(data) {
        sensors.data = data;
        console.log(sensors.data[1].name);
      });
    }]);

})();