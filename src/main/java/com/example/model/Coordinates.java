package com.example.model;

/**
 * Represents a geographic coordinate with latitude and longitude.
 */
public class Coordinates {
    /** The latitude of the coordinate. */
    private double latitude;
    /** The longitude of the coordinate. */
    private double longitude;

    /**
     * Constructs a new Coordinates object.
     *
     * @param latitude  The latitude of the coordinate.
     * @param longitude The longitude of the coordinate.
     */
    public Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the latitude of the coordinate.
     *
     * @return The latitude value.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Gets the longitude of the coordinate.
     *
     * @return The longitude value.
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Calculates the distance to another coordinate.
     *
     * @param other The other coordinate to calculate the distance to.
     * @return The distance between this coordinate and the other coordinate.
     */
    public double distanceTo(Coordinates other) {
        // To be implemented
        return 0;
    }

    /**
     * Returns a string representation of the coordinate.
     *
     * @return A string representation of the coordinate.
     */
    @Override
    public String toString() {
        return String.format("Coordinates{latitude=%f, longitude=%f}", latitude, longitude);
    }
}