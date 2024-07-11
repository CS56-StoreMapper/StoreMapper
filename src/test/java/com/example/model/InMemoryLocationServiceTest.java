package com.example.service;

import com.example.model.Coordinates;
import com.example.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryLocationServiceTest {

    private InMemoryLocationService service;

    @BeforeEach
    public void setUp() {
        service = new InMemoryLocationService();
    }

    @Test
    public void testAddAndGetLocation() {
        Location location = new Location(1L, "Test Location", new Coordinates(40.7128, -74.0060));
        service.addLocation(location);

        Location retrieved = service.getLocationById(1L);
        assertEquals(location, retrieved);
    }

    @Test
    public void testGetAllLocations() {
        Location location1 = new Location(1L, "Location 1", new Coordinates(40.7128, -74.0060));
        Location location2 = new Location(2L, "Location 2", new Coordinates(34.0522, -118.2437));

        service.addLocation(location1);
        service.addLocation(location2);

        System.out.println(location1);
        System.out.println(location2);

        List<Location> allLocations = service.getAllLocations();
        assertEquals(2, allLocations.size());
        assertTrue(allLocations.contains(location1));
        assertTrue(allLocations.contains(location2));
    }
}