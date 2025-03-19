package com.AstroGlow.controller;

import com.AstroGlow.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.AstroGlow.service.UserService;

import java.util.List;

@RestController
@RequestMapping(method = RequestMethod.GET,path="/api/user")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/print")
    public String print() {
        return "print";
    }
    @GetMapping("/all")
    public List<UserEntity> getAllUsers() {
        return userService.getAllUsers();
    }
}
