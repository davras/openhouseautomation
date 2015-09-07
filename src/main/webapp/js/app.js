(function() {
  //lastmod 12/6/2014 2:11pm
  var app = angular.module('gAutoArd', ['ui.bootstrap']);
  /**
   * @const
   */
  SENSOR_DATA_URL = "/status/display/sensors";
  /**
   * @const
   */
  FORECAST_DATA_URL = "/status/display/forecast";
  /**
   * @const
   */
  DEVICE_TYPE_LIST_URL = "/status/devicetypelist";
  /**
   * @const
   */
  DEVICE_TYPESPECIFIC_LIST_URL = "/status/display/devices?type=";
  /**
   * @const
   */
  SCENE_LIST_URL = "/status/display/scenes";
  /**
   * @const
   */
  CONTROLLER_UPDATE_URL = "/status/controller/update";
  /**
   * @const
   */
  SCENE_STATUS_UPDATE_URL = "/status/scenes/set";
  /**
   * @const
   */
  LOGIN_STATUS_URL = "/status/login";

  app.controller('LoginController', ['$scope', '$http', function($scope, $http) {
      var userdetails = this;
      userdetails.username = null;
      userdetails.redirecturl = null;
      $http.get(LOGIN_STATUS_URL).success(function(data) {
        userdetails = data;
        console.log("loading user login: ");
        console.log(userdetails);
      });
      this.getLogin = function() {
        if (userdetails.username != null)
          return userdetails.username;
        if (userdetails.redirecturl != null)
          return userdetails.redirecturl;
        return "ERROR";
      };
    }]);

  app.controller('SensorController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
      var sensors = this;
      sensors.data = [];
      $http.get(SENSOR_DATA_URL).success(function(data) {
        console.log("loading sensor data");
        sensors.data = data;
      });
      var sensorPromise = $interval(function() {
        console.log("refreshing sensors data");
        $http.get(SENSOR_DATA_URL).success(function(data) {
          sensors.data = data;
        });
      }, 60000);
      $scope.$on('$destroy', function() {
        $interval.cancel(sensorPromise);
      });
    }]);

  app.controller('ForecastController', ['$http', function($http) {
      var forecasts = this;
      forecasts.data = [];
      $http.get(FORECAST_DATA_URL).success(function(data) {
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

      console.log("loading devices data");

      // load the tab types first
      $http.get(DEVICE_TYPE_LIST_URL).success(function(data) {
        devices.list = data;
        console.log("get device types");
      });
      this.setTab = function(newval) {
        devices.data = []; // clear the current data
        devices.currenttab = newval; // save the new tab
        console.log("tab set to " + newval);
        // immediately load the controller data for this device
        $http.get(DEVICE_TYPESPECIFIC_LIST_URL + devices.currenttab).success(function(data) {
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
      var devicePromise = $interval(function() {
        //console.log(new Date().getTime() + ">" + (devices.lastcomm + 15000));
        if (devices.fastpull || (new Date().getTime() > (devices.lastcomm + 15000))) {
          $http.get(DEVICE_TYPESPECIFIC_LIST_URL + devices.currenttab).success(function(data) {
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
      }, 30000);
      $scope.$on('$destroy', function() {
        $interval.cancel(devicePromise);
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
          url: CONTROLLER_UPDATE_URL,
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

      $scope.processFormDSP = function(id, state) {
        $scope.id = id;
        $scope.state = state;
        console.log("a button was pushed:" + id + "=>" + state);
        var len = devices.data.length;
        for (var p = 0; p < len; p++) {
          if ($scope.id === devices.data[p].id) {
            devices.data[p].desiredStatePriority = $scope.state;
            console.log("set id:" + devices.data[p].id + " to " + $scope.state);
          }
        }
        $http({
          method: 'post',
          url: CONTROLLER_UPDATE_URL,
          headers: {'Content-Type': 'application/x-www-form-urlencoded'},
          transformRequest: function(obj) {
            var str = [];
            for (var p in obj)
              str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
            return str.join("&");
          },
          data: {id: $scope.id, desiredStatePriority: $scope.state}
        }).success(function() {
          // should check for a 200 return
        });
        devices.fastpull = true;
      };
    }]);
  app.controller('SceneController', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
      var scenes = this;
      scenes.list = [];
      scenes.active = 0;

      $http.get(SCENE_LIST_URL).success(function(data) {
        scenes.list = data;
        console.log("get scenes");
        console.log(scenes.list);
      });

      $scope.processForm = function(id) {
        $scope.id = id;
        scenes.active = id;
        console.log("a scene button was pushed:" + id);
        $http({
          method: 'post',
          url: SCENE_STATUS_UPDATE_URL,
          headers: {'Content-Type': 'application/x-www-form-urlencoded'},
          transformRequest: function(obj) {
            var str = [];
            for (var p in obj)
              str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
            return str.join("&");
          },
          data: { id: $scope.id }
        }).success(function() {
          // should check for a 200 return
        });
      };
      $scope.isSet = function(id) {
        return (scenes.active == id);
      };
    }]);
})();
