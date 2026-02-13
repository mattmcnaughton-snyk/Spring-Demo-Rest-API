package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class PathTraversalController {


    @GetMapping("/file")
    public String readFile(@RequestParam String filename) throws IOException {
        log.info("Getting File: {}", new File("/tmp/" + filename).getAbsolutePath());
        return Files.readString(Path.of("/tmp/" + filename));
    }


    @GetMapping(value = "/file", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> readFile_secure(@RequestParam String filename) throws IOException {

        Path path = Path.of("/tmp", filename).normalize();

        log.info("File Path: {}", path);

        String content = Files.readString(path);

        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    // Command Injection vulnerability - user input passed directly to shell command
    @GetMapping("/ping")
    public String pingHost(@RequestParam String host) throws IOException {
        log.info("Pinging host: {}", host);
        
        // VULNERABLE: User input is directly concatenated into shell command
        Process process = Runtime.getRuntime().exec("ping -c 1 " + host);
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    // Open Redirect vulnerability - user controls redirect destination
    @GetMapping("/redirect")
    public void redirectToUrl(@RequestParam String url, HttpServletResponse response) throws IOException {
        log.info("Redirecting to: {}", url);
        
        // VULNERABLE: User-controlled URL used directly in redirect
        response.sendRedirect(url);
    }
}
