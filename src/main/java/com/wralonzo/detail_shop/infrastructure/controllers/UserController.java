package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.UserCreationService;
import com.wralonzo.detail_shop.application.services.UserService;
import com.wralonzo.detail_shop.domain.dto.auth.LoginRequest;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.dto.user.UserClient;
import com.wralonzo.detail_shop.domain.dto.user.UserRequest;
import com.wralonzo.detail_shop.domain.dto.user.UserUpdateRequest;
import com.wralonzo.detail_shop.domain.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {
    private final UserService userService;
    private final UserCreationService userCreationService;

    public UserController(UserService userService, UserCreationService userCreationService){
        this.userService = userService;
        this.userCreationService = userCreationService;
    }

    @PostMapping("/staff")
    public ResponseEntity<User> createUser(@RequestBody UserRequest request){
        User saveUser = this.userCreationService.SaveUser(request);
        return ResponseEntity.ok(saveUser);
    }

    @PostMapping("/client")
    public ResponseEntity<User> createClient(@RequestBody UserClient request){
        User saveUser = this.userCreationService.SaveClient(request);
        return ResponseEntity.ok(saveUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> createUser(@RequestBody LoginRequest user){
        LoginResponse saveUser = this.userService.login(user);
        return ResponseEntity.ok(saveUser);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')") // Solo un admin debería poder desactivar usuarios
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario activado exitosamente"));
    }

    // URL: PATCH /api/users/5
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserUpdateRequest> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest request) {

        userService.updateUser(id, request);
        return ResponseEntity.ok(request);
    }
}
