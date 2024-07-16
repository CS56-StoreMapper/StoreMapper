package com.example.util;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Function;

public class OSMDataLoader {

    /**
     * Loads OSM data from a serialized file and converts it to a list of objects.
     *
     * This method reads serialized Map<String, Object> instances from the specified file,
     * and uses the provided mapper function to convert each map to an object of type T.
     * It continues reading until it reaches the end of the file (EOFException).
     *
     * The method handles the following scenarios:
     * 1. Successfully reads and converts a map to an object: The object is added to the result list.
     * 2. Reads a map but fails to convert it: The map is skipped (no exception thrown).
     * 3. Reaches the end of the file: Stops reading and returns the accumulated list.
     * 4. Encounters any other IOException: Throws the exception.
     *
     * @param <T> The type of object to be created from the OSM data.
     * @param filename The path to the serialized file containing OSM data.
     * @param mapper A Function that takes a Map<String, Object> and returns an object of type T.
     *               This is typically a method reference (e.g., Node::fromMap) or a lambda expression
     *               that defines how to convert the raw map data into the desired object type.
     * @return A List<T> containing all successfully created objects.
     * @throws IOException If there's an error reading the file (except EOFException).
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
    // public <T> List<T> loadData(String filename, Function<Map<String, Object>, T> mapper) throws IOException, ClassNotFoundException {
    //     try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
    //         List<T> items = new ArrayList<>();
    //         while (true) {
    //             try {
    //                 @SuppressWarnings("unchecked")
    //                 Map<String, Object> dataMap = (Map<String, Object>) ois.readObject();
    //                 items.add(mapper.apply(dataMap));
    //             } catch (EOFException e) {
    //                 break;
    //             }
    //         }
    //         return items;
    //     }
    // }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T> List<T> loadData(String filename, Function<Map<String, Object>, T> mapper) throws IOException {
        File dataFile = DataFileManager.getDataFile(filename);
        try {
            List<Map<String, Object>> dataList = objectMapper.readValue(dataFile, 
                                                 new TypeReference<List<Map<String, Object>>>() {});
            return dataList.stream()
                           .map(mapper)
                           .toList();
        } catch (JsonParseException e) {
            System.err.println("Error parsing JSON file: " + filename);
            System.err.println("First 100 characters of file:");
            try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
                char[] buffer = new char[100];
                reader.read(buffer);
                System.err.println(new String(buffer));
            }
            throw e;
        }
    }

    public <T> void saveData(List<T> items, String filename) throws IOException {
        objectMapper.writeValue(new File(filename), items);
    }

    /**
     * Utility methods for safe type conversion from Map entries to specific types.
     * These methods handle potential null values and type mismatches gracefully.
     */

    /**
     * Safely retrieves a Long value from a Map.
     *
     * @param map The source Map
     * @param key The key to look up
     * @return An Optional containing the Long value if present and of the correct type, otherwise an empty Optional
     */
    public static Optional<Long> getLong(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::longValue);
    }

    /**
     * Safely retrieves a Double value from a Map.
     *
     * @param map The source Map
     * @param key The key to look up
     * @return An Optional containing the Double value if present and of the correct type, otherwise an empty Optional
     */
    public static Optional<Double> getDouble(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .map(Number::doubleValue);
    }

    /**
     * Safely retrieves a Map<String, String> from a Map.
     * This method ensures that both keys and values in the resulting map are Strings.
     *
     * @param map The source Map
     * @param key The key to look up
     * @return An Optional containing the Map<String, String> if present and all entries are Strings, otherwise an empty Optional
     */
    public static Optional<Map<String, String>> getStringMap(Map<String, Object> map, String key) {
        return Optional.ofNullable(map.get(key))
                .filter(Map.class::isInstance)
                .map(m -> (Map<?, ?>) m)
                .map(m -> m.entrySet().stream()
                        .filter(e -> e.getKey() instanceof String && e.getValue() instanceof String)
                        .collect(Collectors.toMap(
                                e -> (String) e.getKey(),
                                e -> (String) e.getValue()
                        ))
                );
    }

    /**
     * Safely retrieves a List of a specific type from a Map.
     * This method ensures that all elements in the resulting list are of the specified type.
     *
     * @param <T> The type of elements in the list
     * @param map The source Map
     * @param key The key to look up
     * @param elementType The Class object representing the type of elements expected in the list
     * @return An Optional containing the List<T> if present, all elements are of the correct type, and the list is non-empty; otherwise an empty Optional
     */
    public static <T> Optional<List<T>> getList(Map<String, Object> map, String key, Class<T> elementType) {
        return Optional.ofNullable(map.get(key))
                .filter(List.class::isInstance)
                .map(list -> (List<?>) list)
                .map(list -> list.stream()
                        .filter(elementType::isInstance)
                        .map(elementType::cast)
                        .collect(Collectors.toList())
                )
                .filter(list -> !list.isEmpty());
    }
}