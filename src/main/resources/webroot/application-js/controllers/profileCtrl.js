flightDiaryApp.controller('profileCtrl',
		function($rootScope, $routeParams, $window, $scope, $http, $location) {
	
	$scope.username = $routeParams.username;
	
	$scope.loading = false;
	
	$scope.retrieveUserFlights = function () {
		$scope.loading = true;
		$http.get('/api/flights/' + $scope.username).then(function (data) {
			$scope.flights = data.data;
			if (data.data.length == 0) {
				$window.location.href = "/#/";
			}
			$scope.initMap();
			$scope.parseFlights();
		}, function (data) {
			$window.location.href = "/#/";
		});
	};
	$scope.retrieveUserFlights();
	
	/* Map manipulation*/
	$scope.map = "";
	var myLatlng = new google.maps.LatLng(44, 20.461414);
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

	$scope.initMap = function () {

		setTimeout(function(){ 
			console.log("Initialize map");
			var mapCanvasId = 'map-holder',
			myOptions = {
					center: myLatlng,
					streetViewControl: false,
					mapTypeControlOptions: {
						mapTypeIds: [google.maps.MapTypeId.ROADMAP, customMapTypeId]
					},
					zoom: 7,
					zoomControlOptions: {
						style: google.maps.ZoomControlStyle.LARGE,
						position: google.maps.ControlPosition.LEFT_CENTER
					}
			}
			$scope.map = new google.maps.Map(document.getElementById(mapCanvasId), myOptions);
			$scope.map.mapTypes.set(customMapTypeId, customMapType);
			$scope.map.setMapTypeId(customMapTypeId);
			setFlights($scope.map, $scope.flights);  
		}, 500); 
	};


	function setFlights(map, flights) {
		for (var i = $scope.markersOnMap.length - 1; i >= 0; i--) {
			scope.markersOnMap[i].setMap(null);
		}; 
		var bounds = new google.maps.LatLngBounds();
		for (var j = flights.length - 1; j >= 0; j--) {
			var markerDeparture = new google.maps.Marker({
				position: {
					lat : flights[j].flightData.departure_latitude,
					lng: flights[j].flightData.departure_longitude
				},
				map: map,
				icon: "img/airport_small.png",
				title: "Airport"
			});
			var markerArrival = new google.maps.Marker({
				position: {
					lat : flights[j].flightData.arrival_latitude,
					lng: flights[j].flightData.arrival_longitude
				},
				map: map,
				icon: "img/airport_small.png",
				title: "Airport"
			});
			var infowindowDep = new google.maps.InfoWindow({content: flights[j].flight.departure});
			google.maps.event.addListener(markerDeparture, 'click', 
					function (infowindow, markerDeparture) {
				return function () {
					infowindow.open(map, markerDeparture);
				};
			}(infowindowDep, markerDeparture));
			var infowindowArr = new google.maps.InfoWindow({content: flights[j].flight.arrival});
			google.maps.event.addListener(markerArrival, 'click', 
					function (infowindow, markerArrival) {
				return function () {
					infowindow.open(map, markerArrival);
				};
			}(infowindowArr, markerArrival));
			markerDeparture.setMap($scope.map);
			markerArrival.setMap($scope.map);
			$scope.markersOnMap.push(markerDeparture);
			$scope.markersOnMap.push(markerArrival);
			bounds.extend(new google.maps.LatLng(flights[j].flightData.departure_latitude, flights[j].flightData.departure_longitude));
			bounds.extend(new google.maps.LatLng(flights[j].flightData.arrival_latitude, flights[j].flightData.arrival_longitude));
			var flightPlanCoordinates = [{lat: flights[j].flightData.departure_latitude, lng: flights[j].flightData.departure_longitude},
			                             {lat: flights[j].flightData.arrival_latitude, lng: flights[j].flightData.arrival_longitude}];
			var flightPath = new google.maps.Polyline({
				path: flightPlanCoordinates,
				geodesic: true,
				strokeColor: '#000099',
				strokeOpacity: 1.0,
				strokeWeight: 2
			});

			flightPath.setMap($scope.map);
		}
		$scope.map.fitBounds(bounds);

	};
	
	
	$scope.parseFlights = function () {
		$scope.distinctAirlines = {};
		$scope.distinctAirports = {};
		$scope.totalDistance = 0;
		for (var i=0; i < $scope.flights.length; i++) {
			if ($scope.distinctAirlines[$scope.flights[i].flight.airline_code] == undefined) {
				$scope.distinctAirlines[$scope.flights[i].flight.airline_code] = 1;
			}
			if ($scope.distinctAirports[$scope.flights[i].flight.arrival] == undefined) {
				$scope.distinctAirports[$scope.flights[i].flight.arrival] = 1;
			}
			if ($scope.distinctAirports[$scope.flights[i].flight.departure] == undefined) {
				$scope.distinctAirports[$scope.flights[i].flight.departure] = 1;
			}
			$scope.totalDistance += $scope.flights[i].flightData.distance;
		}
		$scope.distinctAirports = Object.keys($scope.distinctAirports).length;
		$scope.distinctAirlines = Object.keys($scope.distinctAirlines).length;
		$scope.totalDistance = $rootScope.roundRating($scope.totalDistance);
	};

	
});