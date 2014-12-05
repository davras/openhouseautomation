(function() {
  var app = angular.module('gAutoArd', ['ui.bootstrap']);

  app.controller('SensorController', ['$http', function($http) {
      var sensors = this;
      sensors.data = [];
      $http.get('/status/display/sensors').success(function(data) {
        console.log("loading sensor data");
        sensors.data = data;
      });
      var sensorpromise = $interval(function() {
        console.log("refreshing sensors data");
        $http.get('/status/display/sensors').success(function(data) {
          sensors.data = data;
        });
      }, 60000);
      $scope.$on('$destroy', function() {
        $interval.cancel(sensorpromise);
      });
    }]);

  app.controller('ForecastController', ['$http', function($http) {
      var forecasts = this;
      forecasts.data = [];
      $http.get('/status/display/forecast').success(function(data) {
        forecasts.data = data;
      });
    }]);

  app.controller('DeviceController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
      var devices = this;
      devices.currenttab = "BLANK";
      devices.list = [];
      devices.data = [];
      devices.lastcomm = 0;
      devices.fastpull = false;

      console.log("loading data");

      // load the tab types first
      $http.get('/status/devicetypelist').success(function(data) {
        devices.list = data;
        console.log("get device types");
      });
      this.setTab = function(newval) {
        devices.data = []; // clear the current data
        devices.currenttab = newval; // save the new tab
        console.log("tab set to " + newval);
        // immediately load the controller data for this device
        $http.get('/status/display/devices?type=' + devices.currenttab).success(function(data) {
          devices.data = data;  // now filled with new tab's data
        });
      };
      this.isSet = function(tabname) {
        //console.log("comparing " + devices.currenttab + "==" + tabname);
        return devices.currenttab == tabname;
      };
      this.getLink = function() {
        return "/control_" + devices.currenttab.toLowerCase() + ".html";
      };
      var devicepromise = $interval(function() {
        //console.log(new Date().getTime() + ">" + (devices.lastcomm + 15000));
        if (devices.fastpull || (new Date().getTime() > (devices.lastcomm + 15000))) {
          $http.get('/status/display/devices?type=' + devices.currenttab).success(function(data) {
            devices.data = data;
            devices.lastcomm = new Date().getTime();
            var len = devices.data.length;
            devices.fastpull = false;
            for (var p = 0; p < len; p++) {
              if (devices.data[p].desiredState !== devices.data[p].actualState) {
                devices.fastpull = true;
              }
            }
          });
        }
      }, 1000);
      $scope.$on('$destroy', function() {
        $interval.cancel(devicepromise);
      });

      $scope.processForm = function(id, state) {
        $scope.id = id;
        $scope.state = state;
        console.log("a button was pushed:" + id + "=>" + state);
        var len = devices.data.length;
        for (var p = 0; p < len; p++) {
          if ($scope.id === devices.data[p].id) {
            devices.data[p].desiredState = $scope.state;
            console.log("set id:" + devices.data[p].id + " to " + $scope.state);
          }
        }
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
          data: {id: $scope.id, desiredState: $scope.state}
        }).success(function() {
          // should check for a 200 return
        });
        devices.fastpull = true;
      };
    }]);
})();