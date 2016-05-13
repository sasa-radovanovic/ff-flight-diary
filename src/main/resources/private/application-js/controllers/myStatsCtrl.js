flightDiaryPrivateApp.controller('myStatsCtrl',
		function($rootScope, $routeParams, $scope, $http, $location, $window, $uibModal, $routeParams, charting) {


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

	$(document).ready(function () {

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
			setFlights($scope.map, $rootScope.myFlights);  
		}, 1500); 
	});


	function setFlights(map, flights) {
		for (var i = $scope.markersOnMap.length - 1; i >= 0; i--) {
			scope.markersOnMap[i].setMap(null);
		}; 
		var bounds = new google.maps.LatLngBounds();
		for (var j = flights.length - 1; j >= 0; j--) {
			flights[j].flightData.departure_longitude
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


	/**/

	$scope.getUserStats = function () {
		$http.get("../api/stats/user").then(function (data) {
			if (data) {
				console.log(JSON.stringify(data.data));
				$scope.detailedStats = data.data;
				$scope.formRegionsGraph(data.data.regionDistributionMap);
				$scope.formYearGraph(data.data.yearDistributionMap);
				$scope.formMonthDistributionGraph(data.data.monthDistributionMap);
				$scope.formTimeDistributionGraph(data.data.timeDistributionMap);
			} 
		}, function (data) {

		});
	};
	$scope.getUserStats();
	
	$scope.formTimeDistributionGraph = function (timeMap) {
		$scope.morningFlights = 0;
		$scope.afternoonFlights = 0;
		$scope.eveningFlights = 0;
		
		if (timeMap['morning'] != undefined) {
			$scope.morningFlights = timeMap['morning'];
		}
		if (timeMap['afternoon'] != undefined) {
			$scope.afternoonFlights = timeMap['afternoon'];
		}
		if (timeMap['evening'] != undefined) {
			$scope.eveningFlights = timeMap['evening'];
		}

		$scope.morningFlights = $rootScope.calculatePerc($scope.morningFlights, $rootScope.myFlights.length);
		$scope.afternoonFlights = $rootScope.calculatePerc($scope.afternoonFlights, $rootScope.myFlights.length);
		$scope.eveningFlights = $rootScope.calculatePerc($scope.eveningFlights, $rootScope.myFlights.length);
	}
	
	$scope.formRegionsGraph = function (regionMap) {
		$scope.regionsGraph = [[]];
		for(var reg in regionMap) {
			var perc = $rootScope.calculatePerc(regionMap[reg], $rootScope.myFlights.length);
			var singleReg = [];
			singleReg.push(reg);
			singleReg.push(perc);
			$scope.regionsGraph[0].push(singleReg);
		}
	};
	
	$scope.formYearGraph = function (yearMap) {
		$scope.yearsGraph = [[]];
		for(var year in yearMap) {
			var perc = $rootScope.calculatePerc(yearMap[year], $rootScope.myFlights.length);
			var singleYear = [];
			singleYear.push(year);
			singleYear.push(perc);
			$scope.yearsGraph[0].push(singleYear);
		}
	};
	
	$scope.formMonthDistributionGraph = function (monthMap) {
		$scope.monthGraph = [];
		if (Object.keys(monthMap).length <= 5) {
			var padd = 8;
			for(var k in monthMap) {
				var val = monthMap[k];
				var perc = (val / ($rootScope.myFlights.length)) * 100;
				if (perc > 90) {
					perc = 90;
				}
				$scope.monthGraph.push({
					"code" : k,
					"valueRaw" : monthMap[k],
					"value" : perc,
					"padd" : padd
				});
				padd += 18;
			} 
		} else {
			var postprocessedMap = [];
			for(var k in monthMap) {
				var val = monthMap[k];
				var perc = (val / ($rootScope.myFlights.length)) * 100;
				postprocessedMap.push({
					"code" : k,
					"valueRaw" : monthMap[k],
					"value" : perc
				});
			}
			postprocessedMap.sort(function(a, b) {
				return parseFloat(b.value) - parseFloat(a.value);
			});
			var others = {
					"code" : "Others",
					"value" : 0,
					"valueRaw" : 0
			};
			var topFourSumPerc = 0;
			var topFourSum = 0;
			var padd = 8;
			for (t = 0; t < 4; t++) {
				topFourSumPerc += postprocessedMap[t].value;
				topFourSum += postprocessedMap[t].valueRaw;
				if (postprocessedMap[t].value > 90) {
					postprocessedMap[t].value = 90;
				}
				postprocessedMap[t].padd = padd;
				padd += 18;
				$scope.monthGraph.push(postprocessedMap[t]);
			}
			others.value = Math.round((100 - topFourSumPerc) * 10) / 10;
			others.valueRaw = $scope.myFlights.length - topFourSum;
			others.padd = padd;
			$scope.monthGraph.push(others);
		}
	};

	$scope.myChartOpts = charting.pieChartOptions;
	
	$scope.airlineMap = {};
	$scope.airlinesUsedHandle = function (airline_code) {
		if ($scope.airlineMap[airline_code] == undefined) {
			$scope.airlineMap[airline_code] = 1;
		} else {
			$scope.airlineMap[airline_code]++;
		}
	};

	$scope.airlinesOnGraph = [];
	$scope.formAirlinesGraphData = function () {
		if (Object.keys($scope.airlineMap).length <= 5) {
			var padd = 8;
			for(var k in $scope.airlineMap) {
				var val = $scope.airlineMap[k];
				var perc = (val / ($rootScope.myFlights.length)) * 100;
				$scope.airlineMap[k]  =  Math.round(perc * 10) / 10;
				$scope.airlinesOnGraph.push({
					"code" : k,
					"value" : $scope.airlineMap[k],
					"padd" : padd
				});
				padd += 18;
			} 
		} else {
			var postprocessedMap = [];
			for(var k in $scope.airlineMap) {
				var val = $scope.airlineMap[k];
				var perc = (val / ($rootScope.myFlights.length)) * 100;
				$scope.airlineMap[k]  =  Math.round(perc * 10) / 10;
				postprocessedMap.push({
					"code" : k,
					"value" : $scope.airlineMap[k]
				});
			}
			postprocessedMap.sort(function(a, b) {
				return parseFloat(b.value) - parseFloat(a.value);
			});
			var others = {
					"code" : "Others",
					"value" : 0
			};
			var topFourSum = 0;
			var padd = 8;
			for (t = 0; t < 4; t++) {
				topFourSum += postprocessedMap[t].value;
				postprocessedMap[t].padd = padd;
				padd += 18;
				$scope.airlinesOnGraph.push(postprocessedMap[t]);
			}
			others.value = Math.round((100 - topFourSum) * 10) / 10;
			others.padd = padd;
			$scope.airlinesOnGraph.push(others);
		}
	}

	$scope.airportsVisited = 0;
	$scope.airportsMap = {};
	$scope.airportsVisitedHandle = function (departure, arrival) {
		if ($scope.airportsMap[departure] == undefined) {
			$scope.airportsMap[departure] = 1;
			$scope.airportsVisited ++;
		} else {
			$scope.airportsMap[departure]++;
		}
		if ($scope.airportsMap[arrival] == undefined) {
			$scope.airportsMap[arrival] = 1;
			$scope.airportsVisited ++;
		} else {
			$scope.airportsMap[arrival]++;
		}
	};

	$scope.airportsOnGraph = [];
	$scope.formAirportsGraphData = function () {
		if (Object.keys($scope.airportsMap).length <= 5) {
			var padd = 8;
			for(var k in $scope.airportsMap) {
				var val = $scope.airportsMap[k];
				var perc = (val / ($rootScope.myFlights.length * 2)) * 100;
				$scope.airportsMap[k]  =  Math.round(perc * 10) / 10;
				$scope.airportsOnGraph.push({
					"code" : k,
					"value" : $scope.airportsMap[k],
					"padd" : padd
				});
				padd += 18;
			} 
		} else {
			var postprocessedMap = [];
			for(var k in $scope.airportsMap) {
				var val = $scope.airportsMap[k];
				var perc = (val / ($rootScope.myFlights.length * 2)) * 100;
				$scope.airportsMap[k]  =  Math.round(perc * 10) / 10;
				postprocessedMap.push({
					"code" : k,
					"value" : $scope.airportsMap[k]
				});
			}
			postprocessedMap.sort(function(a, b) {
				return parseFloat(b.value) - parseFloat(a.value);
			});
			var others = {
					"code" : "Others",
					"value" : 0
			};
			var topFourSum = 0;
			var padd = 8;
			for (t = 0; t < 4; t++) {
				console.log(">> " + t + " " + postprocessedMap[t].code + " " + postprocessedMap[t].value);
				topFourSum += postprocessedMap[t].value;
				postprocessedMap[t].padd = padd;
				padd += 18;
				$scope.airportsOnGraph.push(postprocessedMap[t]);
			}
			others.value = Math.round((100 - topFourSum) * 10) / 10;
			others.padd = padd;
			$scope.airportsOnGraph.push(others);
		}
	}

	$scope.analyzeAll = function () {
		$scope.totalDistance = 0;
		$scope.flightClasses = {
				"first" : 0,
				"business" : 0,
				"economy" : 0
		};
		$scope.purposes = {
				"leasure" : 0,
				"business" : 0,
				"family" : 0
		};
		$scope.sources = {
				"airline" : 0,
				"agent" : 0
		};
		$scope.totalRating = 0;
		$rootScope.myFlights.forEach(function (singleFlight) {
			$scope.totalDistance += singleFlight.flightData.distance;
			if (singleFlight.flight.flight_class == 0) {
				$scope.flightClasses.economy ++;
			} else if (singleFlight.flight.flight_class == 1) {
				$scope.flightClasses.business ++;
			} else {
				$scope.flightClasses.first ++;
			}
			$scope.totalRating += singleFlight.flight.rating;
			if (singleFlight.flight.purpose == 0) {
				$scope.purposes.leasure ++;
			} else if (singleFlight.flight.purpose == 1) {
				$scope.purposes.business ++;
			} else {
				$scope.purposes.family ++;
			}
			if (singleFlight.flight.ticket_source == 'airline') {
				$scope.sources.airline ++;
			} else {
				$scope.sources.agent ++;
			}
			$scope.airportsVisitedHandle(singleFlight.flight.departure, singleFlight.flight.arrival);
			$scope.airlinesUsedHandle(singleFlight.flight.airline_code);
		});
		$scope.formAirportsGraphData();
		$scope.formAirlinesGraphData();
		$scope.stats = [];
		$scope.totalDistance = Math.round($scope.totalDistance * 100) / 100;
		$scope.chartClasses = [];
		if ($scope.flightClasses.first > 0) {
			$scope.chartClasses.push({
				"name" : "First",
				"type" : "alert",
				"value" : $rootScope.calculatePerc($scope.flightClasses.first, $scope.myFlights.length)
			});
		}
		if ($scope.flightClasses.business > 0) {
			$scope.chartClasses.push({
				"name" : "Business",
				"type" : "info",
				"value" : $rootScope.calculatePerc($scope.flightClasses.business, $scope.myFlights.length)
			});
		}
		if ($scope.flightClasses.economy > 0) {
			$scope.chartClasses.push({
				"name" : "Economy",
				"type" : "success",
				"value" : $rootScope.calculatePerc($scope.flightClasses.economy, $scope.myFlights.length)
			});
		}
		$scope.chartSources = [];
		if ($scope.sources.airline > 0) {
			$scope.chartSources.push({
				"name" : "Airline",
				"type" : "danger",
				"value" : $rootScope.calculatePerc($scope.sources.airline, $scope.myFlights.length)
			});
		}
		if ($scope.sources.agent > 0) {
			$scope.chartSources.push({
				"name" : "Agent",
				"type" : "warning",
				"value" : $rootScope.calculatePerc($scope.sources.agent, $scope.myFlights.length)
			});
		}
		$scope.chartPurposes = [];
		if ($scope.purposes.leasure > 0) {
			$scope.chartPurposes.push({
				"name" : "Leasure",
				"type" : "info",
				"value" : $rootScope.calculatePerc($scope.purposes.leasure, $scope.myFlights.length)
			});
		}
		if ($scope.purposes.business > 0) {
			$scope.chartPurposes.push({
				"name" : "Business",
				"type" : "success",
				"value" : $rootScope.calculatePerc($scope.purposes.business, $scope.myFlights.length)
			});
		}
		if ($scope.purposes.family > 0) {
			$scope.chartPurposes.push({
				"name" : "Family Visit",
				"type" : "alert",
				"value" : $rootScope.calculatePerc($scope.purposes.family, $scope.myFlights.length)
			});
		}
		$scope.totalRating = Math.round(($scope.totalRating / $scope.myFlights.length) * 10) / 10;
	};
	
	$scope.max = 5;
	$scope.isReadonly = true;


	$scope.retrieveAll = function () {
		console.log("STATS " + $rootScope.myFlights);
		if ($rootScope.myFlights == undefined) {
			$http.get('../api/flights').then(function (data) {
				if (data) {
					console.log(data);
					if (data.data) {
						$rootScope.myFlights = data.data;
						//setFlights($scope.map, $rootScope.myFlights);  
						$scope.analyzeAll();
					} else {
						$window.location.href = "/private/#/profile/";
					}
				} else {
					$window.location.href = "/private/#/profile/";
				}
			});
		} else {
			//setFlights($scope.map, $rootScope.myFlights);   
			$scope.analyzeAll();
		}
	};

	$scope.retrieveAll();


});