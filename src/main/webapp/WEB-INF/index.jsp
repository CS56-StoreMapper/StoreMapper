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
        <input type="number" id="radius-input" placeholder="Radius (km)" value="10">
        <button onclick="performSearch()">Search</button>
    </div>
    <div id="map"></div>
    <div id="results"></div>

    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
    <script>
        var map = L.map('map', { scrollWheelZoom: false }).setView([34.0522, -118.2437], 10);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

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
                        if (layer instanceof L.Marker) {
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

                    if (locations.length > 0) {
                        map.fitBounds(bounds);
                    } else {
                        console.log('No locations found');
                        alert('No locations found for the given search criteria.');
                    }

                    // Update results list
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
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An error occurred while searching. Please try again.');
                });
        }

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
            return content;
        }
        
        function createListItemContent(location) {
            var content = location.osmNode.tags.name || 'Unnamed Location';
            if (location.cuisineType) content += ' - Cuisine: ' + location.cuisineType;
            if (location.shopType) content += ' - Shop Type: ' + location.shopType;
            return content;
        }
    </script>
</body>
</html>