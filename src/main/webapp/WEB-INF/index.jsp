<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StoreMapper</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
    <style>
        #map { height: 500px; margin-bottom: 20px; }
        #controls { display: flex; gap: 10px; margin-bottom: 20px; }
        #searchInput { flex-grow: 1; }
        #locationList { height: 200px; overflow-y: auto; border: 1px solid #ccc; padding: 10px; }
    </style>
</head>
<body>
    <h1>StoreMapper</h1>
    <div id="controls">
        <input type="text" id="searchInput" placeholder="Search locations...">
        <button onclick="searchLocations()">Search</button>
        <button onclick="findNearest()">Find Nearest</button>
        <input type="number" id="radiusInput" placeholder="Radius (miles)">
        <button onclick="findWithinRadius()">Find Within Radius</button>
    </div>
    <div id="map"></div>
    <div id="locationList"></div>

    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
    <script>
        var map = L.map('map').setView([34.0168, -118.4707], 12);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
        let markers = L.featureGroup().addTo(map);

        function loadAllLocations() {
            fetch('/locations')
                .then(response => response.json())
                .then(locations => {
                    displayLocations(locations);
                });
        }

        function searchLocations() {
            const query = document.getElementById('searchInput').value;
            fetch('/locations/search?query=' + encodeURIComponent(query))
                .then(response => response.json())
                .then(locations => {
                    displayLocations(locations);
                });
        }

        function findNearest() {
            map.locate({setView: true, maxZoom: 16})
                .on('locationfound', function(e) {
                    fetch('/locations/nearest?lat=' + e.latlng.lat + '&lon=' + e.latlng.lng)
                        .then(response => response.json())
                        .then(location => {
                            displayLocations([location]);
                            L.marker(e.latlng).addTo(map).bindPopup("You are here").openPopup();
                        });
                });
        }

        function findWithinRadius() {
            const radius = document.getElementById('radiusInput').value;
            map.locate({setView: false})
                .on('locationfound', function(e) {
                    fetch('/locations/within?lat=' + e.latlng.lat + '&lon=' + e.latlng.lng + '&radius=' + radius)
                        .then(response => response.json())
                        .then(locations => {
                            displayLocations(locations);
                            L.marker(e.latlng).addTo(map).bindPopup("You are here").openPopup();
                            L.circle(e.latlng, {radius: radius * 1609.34}).addTo(map); // Convert miles to meters
                        });
                });
        }

        function displayLocations(locations) {
            markers.clearLayers();
            var listHtml = '<ul>';
            locations.forEach(function(location) {
                var lat = location.coordinates.latitude;
                var lon = location.coordinates.longitude;
                var marker = L.marker([lat, lon])
                    .bindPopup(createPopupContent(location));
                markers.addLayer(marker);
                listHtml += '<li>' + createListItemContent(location) + '</li>';
            });
            listHtml += '</ul>';
            document.getElementById('locationList').innerHTML = listHtml;
            if (locations.length > 0) {
                map.fitBounds(markers.getBounds());
            }
        }
        
        function createPopupContent(location) {
            var content = '<strong>' + location.osmNode.tags.name + '</strong><br>';
            content += 'Latitude: ' + location.coordinates.latitude.toFixed(6) + '<br>';
            content += 'Longitude: ' + location.coordinates.longitude.toFixed(6) + '<br>';
            if (location.osmNode.tags.amenity) content += 'Amenity: ' + location.osmNode.tags.amenity + '<br>';
            if (location.osmNode.tags.shop) content += 'Shop: ' + location.osmNode.tags.shop + '<br>';
            if (location.osmNode.tags.brand) content += 'Brand: ' + location.osmNode.tags.brand + '<br>';
            if (location.osmNode.tags['addr:street']) content += 'Street: ' + location.osmNode.tags['addr:street'] + '<br>';
            if (location.osmNode.tags['addr:housenumber']) content += 'House Number: ' + location.osmNode.tags['addr:housenumber'] + '<br>';
            return content;
        }
        
        function createListItemContent(location) {
            var content = location.osmNode.tags.name + ' (' + location.coordinates.latitude.toFixed(6) + ', ' + location.coordinates.longitude.toFixed(6) + ')';
            if (location.osmNode.tags.amenity) content += ' - ' + location.osmNode.tags.amenity;
            if (location.osmNode.tags.shop) content += ' - ' + location.osmNode.tags.shop;
            return content;
        }
        loadAllLocations();
    </script>
</body>
</html>