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
            Graph graph = new Graph();
            Node start = new Node(1, 0.0, 0.0);
            Node end = new Node(2, 1.0, 1.0);
            Map<String, String> tags = new HashMap<>();
            tags.put("highway", "residential");
            Way way = new Way(start, end, Map.of("tags", tags, "nodes", Arrays.asList(1L, 2L)));
            
            graph.addNode(start);
            graph.addNode(end);
            graph.addWay(way);

            assertEquals(2, graph.getNodeCount());
            assertEquals(1, graph.getWayCount());
        }

        @Test
        void testAddBidirectionalWay() {
            Graph graph = new Graph();
            Node start = new Node(1, 0.0, 0.0);
            Node end = new Node(2, 1.0, 1.0);
            Map<String, String> tags = new HashMap<>();
            tags.put("highway", "residential");
            Way way = new Way(start, end, Map.of("tags", tags, "nodes", Arrays.asList(1L, 2L)));
            
            graph.addNode(start);
            graph.addNode(end);
            graph.addWay(way);

            assertEquals(2, graph.getNodeCount());
            assertEquals(1, graph.getWayCount());
            assertTrue(graph.getNeighbors(start).contains(end));
            assertTrue(graph.getNeighbors(end).contains(start));
        }
    }

    @Nested
    class NeighborOperations {
        @Test
        void testGetNeighbors() {
            Graph graph = new Graph();
            Node center = new Node(1, 0.0, 0.0);
            Node north = new Node(2, 0.0, 1.0);
            Node east = new Node(3, 1.0, 0.0);
            
            Map<String, String> tags = new HashMap<>();
            tags.put("highway", "residential");
            
            graph.addNode(center);
            graph.addNode(north);
            graph.addNode(east);
            graph.addWay(new Way(center, north, Map.of("tags", tags, "nodes", Arrays.asList(1L, 2L))));
            graph.addWay(new Way(center, east, Map.of("tags", tags, "nodes", Arrays.asList(1L, 3L))));

            Set<Node> neighbors = graph.getNeighbors(center);
            assertEquals(2, neighbors.size());
            assertTrue(neighbors.contains(north));
            assertTrue(neighbors.contains(east));
        }
    }
    @Nested
    class PathfindingOperations {
        @Test
        void testFindShortestPath() {
            Graph graph = new Graph();
            Node start = new Node(1, 0.0, 0.0);
            Node middle = new Node(2, 1.0, 1.0);
            Node end = new Node(3, 2.0, 2.0);
            
            Map<String, String> tags = new HashMap<>();
            tags.put("highway", "residential");
            
            graph.addNode(start);
            graph.addNode(middle);
            graph.addNode(end);
            graph.addWay(new Way(start, middle, Map.of("tags", tags, "nodes", Arrays.asList(1L, 2L))));
            graph.addWay(new Way(middle, end, Map.of("tags", tags, "nodes", Arrays.asList(2L, 3L))));

            List<Node> path = graph.findShortestPath(start, end);
            assertNotNull(path);
            assertEquals(3, path.size());
            assertEquals(start, path.get(0));
            assertEquals(middle, path.get(1));
            assertEquals(end, path.get(2));
        }

        @Test
        void testFindShortestPathNoRoute() {
            Graph graph = new Graph();
            Node start = new Node(1, 0.0, 0.0);
            Node end = new Node(2, 1.0, 1.0);
            
            graph.addNode(start);
            graph.addNode(end);

            List<Node> path = graph.findShortestPath(start, end);
            assertNull(path);
        }
    }
}