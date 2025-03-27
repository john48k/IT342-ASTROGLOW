package com.astroglow.controller;

import com.astroglow.Entity.AuthenticationEntity;
import com.astroglow.Service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(method= RequestMethod.GET, path ="/api/authentication")
public class AuthenticationController {
    @Autowired
    AuthenticationService aserv;
    @PostMapping("/postAuthentication")
    public AuthenticationEntity postAnalytics(@RequestBody AuthenticationEntity authentication) {
        return aserv.postAuthentication(authentication);
    }
    @GetMapping("/getAllAuthentication")
    public List<AuthenticationEntity> getAllAnalytics() {
        return aserv.getAllAuthentication();
    }
    @PutMapping("/putAuthentication")
    public AuthenticationEntity putAnalytics(@RequestParam int id,@RequestBody AuthenticationEntity newAuthentication) {
        return aserv.putAuthentication(id, newAuthentication);
    }
    @DeleteMapping("/deleteAuthentication")
    public String deleteAuthentication(@PathVariable int id) {
        return aserv.deleteAuthentication(id);
    }
}
