package com.example.model;

import java.util.List;

/**
 * Represents a route between two coordinates.
 */
public final class Route {
    /** The starting coordinates of the route. */
    private Coordinates start;
    /** The ending coordinates of the route. */
    private Coordinates end;
    /** The list of waypoints for the route. */
    private List<Coordinates> waypoints;
    /** The total distance of the route in kilometers. */
    private double totalDistance;

    /**
     * Constructs a new Route object.
     *
     * @param startPoint The starting coordinates of the route.
     * @param endPoint   The ending coordinates of the route.
     */
    public Route(Coordinates startPoint, Coordinates endPoint) {
        this.start = startPoint;
        this.end = endPoint;
        this.waypoints = List.of(start, end);
        this.totalDistance = calculateTotalDistance();
    }

    /**
     * Gets the starting coordinates of the route.
     *
     * @return The starting coordinates.
     */
    public Coordinates getStart() {
        return start;
    }

    /**
     * Gets the ending coordinates of the route.
     *
     * @return The ending coordinates.
     */
    public Coordinates getEnd() {
        return end;
    }

    /**
     * Gets the list of waypoints for the route.
     *
     * @return A list of coordinates representing the waypoints.
     */
    public List<Coordinates> getWaypoints() {
        return waypoints;
    }

    /**
     * Gets the total distance of the route.
     *
     * @return The total distance in kilometers.
     */
    public double getTotalDistance() {
        return totalDistance;
    }

    /**
     * Calculates the total distance of the route.
     *
     * @return The calculated total distance in kilometers.
     */
    private double calculateTotalDistance() {
        // TODO: Implement distance calculation
        return 0.0;
    }

    /**
     * Returns a string representation of the Route.
     *
     * @return A string describing the route, including start, end, and total
     *         distance.
     */
    @Override
    public String toString() {
        return "Route from " + start + " to " + end + ", distance: "
                + totalDistance + " km";
    }
}
