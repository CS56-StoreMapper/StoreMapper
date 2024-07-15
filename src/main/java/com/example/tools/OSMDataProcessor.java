package com.example.tools;

import com.example.util.OSMConverter;

public class OSMDataProcessor {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -cp target/storemapper-1.0-SNAPSHOT.jar com.example.OSMDataProcessor <filename>");
            return;
        }
        try {
            OSMConverter.convertOSMToSerializedFormat(args[0]);
            System.out.println("Conversion completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}