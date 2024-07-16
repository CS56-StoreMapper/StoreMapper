package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.example.model.Coordinates;
import com.example.model.Node;
import com.example.model.Route;
import java.util.List;

public class MITShortPathsTest extends BaseMapTest {

    @Test
    public void test00_short() {
        // Should take the most direct path: New House, Kresge, North Maseeh, Lobby 7, Building 26, 009 OH
        Coordinates start = new Coordinates(42.355, -71.1009); // New House
        Coordinates end = new Coordinates(42.3612, -71.092); // 34-501
        List<Coordinates> expectedPath = List.of(
            new Coordinates(42.355, -71.1009),
            new Coordinates(42.3575, -71.0952),
            new Coordinates(42.3582, -71.0931),
            new Coordinates(42.3592, -71.0932),
            new Coordinates(42.36, -71.0907),
            new Coordinates(42.3612, -71.092)
        );
        Route actualRoute = mapService.calculateRoute(start, end);
        System.out.println("Actual route: " + actualRoute.getNodes());
        assertPathEquals(expectedPath, actualRoute.getNodes());
    }

    @Test
    public void test01_short() {
        // Should take path Building 35, Lobby 7, North Maseeh, South Maseeh
        Coordinates start = new Coordinates(42.3603, -71.095); // near Building 35
        Coordinates end = new Coordinates(42.3573, -71.0928); // Near South Maseeh
        List<Coordinates> expectedPath = List.of(
            new Coordinates(42.3601, -71.0952),
            new Coordinates(42.3592, -71.0932),
            new Coordinates(42.3582, -71.0931),
            new Coordinates(42.3575, -71.0927)
        );
        Route actualRoute = mapService.calculateRoute(start, end);
        assertPathEquals(expectedPath, actualRoute.getNodes());
    }

    // Add more tests following the same pattern...

    private void assertPathEquals(List<Coordinates> expected, List<Node> actual) {
        assertEquals(expected.size(), actual.size(), "Path lengths differ");
        for (int i = 0; i < expected.size(); i++) {
            assertTrue(coordinatesClose(expected.get(i), actual.get(i).toCoordinates()),
                       "Paths differ at position " + i);
        }
    }

    private boolean coordinatesClose(Coordinates c1, Coordinates c2) {
        return Math.abs(c1.getLatitude() - c2.getLatitude()) <= 1e-9 &&
               Math.abs(c1.getLongitude() - c2.getLongitude()) <= 1e-9;
    }
}