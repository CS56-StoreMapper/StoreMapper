package com.example;

import org.junit.jupiter.api.Test;
import com.example.model.Coordinates;

public class MidwestFastPathsTest extends BaseMapTest {
    @Override
    protected String getDatasetName() {
        return "midwest";
    }

    @Override
    protected Coordinates getTestPoint() {
        return new Coordinates(41.5, -89.5); // Example coordinates for Midwest area
    }

    @Test
    public void test00_fast() {
        testRoute("00_fast", RouteType.FASTEST);
    }

    @Test
    public void test01_fast() {
        testRoute("01_fast", RouteType.FASTEST);
    }
    
    @Test
    public void test02_fast() {
        testRoute("02_fast", RouteType.FASTEST);
    }
}