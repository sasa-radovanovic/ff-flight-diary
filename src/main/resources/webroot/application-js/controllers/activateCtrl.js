flightDiaryApp.controller('activateCtrl',
		function($rootScope, $routeParams, $scope, $http, $location) {
	
	$scope.loading = true;
	$scope.activationToken = $routeParams.activation_token;
	$scope.success = false;
	
	$scope.register = function () {
		$http.post("/api/activate/" + $scope.activationToken , {})
		.then(function (data) {
			if (data && data.data && data.status==200) {
				$scope.activatedUser = data.data;
				$scope.success = true;
			} else {
				$scope.success = false;
			}
			$scope.loading = false;
		});
	}
	
	$scope.register();
	
	
	
	
});