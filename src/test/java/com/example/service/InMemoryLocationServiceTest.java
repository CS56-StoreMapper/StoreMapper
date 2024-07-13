package com.example.service;

import com.example.model.Coordinates;
import com.example.model.Store;
import com.example.model.Location;

import com.example.service.InMemoryLocationService;

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
    public void testAddAndGetStore() {
        Store store = new Store(1L, "Test Store", new Coordinates(40.7128, -74.0060));
        service.addLocation(store);

        Location retrieved = service.getLocationById(1L);
        assertEquals(store, retrieved);
    }

    @Test
    public void testGetAllLocations() {
        Store store1 = new Store(1L, "Store 1", new Coordinates(40.7128, -74.0060));
        Store store2 = new Store(2L, "Store 2", new Coordinates(34.0522, -118.2437));

        service.addLocation(store1);
        service.addLocation(store2);

        System.out.println(store1);
        System.out.println(store2);

        List<Location> allLocations = service.getAllLocations();
        assertEquals(2, allLocations.size());
        assertTrue(allLocations.contains(store1));
        assertTrue(allLocations.contains(store2));
    }

    @Test
    public void testWithGeneratedLocations() {
        InMemoryLocationService serviceWithLocations = new InMemoryLocationService(50);
        assertEquals(50, serviceWithLocations.getAllLocations().size());
    }
}