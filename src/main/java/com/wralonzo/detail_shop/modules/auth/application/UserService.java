package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.configuration.exception.ResourceUnauthorizedException;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.auth.LoginRequest;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.ChangePasswordRequest;
import com.wralonzo.detail_shop.modules.auth.domain.mapper.records.LoginResponse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.specs.UserSpecifications;
import com.wralonzo.detail_shop.security.jwt.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

import com.wralonzo.detail_shop.modules.auth.domain.mapper.user.UserMapper;

@Service
@AllArgsConstructor
@Builder
public class UserService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserCreationService userCreationService;
    private final AuditService auditService;
    private final UserMapper userMapper;
    private final PasswordHistoryService passwordHistoryService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = (User) auth.getPrincipal();
        final String jwt = jwtUtil.generateToken(user);

        // Mapeamos el objeto con toda la información incluida
        return userMapper.toLoginResponse(user, jwt);
    }

    public Optional<User> findUserByEmail(String email) {
        return this.userRepository.findByUsername(email);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        user.setEnabled(false);
        user.setDeletedAt(LocalDateTime.now()); // Marcamos la fecha de desactivación
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setEnabled(true);
        user.setDeletedAt(null); // Limpiamos la fecha
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<LoginResponse> getAll(String term, String roleName, Pageable pageable) {
        Specification<User> spec = UserSpecifications.filterUsers(term, roleName);
        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(user -> {
            return userMapper.toLoginResponse(user, "");
        });
    }

    @Transactional(readOnly = true)
    public LoginResponse getById(Long id) {
        User user = this.userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceConflictException("Usuario no encontrado"));
        return userMapper.toLoginResponse(user, "");
    }

    @Transactional
    public void updatePassword(Long id, ChangePasswordRequest changeRequest) throws BadRequestException {
        // 1 & 2. Metadatos de Red
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes();
        HttpServletRequest httpRequest = attributes.getRequest();
        String auditMetadata = " [IP: " + getClientIp(httpRequest) + " | Dispositivo: "
                + httpRequest.getHeader("User-Agent") + "]";

        // 3. Obtener Ejecutor
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceUnauthorizedException("No hay una sesión activa");
        }
        User userExecutor = (User) authentication.getPrincipal();

        // 4 & 5. Permisos
        boolean isAdmin = userExecutor.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !userExecutor.getId().equals(id)) {
            throw new ResourceUnauthorizedException("No tienes permiso para cambiar la contraseña de otro usuario.");
        }

        // 6. Obtener Usuario Objetivo
        User userTarget = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 7. NUEVO: Validar Fortaleza de la Contraseña
        this.validatePasswordComplexity(changeRequest.getNewPassword());

        // 8. Validar Historial (Lanza excepción si es una de las últimas 5)
        // Nota: Debes pasar la nueva contraseña SIN encriptar para que el matches
        // funcione dentro
        this.passwordHistoryService.validatePasswordHistory(userTarget, changeRequest.getNewPassword());

        // 9. Actualizar Entidad
        userTarget.setPassword(passwordEncoder.encode(changeRequest.getNewPassword()));
        userTarget.setPasswordLastChangedAt(LocalDateTime.now());
        userRepository.save(userTarget);

        // 10. Auditoría
        auditService.logAction(
                userExecutor.getUsername(),
                "CHANGE_PASSWORD",
                "El usuario: " + userExecutor.getUsername() +
                        " actualizó la contraseña de: " + userTarget.getUsername() +
                        " motivo: " + changeRequest.getMotive(),
                changeRequest.getChannel(),
                auditMetadata);
    }

    // Método de soporte para capturar la IP real (detrás de proxies)
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void validatePasswordComplexity(String password) throws BadRequestException {
        // Mínimo 8 caracteres, máximo 20
        if (password.length() < 8 || password.length() > 20) {
            throw new BadRequestException("La contraseña debe tener entre 8 y 20 caracteres.");
        }

        // Al menos una mayúscula
        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("La contraseña debe contener al menos una letra mayúscula.");
        }

        // Al menos un número
        if (!password.matches(".*[0-9].*")) {
            throw new BadRequestException("La contraseña debe contener al menos un número.");
        }

        // Al menos un carácter especial
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new BadRequestException("La contraseña debe contener al menos un carácter especial.");
        }
    }
}
