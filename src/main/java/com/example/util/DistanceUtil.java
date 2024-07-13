package com.example.util;

public class DistanceUtil {
    private static final double MILES_TO_KM = 1.60934;
    private static final double KM_TO_MILES = 1 / MILES_TO_KM;

    public static double milesToKm(double miles) {
        return miles * MILES_TO_KM;
    }

    public static double kmToMiles(double km) {
        return km * KM_TO_MILES;
    }
}
