package com.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a route between two nodes.
 */
public final class Route {
    /** The starting node of the route. */
    private final Node start;
    /** The ending node of the route. */
    private final Node end;
    /** The list of nodes for the route. */
    private List<Node> nodes;
    /** The total distance of the route in kilometers. */
    private double totalDistance;

    /**
     * Constructs a new Route object.
     *
     * @param startPoint The starting node of the route.
     * @param endPoint   The ending node of the route.
     */
    public Route(Node startPoint, Node endPoint) {
        this.start = startPoint;
        this.end = endPoint;
        this.nodes = List.of(start, end);
        this.totalDistance = calculateTotalDistance();
    }

    /**
     * Constructs a new Route object from a list of waypoints.
     *
     * @param waypoints The list of nodes that make up the route.
     * @throws IllegalArgumentException if less than two waypoints are provided.
     */
    public Route(List<Node> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("At least one waypoint is required to form a route.");
        }
        this.nodes = List.copyOf(nodes);
        this.start = nodes.get(0);
        this.end = nodes.get(nodes.size() - 1);
        this.totalDistance = calculateTotalDistance();
    }

    /**
     * Gets the starting node of the route.
     *
     * @return The starting node.
     */
    public Node getStart() {
        return start;
    }

    /**
     * Gets the ending node of the route.
     *
     * @return The ending node.
     */
    public Node getEnd() {
        return end;
    }

    /**
     * Gets the list of waypoints for the route.
     *
     * @return A list of nodes representing the waypoints.
     */
    public List<Node> getNodes() {
        return nodes;
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
        double distance = 0.0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            distance += nodes.get(i).toCoordinates().distanceTo(nodes.get(i + 1).toCoordinates());
        }
        return distance;
    }

    public boolean containsNode(Node node) {
        return nodes.contains(node);
    }
    
    public int getNodeCount() {
        return nodes.size();
    }

    public double estimateTravelTime(double averageSpeedKmh) {
        return totalDistance / averageSpeedKmh;
    }

    public List<String> getTurnByTurnDirections() {
        List<String> directions = new ArrayList<>();
        // Implement logic to generate turn-by-turn directions
        // This might involve comparing the bearing between consecutive nodes
        return directions;
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
            ", distance: " + String.format("%.2f", totalDistance) + " km, nodes: " + nodes.size();
    }
}
