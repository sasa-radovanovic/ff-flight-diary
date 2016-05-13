flightDiaryApp.controller('usersStatsCtrl',
		function($rootScope, $routeParams, $window, $scope, $http, $location, $uibModal) {
	
	
	$scope.loading = true;
	$scope.getTopUsers = function () {
		$http.get('/api/stats/users').then(function (data) {
			$scope.topUsers = data.data;
			$scope.loading = false;
		}, function (data) {
			$window.location.href = "/#/";
		});
	};
	$scope.getTopUsers();
	
	$scope.getTotalUsersCount = function () {
		$http.get('/api/users/count').then(function (data) {
			$scope.totalUsersCount = data.data;
		}, function (data) {
			$window.location.href = "/#/";
		});
	};
	$scope.getTotalUsersCount();
	
	$scope.fetchAutoSuggestionSearch = function () {
		if ($scope.usersSearch == undefined || $scope.usersSearch.length < 3) {
			return;
		}
		$http.post("/api/users/partial/" + $scope.usersSearch, {})
		.then(function (data) {
			$scope.autoSuggestionUsers = data.data;
			return data.data;
		}, function (data) {

		});
	};
	
	$scope.$watch('usersSearch', function(newValue, oldValue) {
		if (newValue instanceof Object) {
			$window.location.href = "/#/profile/" + newValue.username;
		}
	});
	
});