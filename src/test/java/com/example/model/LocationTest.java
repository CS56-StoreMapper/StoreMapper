package com.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LocationTest {

    @Test
    public void testLocationCreation() {
        Coordinates coords = new Coordinates(40.7128, -74.0060);
        Location location = new Location(1L, "Test Location", coords);

        assertEquals(1L, location.getId());
        assertEquals("Test Location", location.getName());
        assertEquals(coords, location.getCoordinates());
    }

    @Test
    public void testLocationToString() {
        Coordinates coords = new Coordinates(40.7128, -74.0060);
        Location location = new Location(1L, "Test Location", coords);

        String expected = "Location{id=1, name='Test Location', coordinates=" + coords.toString() + "}";
        assertEquals(expected, location.toString());
    }
}