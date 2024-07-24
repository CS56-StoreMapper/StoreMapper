<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StoreMapper</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.css" />
    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
    <script src="https://unpkg.com/leaflet-routing-machine@3.2.12/dist/leaflet-routing-machine.js"></script>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            max-width: 1200px;
            margin: 0 auto;
        }
        h1 {
            color: #333;
            margin-bottom: 20px;
        }
        #search-container {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 20px;
            align-items: center;
        }
        #search-container > * {
            height: 36px;
            padding: 0 10px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }
        #category-select, #type-select {
            min-width: 120px;
        }
        #search-input {
            flex-grow: 1;
            min-width: 200px;
        }
        #radius-input {
            width: 80px;
        }
        button {
            background-color: #4CAF50;
            color: white;
            border: none;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        button:hover {
            background-color: #45a049;
        }
        #map {
            height: 500px;
            margin-bottom: 20px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }
        #results {
            height: 200px;
            overflow-y: auto;
            border: 1px solid #ccc;
            padding: 10px;
            border-radius: 4px;
        }
        #results div {
            padding: 5px 0;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        #results div:hover {
            background-color: #f0f0f0;
        }
        #legend {
            position: absolute;
            top: 10px;
            right: 10px;
            background: white;
            padding: 10px;
            border-radius: 5px;
            border: 1px solid #ccc;
            z-index: 1000;
            box-shadow: 0 1px 5px rgba(0, 0, 0, 0.4);
        }
        #legend h4 {
            margin: 0 0 5px 0;
            font-size: 14px;
        }
        
        .legend-item {
            margin-bottom: 3px;
            font-size: 12px;
        }
        
        .legend-color {
            display: inline-block;
            width: 15px;
            height: 8px;
            margin-right: 5px;
        }
        
        .leaflet-interactive.route-line {
            stroke: black;
            stroke-width: 1;
            stroke-opacity: 1;
        } 

        @keyframes pulse {
            0% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0.7); }
            70% { box-shadow: 0 0 0 10px rgba(76, 175, 80, 0); }
            100% { box-shadow: 0 0 0 0 rgba(76, 175, 80, 0); }
        }
        
        .pulsating {
            animation: pulse 1s infinite;
        }
        
    </style>
</head>
<body>
    <h1>StoreMapper</h1>
    <div id="search-container">
        <select id="category-select">
            <option value="">All Categories</option>
            <c:forEach var="category" items="${categories}">
                <option value="${category}">${category}</option>
            </c:forEach>
        </select>
        <select id="type-select"></select>
        <input type="text" id="search-input" placeholder="Search...">
        <input type="number" id="radius-input" placeholder="Radius (km)" value="20">
        <button onclick="performSearch()">Search</button>
        <button id="set-location-btn">Set Location</button>
        <button onclick="clearRoute()">Clear Route</button>
    </div>
    <div id="map"></div>
    <div id="results"></div>

    <script>
        var currentRoute = null; 
        var map = L.map('map', { scrollWheelZoom: false });
        var startingMarker;
        var isSettingStartPoint = false;
        var setLocationBtn;


        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

        // Define the bounds
        var bounds = L.latLngBounds(
            [33.965, -118.5129999], // Southwest corner
            [34.07, -118.3849999]   // Northeast corner
        );

        // Fit the map to these bounds
        map.fitBounds(bounds, {maxZoom: 12});

        // Create a custom control for the legend
        var legendControl = L.control({position: 'topright'});

        legendControl.onAdd = function (map) {
            var div = L.DomUtil.create('div', 'info legend');
            div.innerHTML = `
                <h4>Speed Limits</h4>
                <div class="legend-item"><span class="legend-color" style="background-color: red;"></span> ≤ 25 mph</div>
                <div class="legend-item"><span class="legend-color" style="background-color: #FF8C00;"></span> 26-35 mph</div>
                <div class="legend-item"><span class="legend-color" style="background-color: #FFD700;"></span> 36-45 mph</div>
                <div class="legend-item"><span class="legend-color" style="background-color: #30AC45;"></span> > 45 mph</div>
            `;
            return div;
        };
        // Update the CSS for the legend
        var style = document.createElement('style');
        style.textContent = `
            .legend {
                background: white;
                padding: 10px;
                border-radius: 5px;
                border: 1px solid #ccc;
                box-shadow: 0 1px 5px rgba(0,0,0,0.4);
            }
            .legend h4 {
                margin: 0 0 5px 0;
                font-size: 14px;
            }
            .legend-item {
                margin-bottom: 3px;
                font-size: 12px;
            }
            .legend-color {
                display: inline-block;
                width: 15px;
                height: 8px;
                margin-right: 5px;
            }
        `;

        // Create a custom control for the route information
        var routeInfoControl = L.control({position: 'bottomright'});

        routeInfoControl.onAdd = function (map) {
            this._div = L.DomUtil.create('div', 'info route-info');
            this.update();
            return this._div;
        };

        routeInfoControl.update = function (props) {
            console.log("Route information: ", props);
            if (props) {
                this.lastValidProps = props;
            }
            this._div.innerHTML = '<h4>Route Information</h4>' + (this.lastValidProps ?
                '<b>Distance:</b> ' + this.lastValidProps.distance + ' miles<br />' +
                '<b>Estimated Time:</b> ' + this.lastValidProps.time + ' minutes'
                : 'No route selected');
        };

        // Update the CSS for both controls
        style.textContent += `
            .info {
                background: white;
                padding: 10px;
                border-radius: 5px;
                border: 1px solid #ccc;
                box-shadow: 0 1px 5px rgba(0,0,0,0.4);
            }
            .info h4 {
                margin: 0 0 5px 0;
                font-size: 14px;
            }
            .route-info {
                margin-top: 10px;
            }
        `;



        document.head.appendChild(style);

        // Enable scroll wheel zoom when the map is clicked or touched
        map.on('focus', function() { map.scrollWheelZoom.enable(); });
        // Disable scroll wheel zoom when the map loses focus
        map.on('blur', function() { map.scrollWheelZoom.disable(); });

        

        var cuisineTypes = ${cuisineTypesJson};
        var shopTypes = ${shopTypesJson};

        function updateTypeOptions() {
            var category = document.getElementById('category-select').value;
            var typeSelect = document.getElementById('type-select');
            typeSelect.innerHTML = '<option value="">All Types</option>';
            if (category === 'restaurant') {
                cuisineTypes.forEach(addTypeOption);
            } else if (category === 'store') {
                shopTypes.forEach(addTypeOption);
            } else {
                typeSelect.disabled = true;
                return;
            }
            typeSelect.disabled = false;
        }

        function addTypeOption(type) {
            var option = document.createElement('option');
            option.value = type;
            option.textContent = type;
            document.getElementById('type-select').appendChild(option);
        }

        function addCustomButton(map) {
            var customControl = L.Control.extend({
                options: {
                    position: 'topright'
                },
                onAdd: function (map) {
                    var container = L.DomUtil.create('div', 'leaflet-bar leaflet-control');
                    var button = L.DomUtil.create('a', 'leaflet-control-custom', container);
                    button.innerHTML = '📍'; // Pin emoji as button text
                    button.title = 'Set Starting Point';
                    button.href = '#';
                    button.style.width = '30px';
                    button.style.height = '30px';
                    button.style.lineHeight = '30px';
                    button.style.fontSize = '20px';
                    button.style.textAlign = 'center';
                    button.style.backgroundColor = 'white';
                    button.style.color = 'black';
                    
                    L.DomEvent.on(button, 'click', function(e) {
                        L.DomEvent.stopPropagation(e);
                        toggleSetStartPointMode();
                    });
                    
                    return container;
                }
            });
            map.addControl(new customControl());
        }

        function toggleSetStartPointMode() {
            isSettingStartPoint = !isSettingStartPoint;
            console.log("Setting starting point mode:", isSettingStartPoint);            
            if (isSettingStartPoint) {
                map.getContainer().style.cursor = 'crosshair';
                setLocationBtn.classList.add('pulsating');
                alert('Click on the map to set the starting point');
            } else {
                map.getContainer().style.cursor = '';
                setLocationBtn.classList.remove('pulsating');
                alert('Starting point mode disabled');
            }
        }

        function setStartingLocation(latlng) {
            console.log("Setting starting location:", JSON.stringify(latlng));
            if (!latlng || typeof latlng.lat !== 'number' || typeof latlng.lng !== 'number') {
                console.error("Invalid latlng object provided to setStartingLocation");
                return;
            }

            var latLng = L.latLng(latlng.lat, latlng.lng);

            if (startingMarker) {
                map.removeLayer(startingMarker);
            }
            startingMarker = L.marker(latLng, {
                icon: L.icon({
                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                    iconSize: [25, 41],
                    iconAnchor: [12, 41],
                    popupAnchor: [1, -34],
                    shadowSize: [41, 41]
                })
            }).addTo(map);
            startingMarker.bindPopup("Starting Location").openPopup();
            console.log("New starting location set:", latLng);
            isSettingStartPoint = false; // Turn off the mode after setting
            // cursor back to default
            map.getContainer().style.cursor = '';
            setLocationBtn.classList.remove('pulsating');
        }

        // Modify the existing map click handler
        map.on('click', function(e) {
            console.log("Map clicked, isSettingStartPoint:", isSettingStartPoint);
            if (isSettingStartPoint) {
                setStartingLocation(e.latlng);
            }
        });

        addCustomButton(map);

        document.getElementById('category-select').addEventListener('change', updateTypeOptions);
        updateTypeOptions();

        function performSearch() {
            var category = document.getElementById('category-select').value;
            var type = document.getElementById('type-select').value;
            var searchTerm = document.getElementById('search-input').value;
            var radius = document.getElementById('radius-input').value;
            var center = map.getCenter();

            var url = '/search?category=' + encodeURIComponent(category) +
                      '&type=' + encodeURIComponent(type) +
                      '&name=' + encodeURIComponent(searchTerm) +
                      '&radius=' + encodeURIComponent(radius) +
                      '&lat=' + center.lat +
                      '&lon=' + center.lng;

            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Received data:', data);
                    
                    // Clear existing markers
                    map.eachLayer(layer => {
                        console.log("Layer:", getLayerInfo(layer));
                        if (layer instanceof L.Marker && layer !== startingMarker) {
                            map.removeLayer(layer);
                        }
                    });

                    // Ensure data is an array
                    let locations = Array.isArray(data) ? data : (data.locations || []);

                    // Add new markers and fit bounds
                    var bounds = L.latLngBounds();
                    locations.forEach(location => {
                        if (location && location.coordinates) {
                            L.marker([location.coordinates.latitude, location.coordinates.longitude])
                                .addTo(map)
                                .bindPopup(createPopupContent(location));
                            bounds.extend([location.coordinates.latitude, location.coordinates.longitude]);
                        }
                    });

                    // Include starting marker in bounds if it exists
                    if (startingMarker) {
                        bounds.extend(startingMarker.getLatLng());
                    }

                    if (locations.length > 0) {
                        map.fitBounds(bounds);
                    } else {
                        console.log('No locations found');
                        alert('No locations found for the given search criteria.');
                    }

                    // Update results list
                    updateResultsList(locations);
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An error occurred while searching. Please try again.');
                });
        }

        function getLayerInfo(layer) {
            if (layer instanceof L.Marker) {
                return {
                    type: 'Marker',
                    latLng: layer.getLatLng(),
                    options: layer.options
                };
            } else if (layer instanceof L.TileLayer) {
                return {
                    type: 'TileLayer',
                    options: layer.options
                };
            } else {
                return {
                    type: layer.constructor.name,
                    options: layer.options
                };
            }
        }
        

        function updateResultsList(locations) {
            var resultsDiv = document.getElementById('results');
            resultsDiv.innerHTML = '';
            locations.forEach(location => {
                var listItem = document.createElement('div');
                listItem.textContent = createListItemContent(location);
                listItem.addEventListener('click', () => {
                    map.setView([location.coordinates.latitude, location.coordinates.longitude], 16);
                    L.popup()
                        .setLatLng([location.coordinates.latitude, location.coordinates.longitude])
                        .setContent(createPopupContent(location))
                        .openOn(map);
                });
                resultsDiv.appendChild(listItem);
            });
        }

        document.getElementById('search-input').addEventListener('keyup', function(event) {
            if (event.key === 'Enter') {
                performSearch();
            }
        });



        function createPopupContent(location) {
            var content = '<strong>' + (location.osmNode.tags.name || 'Unnamed Location') + '</strong><br>';
            content += 'Latitude: ' + location.coordinates.latitude.toFixed(6) + '<br>';
            content += 'Longitude: ' + location.coordinates.longitude.toFixed(6) + '<br>';
            if (location.cuisineType) content += 'Cuisine: ' + location.cuisineType + '<br>';
            if (location.shopType) content += 'Shop Type: ' + location.shopType + '<br>';
            if (location.osmNode.tags.amenity) content += 'Amenity: ' + location.osmNode.tags.amenity + '<br>';
            if (location.osmNode.tags.shop) content += 'Shop: ' + location.osmNode.tags.shop + '<br>';
            if (location.osmNode.tags.brand) content += 'Brand: ' + location.osmNode.tags.brand + '<br>';
            if (location.osmNode.tags['addr:street']) content += 'Street: ' + location.osmNode.tags['addr:street'] + '<br>';
            if (location.osmNode.tags['addr:housenumber']) content += 'House Number: ' + location.osmNode.tags['addr:housenumber'] + '<br>';
            content += '<select id="popup-route-type-' + location.id + '">' +
                       '<option value="shortest">Shortest Route</option>' +
                       '<option value="fastest">Fastest Route</option>' +
                       '</select>';
            content += '<button onclick="routeToLocation(' + location.coordinates.latitude + ', ' + location.coordinates.longitude + ', \'' + location.id + '\')">Route to Here</button>';
            return content;
        }
        
        function createListItemContent(location) {
            var content = decodeHTMLEntities(location.osmNode.tags.name) || 'Unnamed Location';
            if (location.cuisineType) content += ' - Cuisine: ' + location.cuisineType;
            if (location.shopType) content += ' - Shop Type: ' + location.shopType;
            return content;
        }

        function decodeHTMLEntities(text) {
            var textArea = document.createElement('textarea');
            textArea.innerHTML = text;
            return textArea.value;
        }

        function routeToLocation(lat, lon, locationId) {
            var routeType = document.getElementById('popup-route-type-' + locationId).value;
            var end = L.latLng(lat, lon);
            
            if (startingMarker) {
                var start = startingMarker.getLatLng();
                createRoute(start, end, routeType);
            } else {
                alert("Please set a starting point first by clicking the '📍' button and then clicking on the map.");
            }
        }

        function createRoute(start, end, routeType) {
            console.log("Start:", start, "End:", end, "Route Type:", routeType);

            const startLat = start.lat;
            const startLon = start.lng;
            const endLat = end.lat;
            const endLon = end.lng;
            
            fetch('/route?startLat=' + startLat + '&startLon=' + startLon + 
                  '&endLat=' + endLat + '&endLon=' + endLon + '&type=' + routeType)
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    }
                    return response.json();
                })
                .then(routeData => {
                    // console.log("Route data received:", JSON.stringify(routeData, null, 2));

                    if (currentRoute) {
                        map.removeLayer(currentRoute);
                    }

                    if (routeData.segments && routeData.segments.length > 0) {
                        currentRoute = L.featureGroup().addTo(map);
                        routeInfoControl.addTo(map);
        
                        routeData.segments.forEach(segment => {
                            var coordinates = [
                                [segment.startLat, segment.startLon],
                                [segment.endLat, segment.endLon]
                            ];
                            var color = getColorForSpeedLimit(segment.speedLimit);
                            createRouteSegment(coordinates, color);
                        });
                    } else if (routeData.coordinates && routeData.coordinates.length > 0) {
                        currentRoute = L.polyline(routeData.coordinates.map(coord => [coord.latitude, coord.longitude]), {
                            color: 'blue',
                            opacity: 0.8,
                            weight: 5
                        }).addTo(map);
                    } else {
                        throw new Error("No valid route data found");
                    }

                    
                    // Show the legend when a route is created
                    legendControl.addTo(map);

                    displayRouteInfo(routeData);

                    // Fit the map to the bounds of the route
                    map.fitBounds(currentRoute.getBounds().pad(0.2));
                })
                .catch(error => {
                    console.error('Error fetching route:', error);
                    alert('Unable to calculate route. Please try again.');
                });
        }

        function createRouteSegment(coordinates, color) {
             // Create the black "stroke" polyline
            L.polyline(coordinates, {
                color: 'black',
                weight: 7,  // Slightly thicker than the colored line
                opacity: 0.1,  // Reduced opacity to make it less prominent
                lineJoin: 'round',
                lineCap: 'round'
            }).addTo(currentRoute);
            // Create the colored polyline on top
            L.polyline(coordinates, {
              color: color,
              weight: 6,
              opacity: 1,
              lineJoin: 'round',
              lineCap: 'round'
            }).addTo(currentRoute);
          }

          function displayRouteInfo(routeData) {
            console.log("Route distance: " + routeData.distance);
            console.log("Route estimated time: " + routeData.estimatedTime);
            let distance = routeData.distance !== undefined ? parseFloat(routeData.distance).toFixed(2) : 'N/A';
            let time = routeData.estimatedTime !== undefined ? parseFloat(routeData.estimatedTime).toFixed(2) : 'N/A';
            
            if (distance !== 'N/A' || time !== 'N/A') {
                routeInfoControl.update({
                    distance: distance,
                    time: time
                });
            } 
        
            // Add the control to the map if it hasn't been added yet
            if (!map.hasLayer(routeInfoControl)) {
                routeInfoControl.addTo(map);
            }
        }

        function getColorForSpeedLimit(speedLimit) {
            if (speedLimit <= 25) return 'red';
            if (speedLimit <= 35) return '#FF8C00'; // Dark Orange
            if (speedLimit <= 45) return '#FFD700'; // Gold
            return '#30AC45'; // Green
        }

        function clearRoute() {
            if (currentRoute) {
                map.removeLayer(currentRoute);
                currentRoute = null;
            }
            map.removeControl(legendControl);
            map.removeControl(routeInfoControl);
        }

        function initializeMap() {
            map = L.map('map').setView([40.712, -74.006], 13);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
        }

        // Add this after all the function definitions and map initialization
        document.addEventListener('DOMContentLoaded', function() {
            var setLocationBtn = document.getElementById('set-location-btn');
            if (setLocationBtn) {
                setLocationBtn.addEventListener('click', toggleSetStartPointMode);
            } else {
                console.error("Set Location button not found");
            }
        });
    </script>
</body>
</html>