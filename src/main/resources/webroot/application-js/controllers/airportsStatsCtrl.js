flightDiaryApp.controller('airportsStatsCtrl',
		function($rootScope, $routeParams, $window, $uibModal, $scope, $http, $location) {


	$scope.loading = false;
	$scope.dataExists = false;
	$scope.getAirportsStats = function () {
		$scope.loading = true;
		$http.get("/api/stats/airports").then(function (data) {
			if (data) {
				$scope.stats = data.data;
				$scope.dataExists = true;
				$scope.loading = false;
				initMap($scope.stats);
				$scope.formRegionGraph($scope.stats.byRegionMap);
				$scope.formAirportsList('DEP', data.data.mostUsedDepartures);
				$scope.formAirportsList('ARR', data.data.mostUsedArrival);
			}
		});
	};
	$scope.getAirportsStats();
	
	
	$scope.formAirportsList = function (type, airportMap) {
		if (type == 'DEP') {
			$scope.depAirports = [];
			for (var air in airportMap) {
				$scope.depAirports.push({
					"code" : air,
					"value" : airportMap[air]
				});
			}
			$scope.depAirports.sort(function(a, b) {
				return parseInt(b.value) - parseInt(a.value);
			});
		} else if (type == 'ARR') {
			$scope.arrAirports = [];
			for (var air in airportMap) {
				$scope.arrAirports.push({
					"code" : air,
					"value" : airportMap[air]
				});
			}
			$scope.arrAirports.sort(function(a, b) {
				return parseInt(b.value) - parseInt(a.value);
			});
		}
	};
	
	
	$scope.formRegionGraph = function (regionMap) {
		$scope.regionGraph = [];
		for (var reg in regionMap) {
			$scope.regionGraph.push({
				"name" : reg,
				"value" : $rootScope.calculatePerc(regionMap[reg], $scope.stats.totalUsedAirports)
			});
		}
	};

	/* Map manipulation*/
	$scope.map = "";
	var myLatlng = new google.maps.LatLng(0, 0);
	$scope.markersOnMap = [];

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

	var initMap = function (stats) {

		setTimeout(function(){ 
			console.log("Initialize map");
			var mapCanvasId = 'map-holder',
			myOptions = {
					center: myLatlng,
					streetViewControl: false,
					mapTypeControlOptions: {
						mapTypeIds: [google.maps.MapTypeId.ROADMAP, customMapTypeId]
					},
					zoom: 1,
					zoomControlOptions: {
						style: google.maps.ZoomControlStyle.LARGE,
						position: google.maps.ControlPosition.LEFT_CENTER
					}
			}
			$scope.map = new google.maps.Map(document.getElementById(mapCanvasId), myOptions);
			$scope.map.mapTypes.set(customMapTypeId, customMapType);
			$scope.map.setMapTypeId(customMapTypeId);
			$scope.setHemispheres(stats.northernAirports, stats.southernAirports);
		}, 500); 
	};

	$scope.setHemispheres = function (northernAirports, southernAirports) {
		// Define the LatLng coordinates for the polygon's path.
		var northEastC = [
		                      {lat: 0, lng: 0},
		                      {lat: 85, lng: 0},
		                      {lat: 85, lng: 180},
		                      {lat: 0, lng: 180}
		                      ];

		var northWestC = [
		                       {lat: 0, lng: 0},
		                       {lat: 85, lng: 0},
		                       {lat: 85, lng: -180},
		                       {lat: 0, lng: -180}
		                       ];

		// Construct the polygon.
		var northEast = new google.maps.Polygon({
			paths: northEastC,
			strokeColor: '#0033cc',
			strokeOpacity: 0.8,
			strokeWeight: 0,
			fillColor: '#0033cc',
			fillOpacity: 0.35
		});
		// Construct the polygon.
		var northWest = new google.maps.Polygon({
			paths: northWestC,
			strokeColor: '#0033cc',
			strokeOpacity: 0.8,
			strokeWeight: 0,
			fillColor: '#0033cc',
			fillOpacity: 0.35
		});
		northEast.setMap($scope.map);
		northWest.setMap($scope.map);
		
		var markerInvNorth = new google.maps.Marker({
			position: {
				lat : 45,
				lng: 0
			},
			map: $scope.map,
			icon: "img/none.png",
			title: "Northern Hemisphere Flights"
		});
		
		var infowindowNorth = new google.maps.InfoWindow({content: "<h2>" + northernAirports + " airports</h2>"});
		google.maps.event.addListener(markerInvNorth, 'click', 
				function (infowindowNorth, markerInvNorth) {
			return function () {
				infowindow.open(map, markerInvNorth);
			};
		}(infowindowNorth, markerInvNorth));
		
		infowindowNorth.open($scope.map, markerInvNorth);
		
		var markerInvSouth = new google.maps.Marker({
			position: {
				lat : -75,
				lng: 0
			},
			map: $scope.map,
			icon: "img/none.png",
			title: "Southern Hemisphere Flights"
		});
		
		var infowindowSouth = new google.maps.InfoWindow({content: "<h2>" + southernAirports + " airports</h2>"});
		google.maps.event.addListener(markerInvSouth, 'click', 
				function (infowindowSouth, markerInvSouth) {
			return function () {
				infowindow.open(map, markerInvSouth);
			};
		}(infowindowSouth, markerInvSouth));
		
		infowindowSouth.open($scope.map, markerInvSouth);
	};


	$scope.dynamicPopoverAirport = {
			airportData: {},
			templateUrl: 'airportPopover.html',
			src: ''
	};

	$scope.hoveredAirport = function (code) {
		$scope.hoveredAirportObject = {};
		$scope.retrievingHovered = true;
		$http.get('/api/airports/' + code).then(function (data) {
			if (data) {
				$scope.retrievingHovered = false;
				$scope.hoveredAirportObject = data.data;
			}
		});
	};
	
	$scope.fetchAutoSuggestionSearch = function () {
		if ($scope.airportSearch.length < 2) {
			return;
		}
		$http.post("/api/airports/partial/" + $scope.airportSearch, {})
		.then(function (data) {
			$scope.autoSuggestionAirports = data.data;
			return data.data;
		}, function (data) {

		});
	};

	$scope.$watch('airportSearch', function(newValue, oldValue) {
		if (newValue instanceof Object) {
			$scope.airportToFetch = newValue;
			$scope.openAirportData($scope.airportToFetch);
		}
	});
	
	
	$scope.openAirportData = function (airportObject) {
		var airportStatsModal = $uibModal.open({
			animation: true,
			templateUrl: 'airportStats.html',
			controller: 'airportStatsCtrl',
			resolve: {
				"airportObj" : function () {
					return  airportObject;
				}
			}
		});
		
		airportStatsModal.result.then(function (result) {
			if (result != undefined) {
				if (result.usernameRedirect != undefined) {
					$window.location.href = "/#/profile/" + result.usernameRedirect;
				} else if (result.signin != undefined && result.signin) {
					$window.location.href = "/private/#";
				}
			}
		});
	};
	
	$scope.loadAndOpenAirportData = function (iata_code) {
		$http.get('/api/airports/' + iata_code).then(function (data) {
			if (data) {
				$scope.openAirportData(data.data);
			}
		});
	};
	
});


flightDiaryApp.controller('airportStatsCtrl', function ($scope, $uibModalInstance, $http, $location, $uibModal, $rootScope, airportObj) {

	$scope.airportObject = airportObj;
	
	$scope.map = "";
	$scope.routesMap = "";
	var myLatlng = new google.maps.LatLng(44, 20.461414);
	$scope.markersOnMap = [];
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

	$(document).ready(function () {

		setTimeout(function(){ 
			var mapCanvasId = 'airport-location-map',
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
			$scope.map = new google.maps.Map(document.getElementById(mapCanvasId), myOptions);
			$scope.map.mapTypes.set(customMapTypeId, customMapType);
			$scope.map.setMapTypeId(customMapTypeId);
			setAirportOnMap($scope.map, $scope.airportObject);  
		}, 1000); 
	});

	var setAirportOnMap = function (map, airportObject) {
		for (var i = $scope.markersOnMap.length - 1; i >= 0; i--) {
			scope.markersOnMap[i].setMap(null);
		}; 
		var bounds = new google.maps.LatLngBounds();
		var marker = new google.maps.Marker({
			position: {
				lat : airportObject.longitude,
				lng: airportObject.latitude
			},
			map: map,
			icon: "img/airport_small.png",
			title: airportObject.airport_name
		});
		var infowindow = new google.maps.InfoWindow({content: airportObject.airport_name});
		google.maps.event.addListener(marker, 'click', 
				function (infowindow, marker) {
			return function () {
				infowindow.open(map, marker);
			};
		}(infowindow, marker));
		
		infowindow.open($scope.map, marker);
		marker.setMap($scope.map);
		$scope.markersOnMap.push(marker);
		bounds.extend(new google.maps.LatLng(airportObject.latitude, airportObject.longitude));
	};
	
	$scope.loadingSingleStats = false;
	$scope.retrieveDetailedStats = function () {
		$scope.loadingSingleStats = true;
		$http.get('/api/stats/airports/detailed/' + $scope.airportObject.iata_code).then(function (data) {
			if (data) {
				$scope.loadingSingleStats = false;
				$scope.detailedAirportData = data.data;
				if (data.data != undefined && data.data.usersOnAirport != undefined) {
					$scope.prepareUsersOnTheRoute(data.data.usersOnAirport);
				}
				if (data.data != undefined && data.data.connectionAirports != undefined && data.data.connectionAirports.length > 0) {
					$scope.setRoutesFromTheAirport(data.data.connectionAirports);
				}
			}
		});
	};
	$scope.retrieveDetailedStats();
	
	$scope.prepareUsersOnTheRoute = function (userMap) {
		var usersOnAirport = [];
		for (var user in userMap) {
			usersOnAirport.push({
				"username" : user,
				"times" : userMap[user]
			});
		}
		usersOnAirport.sort(function(a, b) {
			return parseInt(b.value) - parseInt(a.value);
		});
		$scope.usersToShow = [];
		if (usersOnAirport.length > 0) {
			for (var c = 0; c < 2; c ++) {
				if (usersOnAirport[c] != undefined) {
					$scope.usersToShow.push(usersOnAirport[c]);
				}
			}
		}
	}
	
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
	
	$scope.setRoutesFromTheAirport = function (routesObj) {
		setTimeout(function(){ 
			var mapCanvasId = 'routes-from-airport-map',
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
			setFlights($scope.routesMap, routesObj, $scope.airportObject);  
		}, 500); 
	};
	
	function setFlights(map, flights, homeAirport) {
		for (var i = $scope.flightMarkersOnMap.length - 1; i >= 0; i--) {
			$scope.flightMarkersOnMap[i].setMap(null);
		}; 
		var bounds = new google.maps.LatLngBounds();
		for (var j = flights.length - 1; j >= 0; j--) {
			var markerDeparture = new google.maps.Marker({
				position: {
					lat : flights[j].longitude,
					lng: flights[j].latitude
				},
				map: map,
				icon: "img/airport_small.png",
				title: "Airport"
			});
			var markerArrival = new google.maps.Marker({
				position: {
					lat : homeAirport.longitude,
					lng: homeAirport.latitude
				},
				map: map,
				icon: "img/airport_small.png",
				title: "Airport"
			});
			var infowindowDep = new google.maps.InfoWindow({content: flights[j].iata_code});
			google.maps.event.addListener(markerDeparture, 'click', 
					function (infowindow, markerDeparture) {
				return function () {
					infowindow.open(map, markerDeparture);
				};
			}(infowindowDep, markerDeparture));
			var infowindowArr = new google.maps.InfoWindow({content: homeAirport.iata_code});
			google.maps.event.addListener(markerArrival, 'click', 
					function (infowindow, markerArrival) {
				return function () {
					infowindow.open(map, markerArrival);
				};
			}(infowindowArr, markerArrival));
			markerDeparture.setMap(map);
			markerArrival.setMap(map);
			$scope.flightMarkersOnMap.push(markerDeparture);
			$scope.flightMarkersOnMap.push(markerArrival);
			bounds.extend(new google.maps.LatLng(flights[j].longitude, flights[j].latitude));
			bounds.extend(new google.maps.LatLng(homeAirport.longitude, homeAirport.latitude));
			var flightPlanCoordinates = [{lat: flights[j].longitude, lng: flights[j].latitude},
			                             {lat: homeAirport.longitude, lng: homeAirport.latitude}];
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

	$scope.cancel = function () {
		$uibModalInstance.close();
	};
});