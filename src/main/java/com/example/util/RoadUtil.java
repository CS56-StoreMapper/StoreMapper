package com.example.util;

import java.util.Map;
import static java.util.Map.entry;

public final class RoadUtil {
    public static final Map<String, Integer> DEFAULT_SPEED_LIMIT_MPH = Map.ofEntries(
        entry("motorway", 60),
        entry("trunk", 45),
        entry("primary", 35),
        entry("secondary", 30),
        entry("residential", 25),
        entry("tertiary", 25),
        entry("unclassified", 25),
        entry("living_street", 10),
        entry("motorway_link", 30),
        entry("trunk_link", 30),
        entry("primary_link", 30),
        entry("secondary_link", 30),
        entry("tertiary_link", 25),
        entry("default", 25)
    );

    private RoadUtil() {} // Prevents instantiation

    public static int getDefaultSpeedLimit(String highwayType) {
        if (highwayType == null) {
            return DEFAULT_SPEED_LIMIT_MPH.get("default");
        }
        return DEFAULT_SPEED_LIMIT_MPH.getOrDefault(highwayType, 25); // Default to 25 mph
    }
}