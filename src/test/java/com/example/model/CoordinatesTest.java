package com.example.model;

import org.junit.jupiter.api.Test;
import com.example.model.Coordinates;

import com.example.util.DistanceUtil;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class CoordinatesTest {
    private final Coordinates LOS_ANGELES = new Coordinates(34.0549, -118.2426);
    private final Coordinates SAN_FRANCISCO = new Coordinates(37.7749, -122.4194);

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

    @Test
    void testDistance() {
        double distanceLA_SF_km = LOS_ANGELES.distanceTo(SAN_FRANCISCO);
        double distanceLA_SF_miles = DistanceUtil.kmToMiles(distanceLA_SF_km);
        System.out.println("Distance from Los Angeles to San Francisco: " + distanceLA_SF_km + " km");
        System.out.println("Distance from Los Angeles to San Francisco: " + distanceLA_SF_miles + " miles");
        assertThat("Distance from Los Angeles to San Francisco should be approximately 559 km", distanceLA_SF_km, closeTo(559.0, 1));
        assertThat("Distance from Los Angeles to San Francisco should be approximately 347 miles", distanceLA_SF_miles, closeTo(347.0, 1));
    }
}