package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.model.Coordinates;
import com.example.model.Location;
import com.example.model.Store;
import com.example.model.Way;
import com.example.model.Graph;
import com.example.model.Route;
import com.example.model.Node;


class MapServiceTest {

    @Mock
    private LocationService locationService;

    private Graph graph;
    private MapService mapService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        graph = new Graph();
        // Add some nodes and ways to the graph for testing
        Node node1 = new Node(1, 34.0522, -118.2437);
        Node node2 = new Node(2, 34.0523, -118.2438);
        Node node3 = new Node(3, 34.0524, -118.2439);
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);

        Map<String, Object> wayData1 = new HashMap<>();
        wayData1.put("tags", Map.of("highway", "residential", "oneway", "no"));
        Map<String, Object> wayData2 = new HashMap<>();
        wayData2.put("tags", Map.of("highway", "residential", "oneway", "no"));
        
        graph.addWay(new Way(node1, node2, wayData1));
        graph.addWay(new Way(node2, node3, wayData2));
        
        mapService = new MapService(locationService, graph);
    }

    @Nested
    class FindNearestLocationTests {
        @Test
        void testFindNearestLocationNoLocations() {
            Coordinates point = new Coordinates(34.0522, -118.2437);
            when(locationService.getAllLocations()).thenReturn(List.of());
            
            Optional<Location> nearest = mapService.findNearestLocation(point);
            assertTrue(nearest.isEmpty(), "Expected empty Optional when no locations are available");
        }

        @Test
        void testFindNearestLocationEquidistant() {
            Coordinates point = new Coordinates(34.0522, -118.2437);
            Location loc1 = new Store(1, "Store 1", Coordinates.getDestinationPoint(point, 1, 0));
            Location loc2 = new Store(2, "Store 2", Coordinates.getDestinationPoint(point, 1, 180));
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));
    
            Optional<Location> nearest = mapService.findNearestLocation(point);
            System.out.println("Nearest location: " + nearest);
    
            assertTrue(nearest.isPresent(), "Expected a location to be returned");
            assertTrue(nearest.get().getId() == 1 || nearest.get().getId() == 2, "Expected either location 1 or 2 to be returned");
        }

        @Test
        void testFindNearestLocationOneIsNearest() {
            Coordinates point = new Coordinates(34.0522, -118.2437);
            Location loc1 = new Store(1, "Store 1", new Coordinates(34.0500, -118.2500));
            Location loc2 = new Store(2, "Store 2", new Coordinates(34.0600, -118.2600));
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));
    
            Optional<Location> nearest = mapService.findNearestLocation(point);
    
            assertTrue(nearest.isPresent(), "Expected a location to be returned");
            assertEquals(1, nearest.get().getId());
            assertEquals("Store 1", nearest.get().getName());
        }
    }

    @Nested
    class FindLocationsWithinRadiusTests {
        @Test
        void testOneIsWithin() {
            Coordinates center = new Coordinates(34.0522, -118.2437);
            double radiusKm = 10.0;
            
            Location loc1 = new Store(1, "Store 1", Coordinates.getDestinationPoint(center, radiusKm-1, 0));
            Location loc2 = new Store(2, "Store 2", Coordinates.getDestinationPoint(center, radiusKm+1, 90));
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));
        
            List<Location> locationsWithinRadius = mapService.findLocationsWithinRadius(center, radiusKm);
        
            System.out.println("Distance to loc1: " + center.distanceTo(loc1.getCoordinates()));
            System.out.println("Distance to loc2: " + center.distanceTo(loc2.getCoordinates()));
        
            assertEquals(1, locationsWithinRadius.size(), "Expected only one location within radius");
            assertEquals(1, locationsWithinRadius.get(0).getId(), "Expected the first location to be within radius");
        }

        @Test
        void testNoResults() {
            Coordinates center = new Coordinates(34.0522, -118.2437);
            double radiusKm = 1.0;
            
            Location loc1 = new Store(1, "Store 1", Coordinates.getDestinationPoint(center, radiusKm+0.1, 0));
            Location loc2 = new Store(2, "Store 2", Coordinates.getDestinationPoint(center, radiusKm+0.1, 90));
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));

            List<Location> locationsWithinRadius = mapService.findLocationsWithinRadius(center, radiusKm);

            assertTrue(locationsWithinRadius.isEmpty(), "Expected no locations within radius");
        }

        @Test
        void testBoundary() {
            Coordinates center = new Coordinates(34.0522, -118.2437);
            double radiusKm = 10.0;
            
            Location loc1 = new Store(1, "Store 1", Coordinates.getDestinationPoint(center, radiusKm-0.1, 0));
            Location loc2 = new Store(2, "Store 2", Coordinates.getDestinationPoint(center, radiusKm, 90));
            Location loc3 = new Store(3, "Store 3", Coordinates.getDestinationPoint(center, radiusKm+0.1, 180));
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2, loc3));

            List<Location> locationsWithinRadius = mapService.findLocationsWithinRadius(center, radiusKm);

            System.out.println("Distance to loc1: " + center.distanceTo(loc1.getCoordinates()));
            System.out.println("Distance to loc2: " + center.distanceTo(loc2.getCoordinates()));
            System.out.println("Distance to loc3: " + center.distanceTo(loc3.getCoordinates()));

            assertEquals(2, locationsWithinRadius.size(), "Expected two locations within or on the radius");
            assertTrue(locationsWithinRadius.stream().anyMatch(loc -> loc.getId() == 1), "Expected location 1 to be within radius");
            assertTrue(locationsWithinRadius.stream().anyMatch(loc -> loc.getId() == 2), "Expected location 2 to be on the radius boundary");
            assertFalse(locationsWithinRadius.stream().anyMatch(loc -> loc.getId() == 3), "Expected location 3 to be outside radius");
        }

        @Test
        void testZeroRadius() {
            Coordinates center = new Coordinates(34.0522, -118.2437);
            double radiusKm = 0.0;
            
            Location loc1 = new Store(1, "Store 1", center); // Exactly at the center
            Location loc2 = new Store(2, "Store 2", Coordinates.getDestinationPoint(center, 0.1, 0));
            
            when(locationService.getAllLocations()).thenReturn(Arrays.asList(loc1, loc2));

            List<Location> locationsWithinRadius = mapService.findLocationsWithinRadius(center, radiusKm);

            assertEquals(1, locationsWithinRadius.size(), "Expected only the location exactly at the center");
            assertEquals(1, locationsWithinRadius.get(0).getId(), "Expected only the location at the center to be returned");
        }
    }

    @Nested
    class CalculateRouteTests {
        @Test
        void testBasicRoute() {
            Location start = new Store(1, "Start", new Coordinates(34.0522, -118.2437));
            Location end = new Store(2, "End", new Coordinates(34.0524, -118.2439));
            
            Route route = mapService.calculateRoute(start, end);
            
            assertNotNull(route);
            assertEquals(3, route.getNodes().size());
            assertTrue(route.getTotalDistance() > 0);
        }

        @Test
        void testRouteWithCoordinates() {
            Coordinates start = new Coordinates(34.0522, -118.2437);
            Coordinates end = new Coordinates(34.0524, -118.2439);
            
            Route route = mapService.calculateRoute(start, end);
            
            assertNotNull(route);
            assertEquals(3, route.getNodes().size());
            assertTrue(route.getTotalDistance() > 0);
        }
    }

    @Test
    void testGetNodeCount() {
        assertEquals(3, mapService.getNodeCount());
    }

    @Test
    void testGetWayCount() {
        assertEquals(2, mapService.getWayCount(), "Expected 2 ways (counting bidirectional ways only once)");
    }

}