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

    app.controller('DeviceController', ['$http', function($http) {
        var devices = this;
        devices.data = [];
        $http.get('/status/display/devices').success(function(data) {
          devices.data = data;
          console.log(devices.data[0].name);
        });
    }])
})();