package com.example.model;

import java.util.*;

public class DijkstraPathFinder {
    private final Graph graph;

    public DijkstraPathFinder(Graph graph) {
        this.graph = graph;
    }

    public List<Node> findShortestPath(Node start, Node end) {
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        graph.getNodes().forEach(node -> distances.put(node, Double.MAX_VALUE));
        distances.put(start, 0.0);
        queue.offer(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.equals(end)) {
                return reconstructPath(previousNodes, end);
            }

            for (Node neighbor : graph.getNeighbors(current)) {
                double newDist = distances.get(current) + calculateDistance(current, neighbor);

                if (newDist < distances.get(neighbor)) {
                    queue.remove(neighbor);
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }

        return null;
    }

    private List<Node> reconstructPath(Map<Node, Node> previousNodes, Node end) {
        List<Node> path = new ArrayList<>();
        for (Node node = end; node != null; node = previousNodes.get(node)) {
            path.add(0, node);
        }
        return path;
    }

    private double calculateDistance(Node node1, Node node2) {
        return node1.toCoordinates().distanceTo(node2.toCoordinates());
    }
}