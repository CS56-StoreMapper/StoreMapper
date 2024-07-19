package com.example.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents a route between two nodes.
 */
public class Route {
    private final List<Node> nodes;
    private double totalDistance;
    private Graph graph;

    public Route(List<Node> nodes, Graph graph) {
        this.nodes = nodes;
        this.graph = graph;
        this.totalDistance = calculateTotalDistance();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Coordinates> getCoordinates() {
        return nodes.stream()
                    .map(Node::toCoordinates)
                    .toList();
    }

    public double getTotalDistance() {
        double totalDistance = 0;
        List<Node> nodes = getNodes();
        for (int i = 0; i < nodes.size() - 1; i++) {
            totalDistance += nodes.get(i).toCoordinates().distanceTo(nodes.get(i + 1).toCoordinates());
        }
        return totalDistance;
    }

    public double getEstimatedTime(boolean fastest) {
        double totalTimeHours = 0;
        List<Node> nodes = getNodes();
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node start = nodes.get(i);
            Node end = nodes.get(i + 1);
            Way way = graph.getWay(start, end);
            double distance = start.toCoordinates().distanceTo(end.toCoordinates());
            int speedLimitMph = way.getSpeedLimitMph();
            double segmentTimeHours = distance / (speedLimitMph * 1609.34 / 3600);
            totalTimeHours += segmentTimeHours;
            
            System.out.println("Segment " + i + ": Distance = " + distance + "m, Speed = " + speedLimitMph + "mph, Time = " + (segmentTimeHours * 60) + " minutes");
        }
        System.out.println("Total estimated time: " + (totalTimeHours * 60) + " minutes");
        return totalTimeHours * 60; // Convert hours to minutes
    }

    private double calculateTotalDistance() {
        double distance = 0.0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            distance += nodes.get(i).toCoordinates().distanceTo(nodes.get(i + 1).toCoordinates());
        }
        return distance;
    }

    public List<Map<String, Object>> getRouteSegments() {
        List<Map<String, Object>> segments = new ArrayList<>();
        List<Node> nodes = getNodes();
        for (int i = 0; i < nodes.size() - 1; i++) {
            Node start = nodes.get(i);
            Node end = nodes.get(i + 1);
            Way way = graph.getWay(start, end);
            double distance = start.toCoordinates().distanceTo(end.toCoordinates());
            int speedLimitMph = way.getSpeedLimitMph();
            
            Map<String, Object> segment = new HashMap<>();
            segment.put("startLat", start.lat());
            segment.put("startLon", start.lon());
            segment.put("endLat", end.lat());
            segment.put("endLon", end.lon());
            segment.put("distance", distance);
            segment.put("speedLimit", speedLimitMph);
            
            segments.add(segment);
        }
        return segments;
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