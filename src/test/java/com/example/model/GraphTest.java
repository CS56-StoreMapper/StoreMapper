package com.example.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph();
    }

    @Nested
    class NodeOperations {
        @Test
        void testAddNode() {
            Node node = new Node(1, 0.0, 0.0);
            graph.addNode(node);
            assertEquals(1, graph.getNodeCount());
            assertEquals(node, graph.getNode(1));
        }

        @Test
        void testAddDuplicateNode() {
            Node node = new Node(1, 0.0, 0.0);
            graph.addNode(node);
            graph.addNode(node);
            assertEquals(1, graph.getNodeCount());
        }

        @Test
        void testGetNodes() {
            Node node1 = new Node(1, 0.0, 0.0);
            Node node2 = new Node(2, 1.0, 1.0);
            graph.addNode(node1);
            graph.addNode(node2);
            List<Node> nodes = graph.getNodes();
            assertEquals(2, nodes.size());
            assertTrue(nodes.contains(node1));
            assertTrue(nodes.contains(node2));
        }
    }

    @Nested
    class WayOperations {
        @Test
        void testAddWay() {
            Node start = new Node(1, 0.0, 0.0);
            Node end = new Node(2, 1.0, 1.0);
            Way way = new Way(start, end, Map.of("highway", "residential"));
            graph.addWay(way);
            assertEquals(2, graph.getNodeCount());
            assertEquals(1, graph.getWayCount());
        }

        @Test
        void testAddBidirectionalWay() {
            Node start = new Node(1, 0.0, 0.0);
            Node end = new Node(2, 1.0, 1.0);
            Way way = new Way(start, end, Map.of("highway", "residential", "oneway", "no"));
            graph.addWay(way);
            assertEquals(2, graph.getNodeCount());
            assertEquals(1, graph.getWayCount());  // Should count as 1 way, not 2
        }
    }

    @Nested
    class PathfindingOperations {
        @Test
        void testFindShortestPath() {
            Node start = new Node(1, 0.0, 0.0);
            Node middle = new Node(2, 1.0, 1.0);
            Node end = new Node(3, 2.0, 2.0);
            graph.addWay(new Way(start, middle, Map.of()));
            graph.addWay(new Way(middle, end, Map.of()));

            List<Node> path = graph.findShortestPath(start, end);
            assertEquals(3, path.size());
            assertEquals(start, path.get(0));
            assertEquals(middle, path.get(1));
            assertEquals(end, path.get(2));
        }

        @Test
        void testFindShortestPathNoRoute() {
            Node start = new Node(1, 0.0, 0.0);
            Node end = new Node(2, 1.0, 1.0);
            graph.addNode(start);
            graph.addNode(end);

            List<Node> path = graph.findShortestPath(start, end);
            assertTrue(path.isEmpty());
        }
    }

    @Nested
    class NeighborOperations {
        @Test
        void testGetNeighbors() {
            Node center = new Node(1, 0.0, 0.0);
            Node north = new Node(2, 0.0, 1.0);
            Node east = new Node(3, 1.0, 0.0);
            graph.addWay(new Way(center, north, Map.of()));
            graph.addWay(new Way(center, east, Map.of()));

            Set<Node> neighbors = graph.getNeighbors(center);
            assertEquals(2, neighbors.size());
            assertTrue(neighbors.contains(north));
            assertTrue(neighbors.contains(east));
        }
    }
}