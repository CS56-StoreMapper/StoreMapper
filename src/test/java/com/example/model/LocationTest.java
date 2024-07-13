package com.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LocationTest {

    @Test
    public void testStoreCreation() {
        Coordinates coords = new Coordinates(40.7128, -74.0060);
        Store store = new Store(1L, "Test Store", coords);

        assertEquals(1L, store.getId());
        assertEquals("Test Store", store.getName());
        assertEquals(coords, store.getCoordinates());
    }

    @Test
    public void testStoreToString() {
        Coordinates coords = new Coordinates(40.7128, -74.0060);
        Store store = new Store(1L, "Test Store", coords);

        String expected = "Location{id=1, name='Test Store', coordinates=" + coords.toString() + "}";
        assertEquals(expected, store.toString());
    }
}