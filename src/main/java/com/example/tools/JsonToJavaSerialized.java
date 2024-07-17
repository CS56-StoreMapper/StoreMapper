package com.example.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;

public class JsonToJavaSerialized {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java JsonToJavaSerialized <input_json_file> <output_serialized_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> data = mapper.readValue(new File(inputFile), new TypeReference<List<Map<String, Object>>>(){});

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputFile))) {
            for (Map<String, Object> item : data) {
                oos.writeObject(item);
            }
        }
    }
}