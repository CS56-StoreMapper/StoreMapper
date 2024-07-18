package com.example.model;

public record Bounds(double minlat, double minlon, double maxlat, double maxlon) {
    public boolean contains(Coordinates coordinates) {
        return coordinates.getLatitude() >= minlat && coordinates.getLatitude() <= maxlat &&
               coordinates.getLongitude() >= minlon && coordinates.getLongitude() <= maxlon;
    }

    @Override
    public String toString() {
        return String.format("Bounds(minlat=%.6f, minlon=%.6f, maxlat=%.6f, maxlon=%.6f)", 
                             minlat, minlon, maxlat, maxlon);
    }
}
