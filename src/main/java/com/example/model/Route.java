package com.example.model;

import java.util.ArrayList;
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

    public Route(List<Coordinates> waypoints) {
        if (waypoints.size() < 2) {
            throw new IllegalArgumentException("At least two waypoints are required to form a route.");
        }
        this.waypoints = new ArrayList<>(waypoints);
        this.start = waypoints.get(0);
        this.end = waypoints.get(waypoints.size() - 1);
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
     * Sets the waypoints for the route.
     *
     * @param waypoints A list of coordinates representing the waypoints.
     */
    public void setWaypoints(List<Coordinates> waypoints) {
        this.waypoints = waypoints;
        this.totalDistance = calculateTotalDistance();
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
    public double calculateTotalDistance() {
        double totalDistance = 0;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            totalDistance += waypoints.get(i).distanceTo(waypoints.get(i + 1));
        }
        return totalDistance;
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
