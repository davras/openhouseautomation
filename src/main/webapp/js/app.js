(function() {
  var app = angular.module('gAutoArd', ['ui.bootstrap']);

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
      processForm = function() {
        console.log("a button was pushed!");
        $http.post('/status/display/devices', devices.data)
                .success(function(data) {
                  //wtf goes here?

                });
      };
    }]);
  app.controller('DeviceTypeList', ['$http', function($http) {
      var devicetypelist = this;
      devicetypelist.data = [];
      $http.get('/status/devicetypelist').success(function(data) {
        devicetypelist.data = data;
        console.log(devicetypelist.data[0].name);
      });
    }]);
})();