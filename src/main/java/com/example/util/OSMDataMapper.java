package com.example.util;

import java.util.Map;
import java.util.Optional;

/**
 * OSMDataMapper is a functional interface for mapping raw OSM data to specific object types.
 *
 * This interface is designed to be used with the OSMDataLoader to convert Map representations
 * of OSM data into specific object types (e.g., Node, Way).
 *
 * @param <T> The type of object that this mapper will produce.
 */
@FunctionalInterface
public interface OSMDataMapper<T> {

    /**
     * Maps a raw data Map to an object of type T.
     *
     * This method should interpret the raw OSM data in the provided Map and construct
     * an appropriate object of type T. If the data is invalid or insufficient to create
     * a valid object, this method should return an empty Optional.
     *
     * @param data A Map containing raw OSM data. The structure of this Map will depend
     *             on the specific OSM element type (e.g., node, way) being mapped.
     * @return An Optional containing the mapped object if successful, or an empty Optional
     *         if the data could not be mapped to a valid object.
     */
    Optional<T> map(Map<String, Object> data);
}