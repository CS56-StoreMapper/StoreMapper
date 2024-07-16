package com.example;

import org.junit.jupiter.api.Test;
import com.example.model.Coordinates;

public class MidwestShortPathsTest extends BaseMapTest {
    @Override
    protected String getDatasetName() {
        return "midwest";
    }

    @Override
    protected Coordinates getTestPoint() {
        return new Coordinates(41.5, -89.5); // Example coordinates for Midwest area
    }

    @Test
    public void test00_short() {
        testRoute("00_short");
    }

    @Test
    public void test01_short() {
        testRoute("01_short");
    }
    
    @Test
    public void test02_short() {
        testRoute("02_short");
    }
}