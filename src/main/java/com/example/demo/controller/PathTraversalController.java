package com.example.demo.controller;

import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RestController
public class PathTraversalController {

    @GetMapping("/file")
    public String readFile(@RequestParam String filename) throws IOException {
        log.info("Getting File: {}", new File("/tmp/" + filename).getAbsolutePath());
          
        return Files.readString(Path.of("/tmp/" + filename));
    }
}
