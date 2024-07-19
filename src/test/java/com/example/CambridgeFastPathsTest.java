package com.example;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.example.model.Coordinates;

@Disabled("Cambridge tests are slow, enable for full test suite")
public class CambridgeFastPathsTest extends BaseMapTest {
    
    @Override
    protected String getDatasetName() {
        return "cambridge";
    }

    @Override
    protected Coordinates getTestPoint() {
        return new Coordinates(42.358333, -71.063333); // Example coordinates for Cambridge area
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

    @Test
    public void test03_fast() {
        testRoute("03_fast", RouteType.FASTEST);
    }

    @Test
    public void test04_fast() {
        testRoute("04_fast", RouteType.FASTEST);
    }

    @Test
    public void test05_fast() {
        testRoute("05_fast", RouteType.FASTEST);
    }
}
