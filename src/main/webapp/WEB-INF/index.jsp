<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StoreMapper</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
    <style>
        #map { height: 600px; }
        
    </style>
</head>
<body>
    <h1>StoreMapper</h1>
    <div id="map"></div>
    <div id="locationList"></div>

    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
    <script>
        var map = L.map('map').setView([34.0168, -118.4707], 13);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
        let markers = L.featureGroup().addTo(map);

        function loadAllLocations() {
            fetch('/locations')
                .then(response => response.json())
                .then(locations => {
                    displayLocations(locations);
                });
        }
        function displayLocations(locations) {
            markers.clearLayers();
            var listHtml = '<ul>';
            locations.forEach(function(location) {
                var marker = L.marker([location.coordinates.latitude, location.coordinates.longitude])
                    .bindPopup(location.name + ' ' + location.coordinates.latitude + ' ' + location.coordinates.longitude);
                markers.addLayer(marker);
                listHtml += '<li>' + location.name + ' (' + location.type + ')</li>';
            });
            listHtml += '</ul>';
            document.getElementById('locationList').innerHTML = listHtml;
            if (locations.length > 0) {
                map.fitBounds(markers.getBounds());
            }
        }
        loadAllLocations();
    </script>
</body>
</html>