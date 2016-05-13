flightDiaryPrivateApp.controller('newFlightCtrl',
		function($rootScope, $routeParams, $scope, $http, $location, $window, $uibModal, $routeParams) {

	$scope.flight_id = $routeParams.flight_id;
	
	$scope.isNew = true;
	
	$scope.rebuildDate = function (dateString) {
		var parts = dateString.split('/');
		var year = parts[2].split(' ');
		var d = new Date(parts[1] + "/" + parts[0] + "/" + "/" + year[0]);
		return d;
	};
	
	$scope.rebuildTimeDate = function (dateString) {
		var dateComponents = dateString.split(' ');
		var timeComponents = dateComponents[1].split(':');
		var d = new Date();
		d.setHours(timeComponents[0]);
		d.setMinutes(timeComponents[1]);
		return d;
	};
	
	$scope.today = function() {
		$scope.departDate = new Date();
		$scope.arrivDate = new Date();
	};
	
	$scope.isEdit = function () {
		if ($scope.flight_id == null || $scope.flight_id == undefined) {
			$scope.isNew = true;
			$scope.newFlightObj = {
					"flight_class" : 0,
					"airplane" : "",
					"purpose" : 0,
					"rating"  : 0,
					"ticket_source" : "airline"
			};

			$scope.today();

		} else {
			if ($rootScope.flightToEdit == null || $rootScope.flightToEdit == undefined) {
				$window.location.href = "/private/#/profile/";
			}
			$scope.newFlightObj = {
					"flight_class" : $scope.flightToEdit.flight.flight_class,
					"airplane" : "",
					"purpose" : $scope.flightToEdit.flight.purpose,
					"rating"  : $scope.flightToEdit.flight.rating,
					"ticket_source" : $scope.flightToEdit.flight.ticket_source
			};
			$http.get("/api/airports/" + $rootScope.flightToEdit.flight.departure)
			.then(function (data) {
				if (data) {
					$scope.fromAirport = data.data;
					$scope.airportFrom = data.data;
				} 
			}, function (data) {

			});
			$http.get("/api/airports/" + $rootScope.flightToEdit.flight.arrival)
			.then(function (data) {
				if (data) {
					$scope.toAirport = data.data;
					$scope.airportTo = data.data;
				} 
			}, function (data) {

			});
			$http.get("/api/airlines/" + $rootScope.flightToEdit.flight.airline_code)
			.then(function (data) {
				if (data) {
					$scope.airlineCode = data.data;
					$scope.airline = $scope.airlineCode;
					$scope.newFlightObj.airline_code = $scope.airlineCode.airline_iata_code;
				} 
			}, function (data) {

			});
			$http.get("/api/airplane_types/" + $rootScope.flightToEdit.flight.airplane)
			.then(function (data) {
				if (data) {
					$scope.airplane = data.data;
					$scope.airplaneType = $scope.airplane;
					$scope.newFlightObj.airplane = $scope.airplane.type_code;
				} 
			}, function (data) {

			});
			$scope.departDate = $scope.rebuildDate($rootScope.flightToEdit.flight.departure_time);
			$scope.mytime = $scope.rebuildTimeDate($rootScope.flightToEdit.flight.departure_time);
			$scope.isNew = false;
			$scope.flightToEdit = $rootScope.flightToEdit;
		}
	};
	$scope.isEdit();
	
	$scope.saveChanges = function () {
		if (!($scope.fromAirport instanceof Object) || $scope.fromAirport == undefined) {
			alert("From airport is missing.");
			return;
		}
		if (!$scope.toAirport instanceof Object || $scope.toAirport == undefined) {
			alert("To airport is missing.");
			return;
		}
		if (!$scope.airlineCode instanceof Object || $scope.airlineCode == undefined) {
			alert("Airline is missing.");
			return;
		}
		if (!$scope.airplane instanceof Object || $scope.airplane == undefined) {
			alert("Airplane is missing.");
			return;
		}
		
		if (!$scope.mytime instanceof Date || $scope.mytime == undefined) {
			alert("Time is missing");
		}

		$scope.newFlightObj.departure = $scope.fromAirport.iata_code;
		$scope.newFlightObj.arrival = $scope.toAirport.iata_code;
		$scope.newFlightObj.airline_code = $scope.airlineCode.airline_iata_code;
		$scope.newFlightObj.airplane = $scope.airplane.type_code;
		$scope.newFlightObj.departure_time = $scope.arangeTime($scope.departDate, $scope.mytime);

		$scope.newFlightObj.flight_id = $scope.flight_id;
		
		$http.patch("/api/flights", $scope.newFlightObj)
		.then(function (data) {
			if (data.status == 204) {
				$window.location.href = "/private/#/";
			} else {
				console.log("Error");
			}
		}, function (data) {

		});
	}
	
	$scope.showDataMissing = function (msg) {
		var errorModal = $uibModal.open({
			animation: true,
			templateUrl: 'errorModal.html',
			controller: 'errorModalCtrl',
			backdrop: 'static',
			keyboard: false,
			resolve: {
				"error" : function () {
					return  msg;
				}
			}
		});
	};

	$scope.addFlight = function () {
		if (!($scope.fromAirport instanceof Object) || $scope.fromAirport == undefined) {
			$scope.showDataMissing("Departure airport is missing. " +
					"Start typing airport name or location and select one from the drop down list.");
			return;
		}
		if (!$scope.toAirport instanceof Object || $scope.toAirport == undefined) {
			alert("Arrival airport is missing. " +
					"Start typing airport name or location and select one from the drop down list.");
			return;
		}
		if (!$scope.airlineCode instanceof Object || $scope.airlineCode == undefined) {
			$scope.showDataMissing("Airline is missing. Start typing airline " +
					"name or code and select one from drop down list.");
			return;
		}
		if (!$scope.airplane instanceof Object || $scope.airplane == undefined) {
			$scope.showDataMissing("Airplane type is missing. " +
					"Start typing manufacturer or type and then select one you travelled with.");
			return;
		}
		
		if (!$scope.mytime instanceof Date || $scope.mytime == undefined) {
			$scope.showDataMissing("Departure time is missing. Use time picker to select time of departure. If you do not remember exact time, " +
					"enter the closest you remember. This will help us bring you better statistics.");
		}

		$scope.newFlightObj.departure = $scope.fromAirport.iata_code;
		$scope.newFlightObj.arrival = $scope.toAirport.iata_code;
		$scope.newFlightObj.airline_code = $scope.airlineCode.airline_iata_code;
		$scope.newFlightObj.airplane = $scope.airplane.type_code;
		$scope.newFlightObj.departure_time = $scope.arangeTime($scope.departDate, $scope.mytime);

		$http.post("/api/flights", $scope.newFlightObj)
		.then(function (data) {
			if (data.status == 200 && data.data.flight.flight_id != null) {
				$scope.flightAdded(data.data);
			} else {
				$scope.flightAddError();
			}
		}, function (data) {

		});
	};

	$scope.flightAdded = function (flightAdded) {
		var loadModal = $uibModal.open({
			animation: true,
			templateUrl: 'flightAddedModal.html',
			controller: 'flightAddedModalCtrl',
			backdrop: 'static',
			keyboard: false,
			resolve: {
				"flightAdded" : function () {
					return  flightAdded;
				},
				"myUsername" : function () {
					return $rootScope.acquireMyName();
				}
			}
		});


		loadModal.result.then(function (click) {
			if(click == 0){
				$location.path("/");
			} else if (click == 1) {
				var temp = $scope.fromAirport;
				$scope.fromAirport = $scope.toAirport;
				$scope.toAirport = temp;
				$scope.airportFrom = $scope.fromAirport;
				$scope.airportTo = $scope.toAirport;
			} else {
				$scope.newFlightObj = {
						"flight_class" : 0,
						"airplane" : "",
						"purpose" : 0,
						"rating"  : 0,
						"ticket_source" : "airline"
				};
				$scope.airlineCode = undefined;
				$scope.airline = undefined;
				$scope.airplane = undefined;
				$scope.airplaneType = undefined;
				$scope.fromAirport = undefined;
				$scope.toAirport = undefined;
				$scope.airportFrom = undefined;
				$scope.airportTo = undefined;
			}
		});


	};

	$scope.flightAddError = function () {

	};

	$scope.changed = function () {
		if ($scope.mytime == null) {
			var d = new Date();
			d.setHours( 23 );
			d.setMinutes( 0 );
			$scope.mytime = d;
		}
	};

	$scope.formatMinutes = function (minutes) {
		if (minutes > 9) {
			return minutes;
		}
		return "0" + minutes;
	};

	$scope.arangeTime = function (date, time) {
		var returnDate = date.getDate() + "/" + (date.getMonth() + 1) + "/" + date.getFullYear() + " " + time.getHours() + ":" + $scope.formatMinutes(time.getMinutes());
		return returnDate;
	};
	


	$scope.setClass = function (classToSet) {
		$scope.newFlightObj.flight_class = classToSet;
	}; 

	$scope.setSource = function (source) {
		$scope.newFlightObj.ticket_source = source;
	}; 

	$scope.setPurpose = function (purposeToSet) {
		$scope.newFlightObj.purpose = purposeToSet;
	}


	$scope.fetchAutoSuggestionAirlines = function () {
		if ($scope.airline.length < 2) {
			return;
		}
		console.log("Search for " + $scope.airline);
		$http.post("/api/airlines/partial/" + $scope.airline, {})
		.then(function (data) {
			$scope.autoSuggestionAirlines = data.data;
			console.log(data.data);
			return data.data;
		}, function (data) {

		});
	};

	$scope.$watch('airline', function(newValue, oldValue) {
		if (newValue instanceof Object) {
			$scope.airlineCode = newValue;
		}
	});


	$scope.fetchAutoSuggestionAirplaneTypes = function () {
		if ($scope.airplaneType.length < 2) {
			return;
		}
		$http.post("/api/airplane_types/partial/" + $scope.airplaneType, {})
		.then(function (data) {
			$scope.autoSuggestionAirplaneTypes = data.data;
			console.log(data.data);
			return data.data;
		}, function (data) {

		});
	};

	$scope.$watch('airplaneType', function(newValue, oldValue) {
		if (newValue instanceof Object) {
			$scope.airplane = newValue;
		}
	});

	$scope.fetchAutoSuggestionFrom = function () {
		if ($scope.airportFrom.length < 2) {
			return;
		}
		$http.post("/api/airports/partial/" + $scope.airportFrom, {})
		.then(function (data) {
			$scope.autoSuggestionFrom = data.data;
			return data.data;
		}, function (data) {

		});
	}

	$scope.fetchAutoSuggestionTo = function () {
		if ($scope.airportTo.length < 2) {
			return;
		}
		$http.post("/api/airports/partial/" + $scope.airportTo, {})
		.then(function (data) {
			$scope.autoSuggestionTo = data.data;
			return data.data;
		}, function (data) {

		});
	}

	$scope.$watch('airportFrom', function(newValue, oldValue) {
		if (newValue instanceof Object) {
			$scope.fromAirport = newValue;
		}
	});

	$scope.$watch('airportTo', function(newValue, oldValue) {
		if (newValue instanceof Object) {
			$scope.toAirport = newValue;
		}
	});



	$scope.clear = function() {
		$scope.departDate = null;
		$scope.arrivDate = null;
	};

	$scope.inlineOptions = {
			customClass: getDayClass,
			minDate: new Date(),
			showWeeks: true
	};

	$scope.dateOptions = {
			formatYear: 'yy',
			maxDate: new Date(),
			startingDay: 1
	};


	$scope.toggleMin = function() {
		$scope.inlineOptions.minDate = $scope.inlineOptions.minDate ? null : new Date();
		$scope.dateOptions.minDate = $scope.inlineOptions.minDate;
	};

	$scope.toggleMin();

	$scope.open2 = function() {
		$scope.popup2.opened = true;
	};

	$scope.open1 = function() {
		$scope.popup1.opened = true;
	};


	$scope.setDepartDate = function(year, month, day) {
		$scope.departDate = new Date(year, month, day);
	};

	$scope.setArrivDate = function(year, month, day) {
		$scope.arrivDate = new Date(year, month, day);
	};

	$scope.formats = ['dd-MMMM-yyyy', 'yyyy/MM/dd', 'dd.MM.yyyy', 'shortDate'];
	$scope.format = $scope.formats[2];
	$scope.altInputFormats = ['M!/d!/yyyy'];

	$scope.popup2 = {
			opened: false
	};

	$scope.popup1 = {
			opened: false
	};

	function getDayClass(data) {
		var date = data.date,
		mode = data.mode;
		if (mode === 'day') {
			var dayToCheck = new Date(date).setHours(0,0,0,0);

			for (var i = 0; i < $scope.events.length; i++) {
				var currentDay = new Date($scope.events[i].date).setHours(0,0,0,0);

				if (dayToCheck === currentDay) {
					return $scope.events[i].status;
				}
			}
		}
		return '';
	}

});




flightDiaryPrivateApp.controller('flightAddedModalCtrl', function ($scope, $uibModalInstance, $location, $uibModal, $rootScope, flightAdded, myUsername) {

	$scope.flightAdded = flightAdded;
	$scope.myUsername = myUsername;
	
	$scope.showUsers = [];
	
	$scope.usersToShow = function () {
		if (flightAdded != undefined && flightAdded.flightData.usersRecentlyAdded != undefined && flightAdded.flightData.usersRecentlyAdded.length > 1) {
			flightAdded.flightData.usersRecentlyAdded.forEach(function (user) {
				if (user.username != $scope.myUsername) {
					$scope.showUsers.push(user);
				}
			});
		}
	}
	$scope.usersToShow();
	
	$scope.distance = Math.round(flightAdded.flightData.distance * 100) / 100;

	$scope.redirect = function () {
		$uibModalInstance.close(0);
	};

	$scope.addReturnFlight = function () {
		$uibModalInstance.close(1);
	};

	$scope.addNewFlight = function () {
		$uibModalInstance.close(2);
	};
});



flightDiaryPrivateApp.controller('errorModalCtrl', function ($scope, $uibModalInstance, $location, $uibModal, $rootScope, error) {

	$scope.msg = error;

	$scope.cancel = function () {
		$uibModalInstance.close();
	};
});