package com.astroglow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/_ah/health")
    public String healthCheck() {
        return "OK";
    }
} 