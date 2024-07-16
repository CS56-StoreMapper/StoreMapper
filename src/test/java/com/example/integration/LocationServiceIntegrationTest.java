package com.example.integration;

import com.example.model.*;
import com.example.service.InMemoryLocationService;
import com.example.util.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LocationServiceIntegrationTest {

    private InMemoryLocationService locationService;
    private Graph testGraph;

    @BeforeEach
    void setUp() {
        int nodeCount = 40; // Same as in LocationServlet
        testGraph = TestDataGenerator.generateTestGraph(nodeCount);
        locationService = new InMemoryLocationService(testGraph);
    }

    @Test
    void testInitialization() {
        List<Location> allLocations = locationService.getAllLocations();
        assertFalse(allLocations.isEmpty(), "Location list should not be empty");
        assertEquals(40, allLocations.size(), "Should have initialized 40 locations");
    }

    @Test
    void testGetLocationById() {
        Location location = locationService.getLocationById(1L);
        assertNotNull(location, "Location with ID 1 should exist");
    }

    @Test
    void testFindNearestLocation() {
        Coordinates testCoords = new Coordinates(0.0, 0.0); // Adjust based on your test data
        Optional<Location> nearest = locationService.findNearestLocation(testCoords, loc -> true);
        assertTrue(nearest.isPresent(), "Should find a nearest location");
    }

    @Test
    void testGraphConnectivity() {
        List<Node> nodes = testGraph.getNodes();
        assertFalse(nodes.isEmpty(), "Graph should have nodes");
        
        System.out.println("Total nodes in graph: " + nodes.size());
        System.out.println("Total ways in graph: " + testGraph.getWayCount());
        
        // Test that each node has at least one neighbor
        for (Node node : nodes) {
            Set<Node> neighbors = testGraph.getNeighbors(node);
            System.out.println("Node " + node.id() + " has " + neighbors.size() + " neighbors");
            if (neighbors.isEmpty()) {
                System.out.println("Node with no neighbors: " + node);
                testGraph.printNodeAdjacencyList(node.id());
            }
            assertFalse(neighbors.isEmpty(), "Each node should have at least one neighbor. Node: " + node);
        }
        
        testGraph.printGraphStructure();
    }

    @Test
    void testLocationToGraphMapping() {
        List<Location> locations = locationService.getAllLocations();
        for (Location location : locations) {
            Node correspondingNode = testGraph.getNode(location.getId());
            assertNotNull(correspondingNode, "Each location should have a corresponding node in the graph");
            assertEquals(location.getCoordinates().getLatitude(), correspondingNode.lat(), 0.0001);
            assertEquals(location.getCoordinates().getLongitude(), correspondingNode.lon(), 0.0001);
        }
    }

    // Add more tests as needed...
}