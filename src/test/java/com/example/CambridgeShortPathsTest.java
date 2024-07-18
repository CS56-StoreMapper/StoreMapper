package com.example;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.example.model.Coordinates;

@Disabled("Cambridge tests are slow, enable for full test suite")
public class CambridgeShortPathsTest extends BaseMapTest {
    

    @Override
    protected String getDatasetName() {
        return "cambridge";
    }

    @Override
    protected Coordinates getTestPoint() {
        return new Coordinates(42.358333, -71.063333); // Example coordinates for Cambridge area
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

    @Test
    public void test03_short() {
        testRoute("03_short");
    }

    @Test
    public void test04_short() {
        testRoute("04_short");
    }

    @Test
    public void test05_short() {
        testRoute("05_short");
    }
}