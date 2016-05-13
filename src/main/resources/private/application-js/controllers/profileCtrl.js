flightDiaryPrivateApp.controller('profileCtrl',
		function($rootScope, $routeParams, $scope, $http, $location, $window, $uibModal) {
	
	$scope.myFlights = [];
	
	$scope.max = 5;
	$scope.isReadonly = true;
	
	$scope.loading = true;
	$scope.failed = false;
	
	$scope.editFlight = function (flight) {
		$rootScope.editFlightStore(flight);
		$window.location.href = "/private/#/editFlight/" + flight.flight.flight_id + "/";
	};

	
	$scope.getMyFlights = function () {
		$scope.loading = true;
		$http.get('../api/flights').then(function (data) {
			if (data) {
				console.log(data);
				if (data.data) {
					$scope.myFlights = data.data;
					$rootScope.myFlights = data.data;
					$scope.loading = false;
				} else {
					$scope.myFlights = [];
					$scope.loading = false;
				}
			} else {
				$scope.failed = true;
				$scope.loading = false;
			}
		});
	}
	
	$scope.getMyFlights();

	
	$scope.getMyProfileData = function () {
		$rootScope.acquireMyName();
	}
	$scope.getMyProfileData();
	
	$scope.deleteFlight = function (flight) {
		var deleteModal = $uibModal.open({
			animation: true,
			templateUrl: 'deleteFlightModal.html',
			controller: 'deleteFlightModalCtrl',
			backdrop: 'static',
			keyboard: false,
			resolve: {
				"flightToDelete" : function () {
					return  flight;
				}
			}
		});
		
		deleteModal.result.then(function (click) {
			if(click){
				$scope.getMyFlights();
			}
		});
	};	
});


flightDiaryPrivateApp.controller('deleteFlightModalCtrl', function ($scope, $http, $uibModalInstance, $uibModal, $rootScope, flightToDelete) {

	$scope.flightToDelete = flightToDelete;


	$scope.removeFlight = function () {
		$http.delete('../api/flights/' + flightToDelete.flight.flight_id).then(function (data) {
			$uibModalInstance.close(true);
		});
	};

	$scope.cancel = function () {
		$uibModalInstance.close(false);
	};

});