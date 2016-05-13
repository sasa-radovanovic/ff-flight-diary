var flightDiaryPrivateApp = angular.module("flightDiaryPrivateApp", ['ngCookies','ngRoute',   
                                                                     'LocalStorageModule','ngAnimate', 'ui.bootstrap', 'ngSanitize', 'ui.chart']);


flightDiaryPrivateApp.value('charting', {
	pieChartOptions: {
		seriesColors: [ "#ccffcc", "#c1f0c1", "#b3ffff", "#ccccff", "#f2ccff", "#99bbff", "#ffffb3"],
		seriesDefaults: {
			// Make this a pie chart.
			renderer: jQuery.jqplot.PieRenderer,
			rendererOptions: {
				// Put data labels on the pie slices.
				// By default, labels show the percentage of the slice.
				showDataLabels: true
			}
		},
		legend: { show:true, location: 'e' }
	}
});


flightDiaryPrivateApp.run(function($rootScope, $http, $window) {
	$rootScope.logout = function () {
		$http.post('../logout').then(function (data) {
			if (data) {
				$window.location.href = "/";
			}
		});
	}
	$rootScope.editFlightStore = function (flightToEdit) {
		$rootScope.flightToEdit = flightToEdit;
	}
	$rootScope.calculatePerc = function (num, total) {
		var perc = (num * 100) / total;
		perc = Math.round(perc * 100) / 100;
		return perc;
	}
	$rootScope.acquireMyName = function () {
		if ($rootScope.myUsername != undefined && $rootScope.myUsername != "") {
			return $rootScope.myUsername;
		}
		$http.post('../whoami').then(function (data) {
			if (data) {
				$rootScope.myUsername = data.data.youare;
				return $rootScope.myUsername;
			}
		});
	}
})


flightDiaryPrivateApp.config(['$routeProvider', function(routeProvider) {


	routeProvider.when("/", {
		templateUrl: "../private/profile.html",
		controller: "profileCtrl",
		resolve: {
		}
	})
	.when("/profile", {
		templateUrl: "../private/profile.html",
		controller: "profileCtrl",
		resolve: {
		}
	})
	.when("/newFlight", {
		templateUrl: "../private/newFlight.html",
		controller: "newFlightCtrl",
		resolve: {
		}
	})
	.when("/myStats", {
		templateUrl: "../private/myStats.html",
		controller: "myStatsCtrl",
		resolve: {
		}
	})
	.when("/editFlight/:flight_id/", {
		templateUrl: "../private/newFlight.html",
		controller: "newFlightCtrl",
		resolve: {
		}
	})

}]);


