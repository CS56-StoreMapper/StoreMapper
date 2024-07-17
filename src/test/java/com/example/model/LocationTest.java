package com.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class LocationTest {

    @Test
    public void testStoreToString() {
        Node node = new Node(1, 40.7128, -74.0060, Map.of("name", "Test Store"));
        Store store = new Store(1, 40.7128, -74.0060, node);
        String expected = "Store{Location{id=1, name='Test Store', coordinates=Coordinates{latitude=40.712800, longitude=-74.006000}, type=STORE, osmId=1}}";
        assertEquals(expected, store.toString());
    }

    @Test
    public void testRestaurantToString() {
        Node node = new Node(2, 34.0522, -118.2437, Map.of("name", "Test Restaurant"));
        Restaurant restaurant = new Restaurant(2, 34.0522, -118.2437, node);
        String expected = "Restaurant{Location{id=2, name='Test Restaurant', coordinates=Coordinates{latitude=34.052200, longitude=-118.243700}, type=RESTAURANT, osmId=2}}";
        assertEquals(expected, restaurant.toString());
    }

    @Test
    public void testStoreGetName() {
        Node node = new Node(1, 40.7128, -74.0060, Map.of("name", "Test Store"));
        Store store = new Store(1, 40.7128, -74.0060, node);
        assertEquals("Test Store", store.getName());
    }

    @Test
    public void testRestaurantGetName() {
        Node node = new Node(2, 34.0522, -118.2437, Map.of("name", "Test Restaurant"));
        Restaurant restaurant = new Restaurant(2, 34.0522, -118.2437, node);
        assertEquals("Test Restaurant", restaurant.getName());
    }

    @Test
    public void testStoreGetCoordinates() {
        Node node = new Node(1, 40.7128, -74.0060, Map.of("name", "Test Store"));
        Store store = new Store(1, 40.7128, -74.0060, node);
        Coordinates expected = new Coordinates(40.7128, -74.0060);
        assertEquals(expected, store.getCoordinates());
    }

    @Test
    public void testRestaurantGetCoordinates() {
        Node node = new Node(2, 34.0522, -118.2437, Map.of("name", "Test Restaurant"));
        Restaurant restaurant = new Restaurant(2, 34.0522, -118.2437, node);
        Coordinates expected = new Coordinates(34.0522, -118.2437);
        assertEquals(expected, restaurant.getCoordinates());
    }

    @Test
    public void testStoreGetType() {
        Node node = new Node(1, 40.7128, -74.0060, Map.of("name", "Test Store"));
        Store store = new Store(1, 40.7128, -74.0060, node);
        assertEquals(LocationType.STORE, store.getType());
    }

    @Test
    public void testRestaurantGetType() {
        Node node = new Node(2, 34.0522, -118.2437, Map.of("name", "Test Restaurant"));
        Restaurant restaurant = new Restaurant(2, 34.0522, -118.2437, node);
        assertEquals(LocationType.RESTAURANT, restaurant.getType());
    }
}