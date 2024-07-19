package com.example.model;

import java.util.*;
import com.example.util.RoadUtil;
import java.util.logging.Logger;

public class FastestPathFinder {
    private static final Logger logger = Logger.getLogger(FastestPathFinder.class.getName());
    private final Graph graph;

    public FastestPathFinder(Graph graph) {
        this.graph = graph;
    }

    public List<Node> findFastestPath(Node start, Node end) {
        Map<Node, Double> times = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(times::get));

        graph.getNodes().forEach(node -> times.put(node, Double.MAX_VALUE));
        times.put(start, 0.0);
        queue.offer(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.equals(end)) {
                return reconstructPath(previousNodes, end);
            }

            for (Node neighbor : graph.getNeighbors(current)) {
                double newTime = times.get(current) + calculateTime(current, neighbor);

                if (newTime < times.get(neighbor)) {
                    queue.remove(neighbor);
                    times.put(neighbor, newTime);
                    previousNodes.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        return null;
    }

    private double calculateTime(Node start, Node end) {
        Way way = graph.getWay(start, end);
        if (way == null) {
            logger.warning("No way found between nodes " + start.id() + " and " + end.id());
            return Double.POSITIVE_INFINITY;
        }
        double distance = start.toCoordinates().distanceTo(end.toCoordinates());
        int speedLimitMph = estimateSpeedLimit(way);
        return distance / (speedLimitMph * 0.44704); // Convert mph to m/s
    }

    private int estimateSpeedLimit(Way way) {
        Map<String, String> tags = way.getTags();
        logger.info("Estimating speed limit for way " + way.id() + " with tags: " + tags);

        if (tags.containsKey("maxspeed_mph")) {
            Object maxSpeed = tags.get("maxspeed_mph");
            try {
                if (maxSpeed instanceof Integer) {
                    return (Integer) maxSpeed;
                } else if (maxSpeed instanceof String) {
                    return Integer.parseInt((String) maxSpeed);
                }
            } catch (NumberFormatException e) {
                logger.warning("Invalid maxspeed_mph value for way " + way.id() + ": " + maxSpeed);
            }
        }

        // Fall back to highway type
        String highwayType = tags.get("highway");
        return RoadUtil.getDefaultSpeedLimit(highwayType);
    }

    private List<Node> reconstructPath(Map<Node, Node> previousNodes, Node end) {
        List<Node> path = new ArrayList<>();
        for (Node node = end; node != null; node = previousNodes.get(node)) {
            path.add(0, node);
        }
        return path;
    }
}
