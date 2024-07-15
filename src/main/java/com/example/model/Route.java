package com.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a route between two coordinates.
 */
public final class Route {
    /** The starting coordinates of the route. */
    private final Node start;
    /** The ending coordinates of the route. */
    private final Node end;
    /** The list of waypoints for the route. */
    private List<Node> waypoints;
    /** The total distance of the route in kilometers. */
    private double totalDistance;

    /**
     * Constructs a new Route object.
     *
     * @param startPoint The starting coordinates of the route.
     * @param endPoint   The ending coordinates of the route.
     */
    public Route(Node startPoint, Node endPoint) {
        this.start = startPoint;
        this.end = endPoint;
        this.waypoints = List.of(start, end);
        this.totalDistance = calculateTotalDistance();
    }

    public Route(List<Node> waypoints) {
        if (waypoints.size() < 2) {
            throw new IllegalArgumentException("At least two waypoints are required to form a route.");
        }
        this.waypoints = List.copyOf(waypoints);
        this.start = waypoints.get(0);
        this.end = waypoints.get(waypoints.size() - 1);
        this.totalDistance = calculateTotalDistance();
    }

    /**
     * Gets the starting coordinates of the route.
     *
     * @return The starting coordinates.
     */
    public Node getStart() {
        return start;
    }

    /**
     * Gets the ending coordinates of the route.
     *
     * @return The ending coordinates.
     */
    public Node getEnd() {
        return end;
    }

    /**
     * Gets the list of waypoints for the route.
     *
     * @return A list of coordinates representing the waypoints.
     */
    public List<Node> getWaypoints() {
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
    public double calculateTotalDistance() {
        return waypoints.stream()
            .mapToDouble(node -> {
                int index = waypoints.indexOf(node);
                if (index < waypoints.size() - 1) {
                    return node.toCoordinates().distanceTo(waypoints.get(index + 1).toCoordinates());
                }
                return 0.0;
            })
            .sum();
    }

    public boolean containsNode(Node node) {
        return waypoints.contains(node);
    }
    
    public int getNodeCount() {
        return waypoints.size();
    }

    /**
     * Returns a string representation of the Route.
     *
     * @return A string describing the route, including start, end, and total
     *         distance.
     */
    @Override
    public String toString() {
        return "Route from " + start.toCoordinates() + " to " + end.toCoordinates() + 
            ", distance: " + String.format("%.2f", totalDistance) + " km, waypoints: " + waypoints.size();
    }
}
