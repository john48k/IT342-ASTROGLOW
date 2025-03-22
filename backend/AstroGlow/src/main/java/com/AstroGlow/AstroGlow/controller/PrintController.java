package com.AstroGlow.AstroGlow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/print")
public class PrintController {

    @GetMapping
    public String printMessage() {
        return "Hello, this is the /print endpoint!";
    }
}