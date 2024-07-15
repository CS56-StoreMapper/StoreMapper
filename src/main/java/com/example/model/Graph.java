package com.example.model;

import java.util.*;
import java.util.stream.Collectors;

public class Graph {
    private final Map<Long, Node> nodes;
    private final Map<Long, Set<Way>> adjacencyList;

    public Graph() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public void addNode(Node node) {
        nodes.putIfAbsent(node.id(), node);
        adjacencyList.putIfAbsent(node.id(), new HashSet<>());
    }

    public void addWay(Way way) {
        addNode(way.startNode());
        addNode(way.endNode());
        adjacencyList.get(way.startNode().id()).add(way);
        if (!way.isOneWay()) {
            Way reverseWay = new Way(way.endNode(), way.startNode(), way.data());
            adjacencyList.get(way.endNode().id()).add(reverseWay);
        }
    }

    public ArrayList<Node> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public Node getNode(long id) {
        return nodes.get(id);
    }

    public Set<Node> getNeighbors(Node node) {
        return adjacencyList.get(node.id()).stream()
                .map(Way::endNode)
                .collect(Collectors.toSet());
    }

    public List<Node> findShortestPath(Node start, Node end) {
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(
                Comparator.comparingDouble(distances::get));

        distances.put(start, 0.0);
        queue.offer(start);

        while (!queue.isEmpty()) {
            Node current = queue.poll();

            if (current.equals(end)) {
                return reconstructPath(previousNodes, end);
            }

            for (Way way : adjacencyList.get(current.id())) {
                Node neighbor = way.endNode();
                double newDistance = distances.get(current) + calculateDistance(current, neighbor);

                if (newDistance < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    distances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, current);
                    queue.remove(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        return Collections.emptyList(); // No path found
    }

    private List<Node> reconstructPath(Map<Node, Node> previousNodes, Node end) {
        List<Node> path = new ArrayList<>();
        Node current = end;
        while (current != null) {
            path.add(current);
            current = previousNodes.get(current);
        }
        Collections.reverse(path);
        return path;
    }

    private double calculateDistance(Node node1, Node node2) {
        return node1.toCoordinates().distanceTo(node2.toCoordinates());
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public int getWayCount() {
        // Count only the original ways, not the reverse ways
        return (int) adjacencyList.values().stream()
                .flatMap(Set::stream)
                .filter(way -> way.isOneWay() || way.startNode().id() < way.endNode().id())
                .count();
    }
}