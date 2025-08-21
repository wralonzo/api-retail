package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.UserService;
import com.wralonzo.detail_shop.domain.dto.auth.LoginRequest;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> createUser(@RequestBody User user){
        User saveUser = this.userService.createUser(user);
        return ResponseEntity.ok(saveUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> createUser(@RequestBody LoginRequest user){
        LoginResponse saveUser = this.userService.login(user);
        return ResponseEntity.ok(saveUser);
    }
}
