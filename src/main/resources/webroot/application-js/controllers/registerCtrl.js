flightDiaryApp.controller('registerCtrl',
		function($rootScope, $scope, $http, $location, $uibModal) {

	$scope.mail = "";
	$scope.username = "";
	$scope.password = "";
	$scope.passwordRepeat = "";
	
	$scope.error = false;
	$scope.fieldsError = false;
	$scope.passwordError = false;


	$scope.register = function () {
		var verification = $scope.verify();
		if (verification == 1) {
			$scope.fieldsError = true;
			return;
		} else if (verification == 2) {
			$scope.passwordError = true;
			return;
		} 
		$http.post("/api/users" , {
			'username' : $scope.username,
			'mail' : $scope.mail,
			'password' : $scope.password
		}).then(function (data) {
			console.log(data);
			if (data.status == 200 && data.data && data.data.id) {
				var loadModal = $uibModal.open({
					animation: true,
					templateUrl: 'regSuccessModal.html',
					controller: 'regSucModalCtrl',
					backdrop: 'static',
					keyboard: false,
					resolve: {  
					}
				});

				loadModal.result.then(function (redirect) {
					$location.path("/");
				});
			} else {
				$scope.error = true;
			}
		});
	}
	
	
	$scope.verify = function () {
		
		$scope.fieldsError = false;
		$scope.passwordError = false;
		
		if ($scope.mail.length < 3 || $scope.username.length < 3 || $scope.password.length < 3 || $scope.passwordRepeat.length < 3) {
			return 1;
		} else if ($scope.password != $scope.passwordRepeat) {
			return 2;
		} 
		
		return 0;
	}


});

flightDiaryApp.controller('regSucModalCtrl', function ($scope, $uibModalInstance, $location, $uibModal, $rootScope) {

	$scope.redirect = function () {
		$uibModalInstance.close(true);
	};
});