package com.example.model;

import java.util.*;
import java.util.stream.Collectors;



public class Graph {
    private final Map<Long, Node> nodes;
    private final Map<Long, Set<Way>> adjacencyList;

    private class NodeWrapper {
        Node node;
        double priority;
    
        NodeWrapper(Node node, double priority) {
            this.node = node;
            this.priority = priority;
        }
    
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NodeWrapper that = (NodeWrapper) o;
            return Objects.equals(node, that.node);
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(node);
        }
    }

    public Graph() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    public Graph(List<Node> nodes, List<Way> ways) {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        for (Node node : nodes) {
            addNode(node);
        }
        updateWaysWithNodes(ways);
        for (Way way : ways) {
            addWay(way);
        }
    }

    private void updateWaysWithNodes(List<Way> ways) {
        Map<Long, Node> nodesMap = nodes.values().stream()
            .collect(Collectors.toMap(Node::id, n -> n));
        
        for (int i = 0; i < ways.size(); i++) {
            Way way = ways.get(i);
            Node startNode = nodesMap.get(way.startNode().id());
            Node endNode = nodesMap.get(way.endNode().id());
            if (startNode != null && endNode != null) {
                ways.set(i, way.withNodes(startNode, endNode));
            } else {
                throw new IllegalStateException("Missing nodes for way: " + way);
            }
        }
    }

    public void addNode(Node node) {
        nodes.putIfAbsent(node.id(), node);
        adjacencyList.putIfAbsent(node.id(), new HashSet<>());
    }

    public void addWay(Way way) {
        addNode(way.startNode());
        addNode(way.endNode());
        adjacencyList.get(way.startNode().id()).add(way);
        System.out.println("Added way: " + way);
        
        if (!way.isOneWay()) {
            Way reverseWay = new Way(way.endNode(), way.startNode(), way.data());
            adjacencyList.get(way.endNode().id()).add(reverseWay);
            System.out.println("Added reverse way: " + reverseWay);
        }
    }

    public List<Node> getNodes() {
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

    public Node findNearestNode(double lat, double lon) {
        return nodes.values().stream()
            .min(Comparator.comparingDouble(node -> 
                Math.pow(node.lat() - lat, 2) + Math.pow(node.lon() - lon, 2)))
            .orElseThrow(() -> new IllegalStateException("No nodes in the graph"));
    }

    public Node findNearestNode(Coordinates coordinates) {
        return findNearestNode(coordinates.getLatitude(), coordinates.getLongitude());
    }

    public List<Node> findShortestPath(Node start, Node end) {
        System.out.println("Finding shortest path from " + start + " to " + end);
        // Step 1: Initialize data structures
        Map<Node, Double> distances = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));
    
        // Step 2: Initialize distances to infinity for all nodes
        for (Node node : nodes.values()) {
            distances.put(node, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        queue.offer(start);
    
        // Step 3: Main Loop
        while (!queue.isEmpty()) {
            // Step 3.1: Get the node with the smallest distance
            Node current = queue.poll();
            System.out.println("Examining node: " + current);
    
            // Step 3.2: If the current node is the end node, return the path
            // if (current.equals(end) && distances.get(current) <= distances.get(end)) {
            //     return reconstructPath(previousNodes, end);
            // }
    
            // Step 3.3: Get the neighbors of the current node
            Set<Way> neighbors = adjacencyList.get(current.id());
            System.out.println("Neighbors: " + neighbors);
    
            // Step 3.4: For each neighbor, calculate the new distance and update if it's shorter
            for (Way way : neighbors) {
                if (way.getTags().getOrDefault("highway", "").equals("illegal path")) {
                    continue;  // Skip illegal paths
                }
                Node neighbor = way.endNode();
                double newDist = distances.get(current) + calculateDistance(current, neighbor);
    
                // Step 3.4.1: If the new distance is shorter, update the distance and previous node
                if (newDist < distances.get(neighbor)) {
                    queue.remove(neighbor);
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    queue.offer(neighbor);
                    System.out.println("Updated neighbor: " + neighbor + " with distance: " + newDist);
                }
            }
        }
         // Check if a path to the end node was found
        if (previousNodes.containsKey(end)) {
            List<Node> path = reconstructPath(previousNodes, end);
            printPath(path);
            return path;
        }
        
        // Step 4: If no path is found, return an empty list
        System.out.println("No path found");
        return Collections.emptyList();
    }

    private List<Node> reconstructPath(Map<Node, Node> previousNodes, Node end) {
        List<Node> path = new ArrayList<>();
        Node current = end;
        while (current != null) {
            path.add(0, current);
            current = previousNodes.get(current);
        }
        System.out.println("Path found: " + path);
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

    private void printPath(List<Node> path) {
        System.out.println("Path found:");
        for (int i = 0; i < path.size(); i++) {
            System.out.println(i + ": " + path.get(i));
        }
    }

    public void printGraphStructure() {
        System.out.println("Graph Structure:");
        for (Map.Entry<Long, Set<Way>> entry : adjacencyList.entrySet()) {
            Node node = nodes.get(entry.getKey());
            System.out.println("Node: " + node);
            System.out.println("  Outgoing ways:");
            for (Way way : entry.getValue()) {
                System.out.println("    " + way);
            }
            System.out.println("  Incoming ways:");
            for (Set<Way> ways : adjacencyList.values()) {
                for (Way way : ways) {
                    if (way.endNode().equals(node)) {
                        System.out.println("    " + way);
                    }
                }
            }
        }
    }
}