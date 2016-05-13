flightDiaryApp.controller('generalStatsCtrl',
		function($rootScope, $routeParams, $scope, $window, $http, $location, $uibModal) {
	
	$scope.loading = false;
	$scope.airlinesExist = false;
	$scope.airplaneTypesExist = false;
	$scope.getGeneralStats = function () {
		$scope.loading = true;
		$http.get('/api/stats/general').then(function (data) {
			if (data) {
				$scope.generalStats = data.data;
				$scope.loading = false;
				$scope.generatePercData($scope.generalStats.airlines);
				if (data.data.airlines && data.data.airlines.length > 0) {
					$scope.generatePercData($scope.generalStats.airlines);
					$scope.airlinesExist = true;
				}
				if (data.data.airplaneTypes && data.data.airplaneTypes.length > 0) {
					$scope.generatePercData($scope.generalStats.airplaneTypes);
					$scope.airplaneTypesExist = true;
				}
			}
		});
	};
	$scope.getGeneralStats();
	
	$scope.generatePercData = function (array) {
		var total = 0;
		array.forEach(function (elem) {
			total += elem.count;
		});
		var padd = 8;
		array.forEach(function (elem) {
			elem.perc = $rootScope.calculatePerc(elem.count, total);
			elem.padd = padd;
			padd += 18;
		});
	};
	
	$scope.dynamicPopoverAirline = {
			templateUrl: 'airlinePopover.html',
			src: ''
	};

	$scope.hoveredAirline = function (airline) {
		$scope.hoveredAirlineObject = airline;
	};
	
	$scope.dynamicPopoverAirplaneType = {
			templateUrl: 'airplaneTypePopover.html',
			src: ''
	};

	$scope.hoveredAirplaneType = function (airplaneType) {
		$scope.hoveredAirplaneTypeObject = airplaneType;
	};
	
	
	$scope.fetchAutoSuggestionAirlines = function () {
		if ($scope.airlineInp == undefined || $scope.airlineInp.length < 2) {
			return;
		}
		$http.post("/api/airlines/partial/" + $scope.airlineInp, {})
		.then(function (data) {
			$scope.autoSuggestionAirlines = data.data;
			return data.data;
		}, function (data) {

		});
	};

	$scope.$watch('airlineInp', function(newValue, oldValue) {
		if (newValue instanceof Object) {
			$scope.openAirlineData(newValue);
		}
	});

	$scope.fetchAutoSuggestionAirplaneTypes = function () {
		if ($scope.airplaneTypeInp == undefined || $scope.airplaneTypeInp.length < 2) {
			return;
		}
		$http.post("/api/airplane_types/partial/" + $scope.airplaneTypeInp, {})
		.then(function (data) {
			$scope.autoSuggestionAirplaneTypes = data.data;
			return data.data;
		}, function (data) {

		});
	};

	$scope.$watch('airplaneTypeInp', function(newValue, oldValue) {
		if (newValue instanceof Object) {
			$scope.openAirplaneTypeData(newValue);
		}
	});
	
	$scope.openAirlineData = function (airlineObj) {
		var airlineStatsModal = $uibModal.open({
			animation: true,
			templateUrl: 'airlineStats.html',
			controller: 'airlineStatsCtrl',
			resolve: {
				"airlineObj" : function () {
					return  airlineObj;
				}
			}
		});
		
		airlineStatsModal.result.then(function (result) {
			if (result != undefined) {
				if (result.usernameRedirect != undefined) {
					$window.location.href = "/#/profile/" + result.usernameRedirect;
				} else if (result.signin != undefined && result.signin) {
					$window.location.href = "/private/#";
				}
			}
		});
	};
	
	$scope.openAirplaneTypeData = function (airplaneTypeObj) {
		var airplaneStatsModal = $uibModal.open({
			animation: true,
			templateUrl: 'airplaneTypeStats.html',
			controller: 'airplaneStatsCtrl',
			resolve: {
				"airplaneTypeObj" : function () {
					return  airplaneTypeObj;
				}
			}
		});
		
		airplaneStatsModal.result.then(function (result) {
			if (result != undefined) {
				if (result.usernameRedirect != undefined) {
					$window.location.href = "/#/profile/" + result.usernameRedirect;
				} else if (result.signin != undefined && result.signin) {
					$window.location.href = "/private/#";
				}
			}
		});
	};
	
	
	$scope.loadAndOpenAirplaneTypeData = function (iata_code) {
		$http.get('/api/airplane_types/' + iata_code).then(function (data) {
			if (data) {
				$scope.openAirplaneTypeData(data.data);
			}
		});
	};
	
	$scope.loadAndOpenAirlinetData = function (iata_code) {
		$http.get('/api/airlines/' + iata_code).then(function (data) {
			if (data) {
				$scope.openAirlineData(data.data);
			}
		});
	};
	
	
});

flightDiaryApp.controller('airplaneStatsCtrl', function ($scope, $uibModalInstance, $http, $location, $uibModal, $rootScope, airplaneTypeObj) {
	$scope.airplaneTypeObj = airplaneTypeObj;
	
	
	function checkImage (src, good, bad) {
	    var img = new Image();
	    img.onload = good; 
	    img.onerror = bad;
	    img. src = src;
	}
	

	$scope.preCheckTypeCode = function (typeCode) {
		if (typeCode.indexOf('AT') > -1) {
			return 'AT72';
		} 
		if (typeCode.indexOf('388') > -1) {
			return '380';
		}
		if (typeCode.indexOf('77') > -1) {
			return '773';
		}
		if (typeCode.indexOf('76') > -1) {
			return '763';
		}
		if (typeCode.indexOf('SU') > -1) {
			return 'SU95';
		}
		if (typeCode.indexOf('M88') > -1) {
			return 'MD88';
		}
		if (typeCode.indexOf('M80') > -1) {
			return 'MD80';
		}
		if (typeCode.indexOf('M90') > -1) {
			return 'MD90';
		}
		if (typeCode.indexOf('M11') > -1) {
			return 'MD11';
		}
		if (typeCode.indexOf('D10') > -1) {
			return 'DC10';
		}
		if (typeCode.indexOf('78') > -1) {
			return 'B788';
		}
		if (typeCode.indexOf('359') > -1) {
			return 'A359';
		}
		if (typeCode.indexOf('345') > -1) {
			return '343';
		}
		if (typeCode.indexOf('330') > -1) {
			return '332';
		}
		if (typeCode.indexOf('737') > -1) {
			return '733';
		}
		if (typeCode.indexOf('736') > -1) {
			return '735';
		}
		if (typeCode.indexOf('E90') > -1) {
			return 'E190';
		}
		if (typeCode.indexOf('E95') > -1) {
			return 'E195';
		}
		if (typeCode.indexOf('E70') > -1) {
			return 'E170';
		}
		if (typeCode.indexOf('100') > -1) {
			return 'F100';
		}
		if (typeCode.indexOf('DH') > -1) {
			return 'DH8D';
		}
		return typeCode;
	};
	$scope.logoAvailable = false;
	$scope.fetchAirplaneLogo = function (rawCode) {
		var searchCode = $scope.preCheckTypeCode(rawCode);
		$scope.logoSrc = "https://planefinder.net/flightstat/v1/getImage.php?airlineCode=XX&aircraftType=" + searchCode + "&skipFuzzy=1";
		checkImage($scope.logoSrc, function(){ 
				$scope.logoAvailable = true;
				$scope.$apply();
			}, function() { 
				$scope.logoAvailable = false;
			}
		);
	};
	$scope.fetchAirplaneLogo($scope.airplaneTypeObj.type_code);
	
	$scope.fetchAirplaneTypeStatistic = function (type_code) {
		$scope.loadingStats = true;
		$http.get('/api/stats/airplane_types/detailed/' + type_code).then(function (data) {
			$scope.loadingStats = false;
			$scope.detailedStats = data.data;
			$scope.parseAirports(data.data.airports);
		});
	};
	$scope.fetchAirplaneTypeStatistic($scope.airplaneTypeObj.type_code);
	
	$scope.parseAirports = function (airportMap) {
		var localArray = [];
		for (var airportCode in airportMap) {
			localArray.push({
				"code" : airportCode,
				"count" : airportMap[airportCode]
			});
		}
		localArray.sort(function(a, b) {
			return parseInt(b.value) - parseInt(a.value);
		});
		$scope.airportsToShow = [];
		if (localArray.length > 0) {
			for (var c = 0; c < 3; c ++) {
				if (localArray[c] != undefined) {
					$scope.airportsToShow.push(localArray[c]);
				}
			}
		}
	};
	
	$scope.redirectToUser = function (username) {
		$uibModalInstance.close({
			"usernameRedirect" : username,
			"signin" : false
		});
	};
	
	$scope.redirectToSignIn = function () {
		$uibModalInstance.close({
			"signin" : true
		});
	}
	$scope.cancel = function () {
		$uibModalInstance.close();
	};

	
});


flightDiaryApp.controller('airlineStatsCtrl', function ($scope, $uibModalInstance, $http, $location, $uibModal, $rootScope, airlineObj) {

	$scope.airlineObj = airlineObj;
	
	function checkImage (src, good, bad) {
	    var img = new Image();
	    img.onload = good; 
	    img.onerror = bad;
	    img. src = src;
	}

	$scope.logoAvailable = false;
	$scope.logoSrc = 'http://airlinelogos.aero/500px/' + $scope.airlineObj.airline_iata_code + '.png';
	checkImage($scope.logoSrc, function(){ 
			$scope.logoAvailable = true;
			$scope.$apply();
		}, function(){ 
			$scope.logoAvailable = false;
		}
	);
	
	$scope.loadingStats = false;
	$scope.getSingleAirlineStats = function () {
		$scope.loadingStats = true;
		$http.get('/api/stats/airlines/detailed/' + $scope.airlineObj.airline_iata_code).then(function (data) {
			$scope.loadingStats = false;
			$scope.detailedStats = data.data;
			$scope.parseFlights(data.data.flightsList);
			$scope.parseUsers(data.data.frequentUsers);
		});
	};
	$scope.getSingleAirlineStats();
	
	$scope.parseFlights = function (flightsMap) {
		$scope.flights = [];
		for (var flightId in flightsMap) {
			$scope.flights.push({
				"long_one" : flightsMap[flightId].longitude_one,
				"long_two" : flightsMap[flightId].longitude_two,
				"lat_one" : flightsMap[flightId].latitude_one,
				"lat_two" : flightsMap[flightId].latitude_two
			});
		}
		$scope.setRoutesWithAirline($scope.flights);
	};
	
	$scope.users = [];
	$scope.parseUsers = function (usersMap) {
		var locUsers = []; 
		for (var username in usersMap) {
			locUsers.push({
				"count" : usersMap[username],
				"username" : username
			});
		}
		locUsers.sort(function(a, b) {
			return parseInt(b.value) - parseInt(a.value);
		});
		$scope.usersToShow = [];
		if (locUsers.length > 0) {
			for (var c = 0; c < 3; c ++) {
				if (locUsers[c] != undefined) {
					$scope.users.push(locUsers[c]);
				}
			}
		}
	};
	
	
	$scope.redirectToUser = function (username) {
		$uibModalInstance.close({
			"usernameRedirect" : username,
			"signin" : false
		});
	};
	
	$scope.redirectToSignIn = function () {
		$uibModalInstance.close({
			"signin" : true
		});
	}
	$scope.cancel = function () {
		$uibModalInstance.close();
	};
	
	

	$scope.routesMap = "";
	var myLatlng = new google.maps.LatLng(44, 20.461414);
	$scope.flightMarkersOnMap = [];

	var customMapType = new google.maps.StyledMapType(
			[
			 {
				 stylers: [
				           {hue: '#99ffcc'},
				           {visibility: 'simplified'},
				           {gamma: 0.2},
				           {weight: 0.2}
				           ]
			 },
			 {
				 elementType: 'labels',
				 stylers: [{visibility: 'off'}]
			 },
			 {
				 featureType: 'water',
				 stylers: [{color: '#ccffe6'}]
			 }
			 ], {
				name: 'Flight Diary style'
			});
	var customMapTypeId = 'custom_style';
	
	$scope.setRoutesWithAirline = function (routesObj) {
		setTimeout(function(){ 
			var mapCanvasId = 'airline-map-routes',
			myOptions = {
					center: myLatlng,
					streetViewControl: false,
					mapTypeControlOptions: {
						mapTypeIds: [google.maps.MapTypeId.ROADMAP, customMapTypeId]
					},
					zoom: 4,
					zoomControlOptions: {
						style: google.maps.ZoomControlStyle.LARGE,
						position: google.maps.ControlPosition.LEFT_CENTER
					}
			}
			$scope.routesMap = new google.maps.Map(document.getElementById(mapCanvasId), myOptions);
			$scope.routesMap.mapTypes.set(customMapTypeId, customMapType);
			$scope.routesMap.setMapTypeId(customMapTypeId);
			setFlights($scope.routesMap, routesObj);  
		}, 1000); 
	};
	
	function setFlights(map, flights) {
		for (var i = $scope.flightMarkersOnMap.length - 1; i >= 0; i--) {
			$scope.flightMarkersOnMap[i].setMap(null);
		}; 
		var bounds = new google.maps.LatLngBounds();
		for (var j = flights.length - 1; j >= 0; j--) {
			var markerDeparture = new google.maps.Marker({
				position: {
					lat : flights[j].long_one,
					lng: flights[j].lat_one
				},
				map: map,
				icon: "img/airport_small.png",
				title: "Airport"
			});
			var markerArrival = new google.maps.Marker({
				position: {
					lat : flights[j].long_two,
					lng: flights[j].lat_two
				},
				map: map,
				icon: "img/airport_small.png",
				title: "Airport"
			});
			markerDeparture.setMap(map);
			markerArrival.setMap(map);
			$scope.flightMarkersOnMap.push(markerDeparture);
			$scope.flightMarkersOnMap.push(markerArrival);
			bounds.extend(new google.maps.LatLng(flights[j].long_two, flights[j].lat_two));
			bounds.extend(new google.maps.LatLng(flights[j].long_one, flights[j].lat_one));
			var flightPlanCoordinates = [{lat: flights[j].long_two, lng: flights[j].lat_two},
			                             {lat: flights[j].long_one, lng: flights[j].lat_one}];
			var flightPath = new google.maps.Polyline({
				path: flightPlanCoordinates,
				geodesic: true,
				strokeColor: '#000099',
				strokeOpacity: 1.0,
				strokeWeight: 2
			});

			flightPath.setMap(map);
		}
		map.fitBounds(bounds);

	};
});