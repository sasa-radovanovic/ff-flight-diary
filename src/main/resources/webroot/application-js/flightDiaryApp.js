var flightDiaryApp = angular.module("flightDiaryApp", ['ngCookies','ngRoute',   
                                                       'LocalStorageModule','ngAnimate', 'ui.bootstrap', 'ngSanitize']);

flightDiaryApp.run(function($rootScope, $http, $window) {
	$rootScope.calculatePerc = function (num, total) {
		var perc = (num * 100) / total;
		perc = Math.round(perc * 100) / 100;
		return perc;
	}
	$rootScope.roundRating = function (num) {
		return Math.round(num * 100) / 100;
	}
});

flightDiaryApp.config(['$routeProvider', function(routeProvider) {


	routeProvider.when("/", {
		templateUrl: "home.html",
		controller: "",
		resolve: {
		}
	})
	.when("/page1", {
		templateUrl: "page1.html",
		controller: "",
		resolve: {
		}
	})
	.when("/register", {
		templateUrl: "register.html",
		controller: "registerCtrl",
		resolve: {
		}
	})
	.when("/login", {
		templateUrl: "loginpage.html",
		controller: "loginCtrl",
		resolve: {
		}
	})
	.when("/private/private_page", {
		templateUrl: "../private/private_page.html",
		controller: "",
		resolve: {
		}
	})
	.when("/airports", {
		templateUrl: "airportsStats.html",
		controller: "airportsStatsCtrl",
		resolve: {
		}
	})
	.when("/profile/:username", {
		templateUrl: "profile.html",
		controller: "profileCtrl",
		resolve: {
		}
	})
	.when("/users", {
		templateUrl: "users.html",
		controller: "usersStatsCtrl",
		resolve: {
		}
	})
	.when("/general", {
		templateUrl: "generalStats.html",
		controller: "generalStatsCtrl",
		resolve: {
		}
	})
	.when("/activate/:activation_token/", {
		templateUrl: "activate.html",
		controller: "activateCtrl",
		resolve: {
		}
	})
	.when("/about", {
		templateUrl: "about.html"
	})

}]);


