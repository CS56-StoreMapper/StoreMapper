package com.example.util;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;


/**
 * OSMConverter is a utility class for converting OpenStreetMap (OSM) XML data
 * into a serialized format that can be more efficiently processed by the application.
 *
 * This class handles the following tasks:
 * 1. Reading OSM XML data from various input formats (plain, gzip, bzip2)
 * 2. Parsing the XML to extract nodes, ways, and bounds
 * 3. Processing and transforming the data (e.g., handling one-way streets, speed limits)
 * 4. Writing the processed data to separate serialized files for nodes, ways, and bounds
 *
 * Usage:
 * java OSMConverter <filename>
 *
 * Where <filename> is the path to an OSM XML file (can be .osm, .osm.gz, or .osm.bz2)
 *
 * Output:
 * The converter produces three files:
 * - <basename>.nodes: Serialized Node objects
 * - <basename>.ways: Serialized Way objects
 * - <basename>.bounds: Serialized bounds information
 *
 * Where <basename> is the input filename without the extension.
 */
public class OSMConverter {

    /**
     * Converts an OSM XML file to the serialized format used by the application.
     *
     * @param filename The path to the OSM XML file to convert
     * @throws Exception If there's an error during the conversion process
     */
    public static void convertOSMToSerializedFormat(String filename) throws Exception {
        InputStream inputStream = getInputStream(filename);
        String baseName = filename.replaceFirst("\\.(osm|xml)(\\.(?:gz|bz2))?$", "");

        ObjectOutputStream nodesOut = new ObjectOutputStream(new FileOutputStream(baseName + ".nodes"));
        ObjectOutputStream waysOut = new ObjectOutputStream(new FileOutputStream(baseName + ".ways"));
        ObjectOutputStream boundsOut = new ObjectOutputStream(new FileOutputStream(baseName + ".bounds"));

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(inputStream);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "node":
                        processNode(reader, nodesOut);
                        break;
                    case "way":
                        processWay(reader, waysOut);
                        break;
                    case "bounds":
                        processBounds(reader, boundsOut);
                        break;
                }
            }
        }

        reader.close();
        nodesOut.close();
        waysOut.close();
        boundsOut.close();
    }

    /**
     * Creates an appropriate InputStream based on the file extension.
     * Supports plain, gzip, and bzip2 compressed files.
     *
     * @param filename The path to the input file
     * @return An InputStream for reading the file
     * @throws IOException If there's an error opening the file
     */
    private static InputStream getInputStream(String filename) throws IOException {
        if (filename.endsWith(".gz")) {
            return new GZIPInputStream(new FileInputStream(filename));
        } else if (filename.endsWith(".bz2")) {
            return new BZip2CompressorInputStream(new FileInputStream(filename));
        } else {
            return new FileInputStream(filename);
        }
    }

    /**
     * Processes a single node element from the XML.
     * Extracts id, latitude, longitude, and tags.
     *
     * @param reader The XMLStreamReader positioned at the start of a node element
     * @param out The ObjectOutputStream to write the processed node data
     * @throws Exception If there's an error processing the node
     */
    private static void processNode(XMLStreamReader reader, ObjectOutputStream out) throws Exception {
        Map<String, Object> node = new HashMap<>();
        node.put("id", Long.parseLong(reader.getAttributeValue(null, "id")));
        node.put("lat", Double.parseDouble(reader.getAttributeValue(null, "lat")));
        node.put("lon", Double.parseDouble(reader.getAttributeValue(null, "lon")));
        node.put("tags", new HashMap<String, String>());

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("node")) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT && reader.getLocalName().equals("tag")) {
                String key = reader.getAttributeValue(null, "k");
                String value = reader.getAttributeValue(null, "v");
                ((Map<String, String>)node.get("tags")).put(key, value);
            }
        }

        out.writeObject(node);
    }

    /**
     * Processes a single way element from the XML.
     * Extracts id, node references, and tags.
     * Handles special processing for one-way streets and speed limits.
     *
     * @param reader The XMLStreamReader positioned at the start of a way element
     * @param out The ObjectOutputStream to write the processed way data
     * @throws Exception If there's an error processing the way
     */
    private static void processWay(XMLStreamReader reader, ObjectOutputStream out) throws Exception {
        Map<String, Object> way = new HashMap<>();
        way.put("id", Long.parseLong(reader.getAttributeValue(null, "id")));
        way.put("nodes", new ArrayList<Long>());
        way.put("tags", new HashMap<String, String>());

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT && reader.getLocalName().equals("way")) {
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "nd":
                        ((List<Long>)way.get("nodes")).add(Long.parseLong(reader.getAttributeValue(null, "ref")));
                        break;
                    case "tag":
                        String key = reader.getAttributeValue(null, "k");
                        String value = reader.getAttributeValue(null, "v");
                        ((Map<String, String>)way.get("tags")).put(key, value);
                        break;
                }
            }
        }

        // Process one-way tags similar to the Python code
        Map<String, String> tags = (Map<String, String>)way.get("tags");
        if (tags.containsKey("oneway")) {
            String oneway = tags.get("oneway");
            if (oneway.equals("reversible") || oneway.equals("-1")) {
                tags.put("oneway", "yes");
                if (oneway.equals("-1")) {
                    Collections.reverse((List<Long>)way.get("nodes"));
                }
            }
        }

        // Process speed limits
        for (String tagName : Arrays.asList("maxspeed", "maxspeed:advisory")) {
            if (tags.containsKey(tagName)) {
                try {
                    int speed = Integer.parseInt(tags.get(tagName).split(" ")[0]);
                    tags.put("maxspeed_mph", String.valueOf(speed));
                    break;
                } catch (NumberFormatException e) {
                    // Ignore if we can't parse the speed
                }
            }
        }

        out.writeObject(way);
    }

    /**
     * Processes the bounds element from the XML.
     * Extracts minimum and maximum latitude and longitude.
     *
     * @param reader The XMLStreamReader positioned at the start of a bounds element
     * @param out The ObjectOutputStream to write the processed bounds data
     * @throws Exception If there's an error processing the bounds
     */
    private static void processBounds(XMLStreamReader reader, ObjectOutputStream out) throws Exception {
        Map<String, Double> bounds = new HashMap<>();
        bounds.put("minlat", Double.parseDouble(reader.getAttributeValue(null, "minlat")));
        bounds.put("minlon", Double.parseDouble(reader.getAttributeValue(null, "minlon")));
        bounds.put("maxlat", Double.parseDouble(reader.getAttributeValue(null, "maxlat")));
        bounds.put("maxlon", Double.parseDouble(reader.getAttributeValue(null, "maxlon")));
        out.writeObject(bounds);
    }

    /**
     * Main method to run the OSM converter from the command line.
     *
     * @param args Command line arguments. Expects a single argument: the input filename.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java OSMConverter <filename>");
            return;
        }
        try {
            convertOSMToSerializedFormat(args[0]);
            System.out.println("Conversion completed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
