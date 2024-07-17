package com.example.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DataFileManager {
    private static final String EXTERNAL_BASE_URL = "https://pub-dacce4b1e1694983965461fa4da90666.r2.dev/";
    public static final String DATA_DIR = "data";

    public static File getDataFile(String fileName) throws IOException {
        File dataFile = new File(DATA_DIR, fileName);
        if (dataFile.exists() && dataFile.length() > 0) {
            System.out.println("Using local file: " + dataFile.getAbsolutePath());
            return dataFile;
        }
        System.out.println("Local file not found or empty, attempting download: " + fileName);
        downloadFile(fileName, dataFile);
        return dataFile;
    }

    private static void downloadFile(String fileName, File destination) throws IOException {
        String fullUrl = EXTERNAL_BASE_URL + fileName;
        System.out.println("Attempting to download from: " + fullUrl);
        try {
            URI uri = URI.create(fullUrl);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(uri).build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            
            if (response.statusCode() == 200) {
                Files.write(destination.toPath(), response.body());
            } else {
                throw new IOException("Failed to download file: HTTP status code " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Download interrupted", e);
        }
    }
}