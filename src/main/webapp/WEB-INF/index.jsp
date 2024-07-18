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
        #map { height: 400px; width: 100%; }
        #results { margin-top: 20px; }
    </style>
</head>
<body>
    <h1>StoreMapper</h1>
    <div>
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
        var map = L.map('map').setView([34.0522, -118.2437], 10);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

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
                                .bindPopup(location.name);
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
                    resultsDiv.innerHTML = locations.map(location => `<p>${location.name}</p>`).join('');
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('An error occurred while searching. Please try again.');
                });
        }
    </script>
</body>
</html>