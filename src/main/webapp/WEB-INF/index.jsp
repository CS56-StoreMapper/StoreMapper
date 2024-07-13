<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>StoreMapper</title>
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.7.1/dist/leaflet.css" />
    <style>
        #map { height: 400px; }
    </style>
</head>
<body>
    <h1>StoreMapper</h1>
    <div id="map"></div>

    <script src="https://unpkg.com/leaflet@1.7.1/dist/leaflet.js"></script>
    <script>
        var map = L.map('map').setView([34.0522, -118.2437], 13);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);

        fetch('/locations')
            .then(response => response.json())
            .then(locations => {
                console.log('Received locations:', locations);
                locations.forEach(location => {
                    L.marker([location.coordinates.latitude, location.coordinates.longitude])
                        .addTo(map)
                        .bindPopup(location.name);
                });
            })
            .catch(error => console.error('Error fetching locations:', error));
    </script>
</body>
</html>