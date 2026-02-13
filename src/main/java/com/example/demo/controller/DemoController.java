package com.example.demo.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        System.out.println("Hello from Spring Boot!");
        return "Hello from Spring Boot!";
    }
}

