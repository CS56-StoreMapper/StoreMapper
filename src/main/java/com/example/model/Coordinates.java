package com.example.model;

import java.util.Objects;

/**
 * Represents a geographic coordinate with latitude and longitude.
 */
public class Coordinates {
    /** The latitude of the coordinate. */
    private final double latitude;
    /** The longitude of the coordinate. */
    private final double longitude;

    public static final double NORTH = 0.0;
    public static final double EAST = 90.0;
    public static final double SOUTH = 180.0;
    public static final double WEST = 270.0;

    /**
     * Constructs a new Coordinates object.
     *
     * @param latitude  The latitude of the coordinate.
     * @param longitude The longitude of the coordinate.
     */
    public Coordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
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
     * Calculates the great-circle distance between this coordinate and another coordinate.
     * This method uses the Haversine formula to account for the Earth's curvature.
     *
     * @param other The other coordinate to calculate the distance to
     * @return The distance in kilometers
     *
     * Note: This method assumes a spherical Earth model with a radius of 6371 km.
     *       For very long distances or high-precision applications, consider using
     *       a more accurate model that accounts for the Earth's ellipsoidal shape.
     *
     * The calculation steps are as follows:
     * 1. Convert latitude and longitude differences to radians
     * 2. Calculate the Haversine formula components:
     *    a = sin²(Δlat/2) + cos(lat1) * cos(lat2) * sin²(Δlon/2)
     * 3. Calculate the central angle: c = 2 * atan2(√a, √(1-a))
     * 4. Multiply by Earth's radius to get the distance: d = R * c
     *
     * @see <a href="https://en.wikipedia.org/wiki/Haversine_formula">Haversine formula</a>
     */
    public double distanceTo(Coordinates other) {
        double R = 6371; // Earth's radius in kilometers
        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }  

    /**
     * Calculates a new coordinate point given a starting point, distance, and bearing.
     * This method uses spherical trigonometry to account for the Earth's curvature.
     *
     * @param start The starting coordinate point
     * @param distanceKm The distance to travel in kilometers
     * @param bearingDegrees The bearing in degrees (0° is north, 90° is east, 180° is south, 270° is west)
     * @return A new Coordinates object representing the destination point
     *
     * Note: This method assumes a spherical Earth model with a radius of 6371 km.
     *       For very long distances or high-precision applications, consider using
     *       a more accurate model that accounts for the Earth's ellipsoidal shape.
     *
     * The calculation steps are as follows:
     * 1. Convert distance to radians by dividing by Earth's radius
     * 2. Convert bearing and start coordinates to radians
     * 3. Calculate new latitude using the spherical law of cosines:
     *    - sin(lat1) * cos(d) represents the vertical component due to Earth's curvature
     *    - cos(lat1) * sin(d) * cos(bearing) represents the north-south component of movement
     * 4. Calculate new longitude:
     *    - sin(bearing) * sin(d) * cos(lat1) represents the east-west movement
     *    - cos(d) - sin(lat1) * sin(lat2) adjusts for the Earth's curvature
     * 5. Use atan2 to handle the correct quadrant of the angle for longitude
     * 6. Convert results back to degrees and return new Coordinates
     *
     * @see <a href="https://en.wikipedia.org/wiki/Great-circle_navigation">Great-circle navigation</a>
     */
    public static Coordinates getDestinationPoint(Coordinates start, double distanceKm, double bearingDegrees) {
        final double R = 6371; // Earth's radius in kilometers
        double d = distanceKm / R;
        double bearingRad = Math.toRadians(bearingDegrees);
        double lat1 = Math.toRadians(start.latitude);
        double lon1 = Math.toRadians(start.longitude);
    
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(d) +
                    Math.cos(lat1) * Math.sin(d) * Math.cos(bearingRad));
        double lon2 = lon1 + Math.atan2(Math.sin(bearingRad) * Math.sin(d) * Math.cos(lat1),
                                        Math.cos(d) - Math.sin(lat1) * Math.sin(lat2));
    
        return new Coordinates(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(getLatitude(), that.getLatitude()) == 0 && Double.compare(getLongitude(), that.getLongitude()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLatitude(), getLongitude());
    }
    
    /**
     * Returns a string representation of the coordinate.
     *
     * @return A string representation of the coordinate.
     */
    @Override
    public String toString() {
        return String.format("Coordinates{latitude=%.6f, longitude=%.6f}", latitude, longitude);
    }
}