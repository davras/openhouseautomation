(function() {
  var app = angular.module('gAutoArd', ['ui.bootstrap']);

  app.controller('SensorController', ['$http', function($http) {
      var sensors = this;
      sensors.data = [];
      $http.get('/status/display/sensors').success(function(data) {
        sensors.data = data;
      });
    }]);
  app.controller('ForecastController', ['$http', function($http) {
      var forecasts = this;
      forecasts.data = [];
      $http.get('/status/display/forecast').success(function(data) {
        forecasts.data = data;
      });
    }]);

  app.controller('FanController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
      var fans = this;
      fans.data = [];
      fans.lastcomm = 0;
      fans.fastpull = false;
      console.log("loading fan data");
      $http.get('/status/display/devices?type=WHOLEHOUSEFAN').success(function(data) {
        fans.data = data;
      });

      var fanpromise = $interval(function() {
        if (fans.fastpull || (new Date().getTime() > (fans.lastcomm + 15000))) {
          console.log("refreshing fan data");
          $http.get('/status/display/devices?type=WHOLEHOUSEFAN').success(function(data) {
            fans.data = data;
          });
          fans.lastcomm = new Date().getTime();
          var len = fans.data.length;
          fans.fastpull = false;
          for (var p = 0; p < len; p++) {
            console.log("comparing " + fans.data[p].id + ":" + fans.data[p].desiredState + 
                    " to " + fans.data[p].actualState);
            if (fans.data[p].desiredState !== fans.data[p].actualState) {
              fans.fastpull = true;
            }
            }
            console.log("fan fastpull is " + fans.fastpull);
          }
        }, 1000);
      $scope.$on('$destroy', function() {
        $interval.cancel(fanpromise);
      });
      $scope.processForm = function(id, state) {
        $scope.id = id;
        $scope.state = state;
        console.log("a fan button was pushed:" + id + "=>" + state);
        for (var p = 0; p < len; p++) {
          if ($scope.id === fans.data[p].id) {
            fans.data[p].desiredState = $scope.state;
            console.log("set fan id:" + lights.data[p].id + " to " + $scope.state);
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
        fans.fastpull = true;
      };
    }]);
  app.controller('LightController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
      var lights = this;
      lights.data = [];
      lights.lastcomm = 0;
      lights.fastpull = false;
      console.log("loading lights data");
      // the initial load of data to display
      $http.get('/status/display/devices?type=LIGHTS').success(function(data) {
        lights.data = data;
      });
      // occasional refresh of data
      var promise = $interval(function() {
        if (lights.fastpull || (new Date().getTime() > (lights.lastcomm + 15000))) {
          console.log("refreshing lights data");
          $http.get('/status/display/devices?type=LIGHTS').success(function(data) {
            lights.data = data;
          });
          lights.lastcomm = new Date().getTime();
          var len = lights.data.length;
          lights.fastpull = false;
          for (var p = 0; p < len; p++) {
            console.log("comparing " + lights.data[p].id + ":" + lights.data[p].desiredState +
                    " to " + lights.data[p].actualState);
            if (lights.data[p].desiredState !== lights.data[p].actualState) {
              lights.fastpull = true;
            }
          }
          console.log("lights fastpull is " + lights.fastpull);
        }
      }, 1000);
      $scope.$on('$destroy', function() {
        $interval.cancel(promise);
      });
      // handle UI events from page
      $scope.processForm = function(id, state) {
        $scope.id = id;
        $scope.state = state;
        // TODO handle more than one in the array
        console.log("a light button was pushed:" + id + "=>" + state);
        var len = lights.data.length;
        for (var p = 0; p < len; p++) {
          console.log("comparing " + lights.data[p].id + " to " + $scope.id);
          if ($scope.id === lights.data[p].id || $scope.id === 100) {
            lights.data[p].desiredState = $scope.state;
            console.log("set light id:" + lights.data[p].id + " to " + $scope.state);
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
          // TODO should check for a 200 return and display error message
        });
        // go to fast poll while waiting for state to change
        lights.fastpull = true;
      };
    }]);

  app.controller('AlarmController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
      var alarms = this;
      alarms.data = [];
      alarms.lastcomm = 0;
      alarms.fastpull = false;
      console.log("loading alarms data");
      // the initial load of data to display
      $http.get('/status/display/devices?type=ALARM').success(function(data) {
        alarms.data = data;
      });
      // occasional refresh of data
      var promise = $interval(function() {
        if (alarms.fastpull || (new Date().getTime() > (alarms.lastcomm + 15000))) {
          console.log("refreshing alarms data");
          $http.get('/status/display/devices?type=ALARM').success(function(data) {
            alarms.data = data;
          });
          alarms.lastcomm = new Date().getTime();
          var len = alarms.data.length;
          alarms.fastpull = false;
          for (var p = 0; p < len; p++) {
            console.log("comparing " + alarms.data[p].id + ":" + alarms.data[p].desiredState +
                    " to " + alarms.data[p].actualState);
            if (alarms.data[p].desiredState !== alarms.data[p].actualState) {
              alarms.fastpull = true;
            }
          }
          console.log("alarms fastpull is " + alarms.fastpull);
        }
      }, 1000);
      $scope.$on('$destroy', function() {
        $interval.cancel(promise);
      });
      // handle UI events from page
      $scope.processForm = function(id, state) {
        $scope.id = id;
        $scope.state = state;
        // TODO handle more than one in the array
        console.log("a light button was pushed:" + id + "=>" + state);
        var len = alarms.data.length;
        for (var p = 0; p < len; p++) {
          console.log("comparing " + alarms.data[p].id + " to " + $scope.id);
          if ($scope.id === alarms.data[p].id || $scope.id === 100) {
            alarms.data[p].desiredState = $scope.state;
            console.log("set light id:" + alarms.data[p].id + " to " + $scope.state);
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
          // TODO should check for a 200 return and display error message
        });
        // go to fast poll while waiting for state to change
        alarms.fastpull = true;
      };
    }]);

  app.controller('DeviceTypeList', ['$http', function($http) {
      var devicetypelist = this;
      devicetypelist.data = [];
      devicetypelist.tab = -1;
      this.setTab = function(newval) {
        devicetypelist.tab = newval;
        console.log("tab set to " + newval);
      };
      this.isSet = function(tabnum) {
        //console.log("comparing " + devicetypelist.tab + "===" + tabnum);
        return devicetypelist.tab === tabnum;
      };
      $http.get('/status/devicetypelist').success(function(data) {
        devicetypelist.data = data;
      });
    }]);
})();