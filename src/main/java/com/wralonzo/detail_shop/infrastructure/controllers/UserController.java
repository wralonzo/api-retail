package com.wralonzo.detail_shop.infrastructure.controllers;

import com.wralonzo.detail_shop.application.services.UserCreationService;
import com.wralonzo.detail_shop.application.services.UserService;
import com.wralonzo.detail_shop.domain.dto.auth.LoginRequest;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.dto.user.ChangePasswordRequest;
import com.wralonzo.detail_shop.domain.dto.user.UserClient;
import com.wralonzo.detail_shop.domain.dto.user.UserRequest;
import com.wralonzo.detail_shop.domain.entities.User;
import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("")
public class UserController {
    private final UserService userService;
    private final UserCreationService userCreationService;

    public UserController(UserService userService, UserCreationService userCreationService) {
        this.userService = userService;
        this.userCreationService = userCreationService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> createUser(@Valid @RequestBody LoginRequest user) {
        LoginResponse saveUser = this.userService.login(user);
        return ResponseEntity.ok(saveUser);
    }

    @PostMapping("/user")
    public ResponseEntity<LoginResponse> createUser(@Valid @RequestBody UserRequest request) {
        LoginResponse saveUser = this.userCreationService.SaveUser(request);
        return ResponseEntity.ok(saveUser);
    }

    @PostMapping("/user/client")
    public ResponseEntity<User> createClient(@Valid @RequestBody UserClient request) {
        User saveUser = this.userCreationService.SaveClient(request, true);
        return ResponseEntity.ok(saveUser);
    }

    @PatchMapping("/user/{id}/deactivate")
    @PreAuthorize("hasAuthority('ADMIN')") // Solo un admin debería poder desactivar usuarios
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente"));
    }

    @PatchMapping("/user/{id}/activate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario activado exitosamente"));
    }

    @PatchMapping("/user/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserRequest> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {

        userService.updateUser(id, request);
        return ResponseEntity.ok(request);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<LoginResponse>> getAll(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String roleName,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<LoginResponse> users = this.userService.getAll(term, roleName, pageable);
        return ResponseUtil.ok(users);
    }

    @GetMapping("/user/{id}/profile")
    public ResponseEntity<LoginResponse> profile(@PathVariable Long id) {
        LoginResponse users = this.userService.getById(id);
        return ResponseUtil.ok(users);
    }

    @PatchMapping("/user/{id}/password")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {

        userService.updatePassword(id, request.getNewPassword(), request.getMotive());

        return ResponseUtil.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }

}
