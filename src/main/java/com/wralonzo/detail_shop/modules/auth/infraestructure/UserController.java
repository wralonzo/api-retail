package com.wralonzo.detail_shop.modules.auth.infraestructure;

import com.wralonzo.detail_shop.infrastructure.adapters.ResponseUtil;
import com.wralonzo.detail_shop.modules.auth.application.GoogleAuthService;
import com.wralonzo.detail_shop.modules.auth.application.UserCreationService;
import com.wralonzo.detail_shop.modules.auth.application.UserService;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.auth.GoogleRequest;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.auth.LoginRequest;
import com.wralonzo.detail_shop.modules.auth.domain.mapper.records.LoginResponse;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.ChangePasswordRequest;
import com.wralonzo.detail_shop.modules.customers.application.ClientService;
import com.wralonzo.detail_shop.modules.customers.domain.dto.client.ClientResponse;
import com.wralonzo.detail_shop.modules.customers.domain.dto.client.FullClientCreateRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserStaffCreateRequest;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserCreationService userCreationService;
    private final GoogleAuthService googleService;
    private final ClientService clientService;

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest user) {
        LoginResponse saveUser = this.userService.login(user);
        return ResponseEntity.ok(saveUser);
    }

    @PostMapping("/user")
    public ResponseEntity<LoginResponse> createUser(@Valid @RequestBody UserStaffCreateRequest request,
            @AuthenticationPrincipal User adminPrincipal) {
        LoginResponse saveUser = this.userCreationService.SaveUser(request);
        return ResponseEntity.ok(saveUser);
    }

    @PostMapping("/user/client")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody FullClientCreateRequest request) {
        ClientResponse saveUser = this.clientService.createFullClient(request);
        return ResponseEntity.ok(saveUser);
    }

    @PatchMapping("/user/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente"));
    }

    @PatchMapping("/user/{id}/activate")
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(Map.of("message", "Usuario activado exitosamente"));
    }

    @PatchMapping("/user/{id}")
    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<LoginResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserStaffCreateRequest request) {

        LoginResponse user = userCreationService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<Page<LoginResponse>> getAll(
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String userType,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        Page<LoginResponse> users = this.userService.getAll(term, roleName, userType, pageable);
        return ResponseUtil.ok(users);
    }

    @GetMapping("/user/{id}/profile")
    @PreAuthorize("hasAnyAuthority('USER_PROFILE', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<LoginResponse> profile(@PathVariable Long id) {
        LoginResponse users = this.userService.getById(id);
        return ResponseUtil.ok(users);
    }

    @PreAuthorize("hasAnyAuthority('USER_CREATE', 'ROLE_SUPER_ADMIN', 'ROLE_ADMIN')")
    @PatchMapping("/user/{id}/password")
    public ResponseEntity<?> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) throws BadRequestException {
        userService.updatePassword(id, request);
        return ResponseUtil.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }

    @PostMapping("/auth/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody GoogleRequest request)
            throws GeneralSecurityException, IOException {
        LoginResponse response = this.googleService.authenticateWithGoogle(request);
        return ResponseEntity.ok(response);
    }
}
