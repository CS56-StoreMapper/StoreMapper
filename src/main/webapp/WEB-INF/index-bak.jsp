<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StoreMapper</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
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
        #name-search {
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
        #reset-button {
            background-color: #f44336;
        }
        #reset-button:hover {
            background-color: #da190b;
        }
        #map {
            height: 500px;
            margin-bottom: 20px;
            border: 1px solid #ccc;
            border-radius: 4px;
        }
        #results-list {
            height: 200px;
            overflow-y: auto;
            border: 1px solid #ccc;
            padding: 10px;
            border-radius: 4px;
        }
        #results-list div {
            padding: 5px 0;
            cursor: pointer;
            transition: background-color 0.3s;
        }
        #results-list div:hover {
            background-color: #f0f0f0;
        }
    </style>
</head>
<body>
    <h1>StoreMapper</h1>
    <div id="search-container">
        <p>Debug: Categories: ${categories}</p>
        <select id="category-select">
            <c:forEach var="category" items="${categories}">
                <option value="${category}">${category}</option>
            </c:forEach>
        </select>
        <input type="text" id="name-search" placeholder="Search by name...">
        <select id="type-select"></select>
        <input type="number" id="radius-input" placeholder="Radius (km)" value="5">
        <button onclick="performSearch()">Search</button>
        <button onclick="findNearest()">Find Nearest</button>
        <button onclick="findWithinRadius()">Find Within Radius</button>
        <button id="reset-button">Reset Map</button>
    </div>
    <div id="map"></div>
    <div id="results-list"></div>

    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
    <link rel="stylesheet" href="https://unpkg.com/leaflet.markercluster@1.4.1/dist/MarkerCluster.css" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet.markercluster@1.4.1/dist/MarkerCluster.Default.css" />
    <script src="https://unpkg.com/leaflet.markercluster@1.4.1/dist/leaflet.markercluster.js"></script>
    <script>
        var map, markers = L.markerClusterGroup();

        // Using EL to pass Java objects as JSON to JavaScript
        var cuisineTypes = ${cuisineTypesJson};
        var shopTypes = ${shopTypesJson};

        // Example of using these variables:
        console.log("Number of cuisine types:", cuisineTypes.length);
        console.log("First shop type:", shopTypes[0]);

        function initMap() {
            map = L.map('map').setView([34.0522, -118.2437], 12);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
            map.addLayer(markers);
    
            document.getElementById('category-select').addEventListener('change', updateTypeOptions);
            updateTypeOptions();
        }

        function updateTypeOptions() {
            var category = document.getElementById('category-select').value;
            var typeSelect = document.getElementById('type-select');
            typeSelect.innerHTML = '<option value="">All Types</option>';
            var types = category === 'restaurant' ? cuisineTypes : shopTypes;
            types.forEach(type => {
                if (type) {
                    var option = document.createElement('option');
                    option.value = type;
                    option.textContent = type;
                    typeSelect.appendChild(option);
                }
            });
        }

        function showLoading() {
            document.body.style.cursor = 'wait';
            // You could also add a spinning icon or text to the page
        }
        
        function hideLoading() {
            document.body.style.cursor = 'default';
            // Remove the spinning icon or text if you added one
        }
        
        function performSearch() {
            showLoading();
            var category = document.getElementById('category-select').value;
            var name = document.getElementById('name-search').value;
            var type = document.getElementById('type-select').value;
            var radius = document.getElementById('radius-input').value;
            var center = map.getCenter();
        
            fetch(`/search?category=${encodeURIComponent(category)}&name=${encodeURIComponent(name)}&type=${encodeURIComponent(type)}&radius=${encodeURIComponent(radius)}&lat=${center.lat}&lon=${center.lng}`)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(locations => {
                    displayLocations(locations);
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An error occurred while searching. Please try again.');
                });
        }
        
        function findNearest() {
            map.locate({setView: true, maxZoom: 16})
                .on('locationfound', function(e) {
                    fetch('/nearest?lat=' + e.latlng.lat + '&lon=' + e.latlng.lng)
                        .then(response => response.json())
                        .then(location => {
                            displayLocations([location]);
                            L.marker(e.latlng).addTo(map).bindPopup("You are here").openPopup();
                        });
                });
        }
        
        function findWithinRadius() {
            var category = document.getElementById('category-select').value;
            var type = document.getElementById('type-select').value;
            var radius = document.getElementById('radius-input').value;
            map.locate({setView: false})
                .on('locationfound', function(e) {
                    fetch('/search?category=' + encodeURIComponent(category) + 
                          '&type=' + encodeURIComponent(type) + 
                          '&radius=' + encodeURIComponent(radius) + 
                          '&lat=' + e.latlng.lat + 
                          '&lon=' + e.latlng.lng)
                        .then(function(response) { return response.json(); })
                        .then(function(locations) {
                            displayLocations(locations);
                            L.marker(e.latlng).addTo(map).bindPopup("You are here").openPopup();
                            L.circle(e.latlng, {radius: radius * 1000}).addTo(map); // Convert km to meters
                        })
                        .catch(function(error) {
                            console.error('Error:', error);
                            alert('An error occurred while searching. Please try again.');
                        });
                })
                .on('locationerror', function(e) {
                    console.error('Location error:', e);
                    alert('Unable to find your location. Please ensure location services are enabled.');
                });
        }
        
        function displayLocations(locations) {
            markers.clearLayers();
            var resultsList = document.getElementById('results-list');
            resultsList.innerHTML = '';
        
            locations.forEach(location => {
                var marker = L.marker([location.coordinates.latitude, location.coordinates.longitude])
                    .bindPopup(createPopupContent(location));
                markers.addLayer(marker);
        
                var listItem = document.createElement('div');
                listItem.textContent = createListItemContent(location);
                listItem.addEventListener('click', () => {
                    map.setView([location.coordinates.latitude, location.coordinates.longitude], 16);
                    marker.openPopup();
                });
                resultsList.appendChild(listItem);
            });
        
            if (locations.length > 0) {
                map.fitBounds(markers.getBounds());
            } else {
                alert('No locations found');
            }
        }

        
        function createPopupContent(location) {
            var content = '<strong>' + location.osmNode.tags.name + '</strong><br>';
            content += 'Latitude: ' + location.coordinates.latitude.toFixed(6) + '<br>';
            content += 'Longitude: ' + location.coordinates.longitude.toFixed(6) + '<br>';
            if (location.cuisineType) content += 'Cuisine: ' + location.cuisineType + '<br>';
            if (location.shopType) content += 'Shop Type: ' + location.shopType + '<br>';
            if (location.osmNode.tags.amenity) content += 'Amenity: ' + location.osmNode.tags.amenity + '<br>';
            if (location.osmNode.tags.shop) content += 'Shop: ' + location.osmNode.tags.shop + '<br>';
            if (location.osmNode.tags.brand) content += 'Brand: ' + location.osmNode.tags.brand + '<br>';
            if (location.osmNode.tags['addr:street']) content += 'Street: ' + location.osmNode.tags['addr:street'] + '<br>';
            if (location.osmNode.tags['addr:housenumber']) content += 'House Number: ' + location.osmNode.tags['addr:housenumber'] + '<br>';
            return content;
        }
        
        function createListItemContent(location) {
            var content = location.osmNode.tags.name || 'Unnamed Location';
            if (location.cuisineType) content += ' - Cuisine: ' + location.cuisineType;
            if (location.shopType) content += ' - Shop Type: ' + location.shopType;
            return content;
        }

        function resetMap() {
            // Reset the map view to the initial position and zoom level
            map.setView([34.0522, -118.2437], 12);
            
            // Clear all markers
            markers.clearLayers();
            
            // Reset the search inputs
            document.getElementById('category-select').value = 'restaurant';
            document.getElementById('name-search').value = '';
            updateTypeOptions(); // This will reset the type select based on the category
            document.getElementById('radius-input').value = '5';
            
            // Clear the results list
            document.getElementById('results-list').innerHTML = '';
            
            // Remove any circles (used in findWithinRadius)
            map.eachLayer(function (layer) {
                if (layer instanceof L.Circle) {
                    map.removeLayer(layer);
                }
            });
            
            // Optionally, we could load all locations here if we want to show something on reset
            // loadAllLocations();
        }

        initMap();

        // Adding this line to make sure the reset button is connected to the function
        document.getElementById('reset-button').addEventListener('click', resetMap);
    </script>
</body>
</html>