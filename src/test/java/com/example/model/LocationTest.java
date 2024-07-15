package com.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LocationTest {

    @Test
    public void testStoreToString() {
        Store store = new Store(1, "Test Store", new Coordinates(40.7128, -74.0060));
        String expected = "Store{Location{id=1, name='Test Store', coordinates=Coordinates{latitude=40.712800, longitude=-74.006000}, type=STORE, osmId=N/A}}";
        assertEquals(expected, store.toString());
    }

    @Test
    public void testRestaurantToString() {
        Restaurant restaurant = new Restaurant(2, "Test Restaurant", new Coordinates(34.0522, -118.2437));
        String expected = "Restaurant{Location{id=2, name='Test Restaurant', coordinates=Coordinates{latitude=34.052200, longitude=-118.243700}, type=RESTAURANT, osmId=N/A}}";
        assertEquals(expected, restaurant.toString());
    }
}