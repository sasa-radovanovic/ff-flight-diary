flightDiaryApp.controller('loginCtrl',
		function($rootScope, $scope, $http, $location, $window, $uibModal) {

	$scope.loginError = false;
	$scope.formData = {
			"username" : "",
			"password" : ""
	};
	$scope.submit = function () {
		$scope.loginError = false;
		$http({
		    method: 'POST',
		    url: '/loginhandler',
		    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
		    transformRequest: function(obj) {
		        var str = [];
		        for(var p in obj)
		        str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
		        return str.join("&");
		    },
		    data: $scope.formData
		}).success(function (data) {
			$window.location.href = "/private/#";
		}).error(function (data) {
			if (data == "Forbidden") {
				$scope.loginError = true;
			}
		});
	};

});