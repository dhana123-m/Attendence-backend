package com.attendance;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainServer {

    public static void main(String[] args) throws Exception {
        // Ensure directories and files exist
        Files.createDirectories(Paths.get("known_faces"));
        if (!Files.exists(Paths.get("attendance.txt"))) {
            Files.createFile(Paths.get("attendance.txt"));
        }

        // Create HTTP server on port 8182
        HttpServer server = HttpServer.create(new InetSocketAddress(8182), 0);

        // ✅ Home route (CORS-enabled)
        server.createContext("/", exchange -> {
            addCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String response = "✅ Attendance Server Running (Port 8182)";
            sendResponse(exchange, response);
        });

        // ✅ Register route (CORS-enabled)
        server.createContext("/register", exchange -> {
            addCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, "❌ Only POST method allowed");
                return;
            }

            try {
                handleFileUpload(exchange);
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, "❌ Error during registration: " + e.getMessage());
            }
        });

        // ✅ Recognition route (CORS-enabled)
        server.createContext("/recognize", exchange -> {
            addCORS(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            System.out.println("🔍 Recognition request received...");

            String result;
            try {
                result = FaceRecognizer.recognizeOnce();
                System.out.println("🎯 Recognition result: " + result);
            } catch (Exception e) {
                e.printStackTrace();
                result = "❌ Error during recognition: " + e.getMessage();
            }

            sendResponse(exchange, result);
        });

        // Start server
        server.setExecutor(null);
        server.start();
        System.out.println("✅ Attendance Server started on port 8182...");
        System.out.println("🌐 Open: http://localhost:8182/");
        System.out.println("🖼️ Register: POST http://localhost:8182/register");
        System.out.println("🎥 Recognize: GET http://localhost:8182/recognize");
    }

    // --- ✅ Add CORS headers (fixes browser blocking issues)
    private static void addCORS(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
    }

    // --- Utility: send simple text response
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // --- Handle photo upload (multipart/form-data)
    private static void handleFileUpload(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-type");
        if (contentType == null || !contentType.contains("multipart/form-data")) {
            sendResponse(exchange, "❌ Invalid Content-Type. Use multipart/form-data.");
            return;
        }

        // Extract boundary from Content-Type header
        String boundary = contentType.split("boundary=")[1];
        if (boundary == null) {
            sendResponse(exchange, "❌ Missing boundary in request.");
            return;
        }

        // Read full request body
        InputStream is = exchange.getRequestBody();
        byte[] data = is.readAllBytes();
        String body = new String(data, StandardCharsets.ISO_8859_1); // preserve raw bytes

        // Parse name field
        String name = null;
        int nameIndex = body.indexOf("name=\"name\"");
        if (nameIndex != -1) {
            int start = body.indexOf("\r\n\r\n", nameIndex) + 4;
            int end = body.indexOf("\r\n", start);
            name = body.substring(start, end).trim();
        }

        if (name == null || name.isEmpty()) {
            sendResponse(exchange, "❌ Missing name field.");
            return;
        }

        // Parse photo file bytes
        int fileIndex = body.indexOf("name=\"photo\"");
        if (fileIndex == -1) {
            sendResponse(exchange, "❌ Missing photo file.");
            return;
        }

        int fileStart = body.indexOf("\r\n\r\n", fileIndex) + 4;
        int fileEnd = body.indexOf("--" + boundary, fileStart) - 2;

        byte[] fileBytes = Arrays.copyOfRange(data, fileStart, fileEnd);

        // Save the uploaded file
        Path filePath = Paths.get("known_faces", name + ".jpg");
        Files.write(filePath, fileBytes);
        sendResponse(exchange, "✅ Registered successfully as: " + name);
        System.out.println("✅ Saved face for: " + name + " -> " + filePath);
    }
}
