package com.AstroGlow.AstroGlow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    @GetMapping("/print")
    public String printMessage() {
        return "Hello, this is the /users/print endpoint!";
    }
}