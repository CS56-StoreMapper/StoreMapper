package com.example.util;

public class MemoryUtil {
    public static String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return String.format("Used: %d MB, Free: %d MB, Total: %d MB", 
            usedMemory / (1024 * 1024), 
            freeMemory / (1024 * 1024), 
            totalMemory / (1024 * 1024));
    }
}
