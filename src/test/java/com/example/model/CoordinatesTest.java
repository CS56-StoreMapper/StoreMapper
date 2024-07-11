package com.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CoordinatesTest {

    @Test
    public void testCoordinatesCreation() {
        Coordinates coords = new Coordinates(40.7128, -74.0060);

        assertEquals(40.7128, coords.getLatitude(), 0.0001);
        assertEquals(-74.0060, coords.getLongitude(), 0.0001);
    }

    @Test
    public void testCoordinatesToString() {
        Coordinates coords = new Coordinates(40.7128, -74.0060);

        String expected = "Coordinates{latitude=40.712800, longitude=-74.006000}";
        assertEquals(expected, coords.toString());
    }
}