package com.example.tools;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class SerializedFileReader {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SerializedFileReader <filename>");
            System.exit(1);
        }

        String filename = args[0];
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            int objectCount = 0;
            while (true) {
                try {
                    Object obj = ois.readObject();
                    objectCount++;
                    System.out.println("Object " + objectCount + ":");
                    System.out.println("  Class: " + obj.getClass().getName());
                    System.out.println("  Content: " + obj);
                    System.out.println();
                } catch (EOFException e) {
                    break;
                }
            }
            System.out.println("Total objects read: " + objectCount);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}