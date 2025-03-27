package com.astroglow.controller;

import com.astroglow.Entity.UserEntity;
import com.astroglow.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @GetMapping
    public String index(){
        return "<h1>Welcome, This is a landing page</h1>";
    }
    @Autowired
    UserService userService;

    @PostMapping(value ="/postUser", consumes = "application/json")
    public UserEntity postUser(@RequestBody UserEntity user) {
        return userService.postUser(user);
    }

    @GetMapping("/getAllUser")
    public List<UserEntity> getAllUser(){
        return userService.getAllUsers();
    }

    @PutMapping("/putUser")
    public UserEntity putUser(@RequestParam int id, @RequestBody UserEntity newUserDetails) {
        return userService.putUser(id, newUserDetails);
    }

    @DeleteMapping("/deleteUser/{id}")  // Fixed path variable in URL
    public String deleteUser(@PathVariable int id) {
        return userService.deleteUser(id);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserEntity> signup(@RequestBody UserEntity user) {
        UserEntity newUser = userService.registerUser(user);
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity user) {
        UserEntity loggedInUser = userService.loginUser(user.getUserName(), user.getUserPassword());
        if (loggedInUser != null) {
            return ResponseEntity.ok(loggedInUser);
        }
        return ResponseEntity.status(401).body(null);
    }
    

    // This is for the Google and Github JSON user-info
    @GetMapping("/user-info")
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User != null) {
            return oAuth2User.getAttributes();
        } else {
            return Collections.emptyMap();
        }
    }
}
