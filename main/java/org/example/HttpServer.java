package com.iprody;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpServer {

    private static final String STATIC_DIR = "simple-http-server/static";

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server started at http://localhost:8080");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String requestLine = in.readLine();
            if (requestLine == null) {
                clientSocket.close();
                continue;
            }

            System.out.println("Request Line: " + requestLine);

            // Разбиваем строку запроса на части по пробелам
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) {
                clientSocket.close();
                continue;
            }

            String urlPath = requestParts[1];

            if (urlPath.startsWith("/")) {
                urlPath = urlPath.substring(1);
            }

            Path filePath = Paths.get(STATIC_DIR, urlPath).normalize();

            System.out.println("Requested URL Path: " + urlPath);
            System.out.println("Resolved File Path: " + filePath.toAbsolutePath());
            System.out.println("File Exists: " + Files.exists(filePath));
            System.out.println("Is Directory: " + Files.isDirectory(filePath));

            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                 System.out.println(line);
            }

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                byte[] fileContent = Files.readAllBytes(filePath);
                String contentType = Files.probeContentType(filePath);

                out.write("HTTP/1.1 200 OK\r\n");
                out.write("Content-Type: " + contentType + "\r\n");
                out.write("Content-Length: " + fileContent.length + "\r\n");
                out.write("\r\n");
                out.flush();
                clientSocket.getOutputStream().write(fileContent);
            } else {
                String notFoundResponse = "<h1>404 Not Found</h1>";
                out.write("HTTP/1.1 404 Not Found\r\n");
                out.write("Content-Type: text/html; charset=UTF-8\r\n");
                out.write("Content-Length: " + notFoundResponse.length() + "\r\n");
                out.write("\r\n");
                out.write(notFoundResponse);
            }
            out.flush();
            clientSocket.close();
        }
    }
}