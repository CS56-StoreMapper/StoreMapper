// package com.example;

// import org.junit.jupiter.api.Test;
// import static org.junit.jupiter.api.Assertions.*;
// import com.example.model.Coordinates;
// import com.example.model.Node;
// import com.example.model.Route;
// import java.util.List;

// public class MITFastPathsTest extends BaseMapTest {

//     @Test
//     public void test00_fast() {
//         // Should take the a longer, but faster path: New House, Kresge, North Maseeh, Lobby 7, Building 35, 009 OH
//         Coordinates start = new Coordinates(42.355, -71.1009); // New House
//         Coordinates end = new Coordinates(42.3612, -71.092); // 34-501
//         List<Coordinates> expectedPath = List.of(
//             new Coordinates(42.355, -71.1009),
//             new Coordinates(42.3575, -71.0927),
//             new Coordinates(42.3582, -71.0931),
//             new Coordinates(42.3592, -71.0932),
//             new Coordinates(42.3601, -71.0952),
//             new Coordinates(42.3612, -71.092)
//         );
//         Route actualRoute = mapService.calculateFastRoute(start, end);
//         assertPathEquals(expectedPath, actualRoute.getNodes());
//     }

//     @Test
//     public void test01_fast() {
//         // Should take path Building 26, 009 OH, Building 35, Lobby 7
//         // Tests that the 'maxspeed_mph' is used instead of highway type speed limit
//         // also tests that in the presence of a repeated way, the highest speed limit is preferred
//         Coordinates start = new Coordinates(42.36, -71.0907); // near Lobby 26
//         Coordinates end = new Coordinates(42.3592, -71.0932); // Near Lobby 7
//         List<Coordinates> expectedPath = List.of(
//             new Coordinates(42.36, -71.0907),
//             new Coordinates(42.3612, -71.092),
//             new Coordinates(42.3601, -71.0952),
//             new Coordinates(42.3592, -71.0932)
//         );
//         Route actualRoute = mapService.calculateFastRoute(start, end);
//         assertPathEquals(expectedPath, actualRoute.getNodes());
//     }

//     @Test
//     public void test02_fast() {
//         // Should take path Kresge, North Maseeh, South Maseeh, New House
//         // Tests that one-ways are only allowed to go in certain direction
//         Coordinates start = new Coordinates(42.3576, -71.0952); // Kresge
//         Coordinates end = new Coordinates(42.355, -71.1009); // New House
//         List<Coordinates> expectedPath = List.of(
//             new Coordinates(42.3575, -71.0952),
//             new Coordinates(42.3582, -71.0931),
//             new Coordinates(42.3575, -71.0927),
//             new Coordinates(42.355, -71.1009)
//         );
//         Route actualRoute = mapService.calculateFastRoute(start, end);
//         assertPathEquals(expectedPath, actualRoute.getNodes());
//     }

//     @Test
//     public void test03_fast() {
//         // Should take path Kresge, North Maseeh, Lobby 7, Building 35, 009 Oh
//         // Tests for non-exact locations
//         // Tests that nodes that aren't in any way are not used
//         Coordinates start = new Coordinates(42.3576, -71.0951); // close to Kresge
//         Coordinates end = new Coordinates(42.3609, -71.0911); // is near an invalid node: Unreachable Node
//         List<Coordinates> expectedPath = List.of(
//             new Coordinates(42.3575, -71.0952),
//             new Coordinates(42.3582, -71.0931),
//             new Coordinates(42.3592, -71.0932),
//             new Coordinates(42.3601, -71.0952),
//             new Coordinates(42.3612, -71.092)
//         );
//         Route actualRoute = mapService.calculateFastRoute(start, end);
//         assertPathEquals(expectedPath, actualRoute.getNodes());
//     }

//     private void assertPathEquals(List<Coordinates> expected, List<Node> actual) {
//         assertEquals(expected.size(), actual.size(), "Path lengths differ");
//         for (int i = 0; i < expected.size(); i++) {
//             assertTrue(coordinatesClose(expected.get(i), actual.get(i).toCoordinates()),
//                        "Paths differ at position " + i);
//         }
//     }

//     private boolean coordinatesClose(Coordinates c1, Coordinates c2) {
//         return Math.abs(c1.getLatitude() - c2.getLatitude()) <= 1e-9 &&
//                Math.abs(c1.getLongitude() - c2.getLongitude()) <= 1e-9;
//     }
// }