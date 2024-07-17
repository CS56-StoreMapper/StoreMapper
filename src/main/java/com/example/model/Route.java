package com.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a route between two nodes.
 */
public class Route {
    private final List<Node> nodes;
    private double totalDistance;

    public Route(List<Node> nodes) {
        this.nodes = nodes;
        this.totalDistance = calculateTotalDistance();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    private double calculateTotalDistance() {
        double distance = 0.0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            distance += nodes.get(i).toCoordinates().distanceTo(nodes.get(i + 1).toCoordinates());
        }
        return distance;
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
     * @return A string describing the route, including start, end, total distance, and number of nodes.
     */
    @Override
    public String toString() {
        if (nodes.isEmpty()) {
            return "Empty route";
        }
        Node start = nodes.get(0);
        Node end = nodes.get(nodes.size() - 1);
        return String.format("Route from %s to %s, distance: %.2f km, nodes: %d",
            start.toCoordinates(), end.toCoordinates(), totalDistance, nodes.size());
    }
}
