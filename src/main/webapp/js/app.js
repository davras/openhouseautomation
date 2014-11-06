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

  app.controller('FanController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
      var fan = this;
      fan.data = [];
      fan.myInterval = 10000;
      var promise = $interval(function() {
        console.log("refreshing data");
        $http.get('/status/display/devices?type=WHOLEHOUSEFAN').success(function(data) {
          fan.data = data;
        });
      }, fan.myInterval);
      $scope.$on('$destroy', function() {
        $interval.cancel(promise);
      });
      $scope.processForm = function() {
        console.log("a button was pushed:" + $scope.fan.data[0].desiredState);
        $http({
          method: 'post',
          url: '/status/controller/update',
          headers: {'Content-Type': 'application/x-www-form-urlencoded'},
          transformRequest: function(obj) {
            var str = [];
            for (var p in obj)
              str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
            return str.join("&");
          },
          data: {id: $scope.fan.data[0].id, desiredState: $scope.fan.data[0].desiredState}
        }).success(function() {
          // should check for a 200 return
        });
      };
    }]);
  app.controller('LightController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
      var lights = this;
      lights.data = [];
      lights.myInterval = 10000;
      var promise = $interval(function() {
        console.log("refreshing lights data");
        $http.get('/status/display/devices?type=LIGHTS').success(function(data) {
          lights.data = data;
        });
      }, lights.myInterval);
      $scope.$on('$destroy', function() {
        $interval.cancel(promise);
      });
      $scope.processForm = function() {
        // TODO handle more than one in the array
        console.log("a button was pushed:" + $scope.lights.data[0].desiredState);
        $http({
          method: 'post',
          url: '/status/controller/update',
          headers: {'Content-Type': 'application/x-www-form-urlencoded'},
          transformRequest: function(obj) {
            var str = [];
            for (var p in obj)
              str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
            return str.join("&");
          },
          data: {id: $scope.fan.data[0].id, desiredState: $scope.fan.data[0].desiredState}
        }).success(function() {
          // should check for a 200 return
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