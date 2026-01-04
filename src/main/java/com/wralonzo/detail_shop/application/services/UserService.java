package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.*;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.domain.dto.auth.LoginRequest;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.dto.user.UserRequest;
import com.wralonzo.detail_shop.domain.entities.Employee;
import com.wralonzo.detail_shop.domain.entities.User;
import com.wralonzo.detail_shop.security.jwt.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Builder
public class UserService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final WarehouseRepository warehouseRepository;
    private final PositionTypeRepository positionTypeRepository;
    private final RoleService roleService;
    private final UserCreationService userCreationService;
    private final AuditService auditService;

    @Transactional

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            final String jwt = jwtUtil.generateToken(user);
            return userCreationService.converUser(user, jwt);

        }

        throw new ResourceConflictException("El sistema no pudo recuperar el perfil del usuario.");
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

    @Transactional
    public void updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (request.getUser().getFullName() != null)
            user.setFullName(request.getUser().getFullName());
        if (request.getUser().getPhone() != null)
            user.setPhone(request.getUser().getPhone());
        if (request.getUser().getAddress() != null)
            user.setAddress(request.getUser().getAddress());
        if (request.getUser().getAvatar() != null)
            user.setAvatar(request.getUser().getAvatar());
        Employee employee = user.getEmployee();

        if (request.getWarehouse() != null) {
            employee.setWarehouse(warehouseRepository.findById(request.getWarehouse())
                    .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada")));
        }

        if (request.getPositionType() != null) {
            employee.setPositionType(positionTypeRepository.findById(request.getPositionType())
                    .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado")));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(this.roleService.getRolesFromRequest(request.getRoles()));
        }
        employee.setUpdateAt(LocalDateTime.now());
        user.setUpdateAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Page<LoginResponse> getAll(String term, String roleName, Pageable pageable) {
        String cleanTerm = (term != null && !term.trim().isEmpty()) ? term.trim() : null;
        String cleanRole = (roleName != null && !roleName.trim().isEmpty()) ? roleName.trim() : null;
        Page<User> users = this.userRepository.findAllWithFilters(cleanTerm, cleanRole, pageable);
        return users.map(user -> userCreationService.converUser(user, ""));
    }
    

    @Transactional(readOnly = true)
    public LoginResponse getById(Long id) {
        User user = this.userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceConflictException("Cliente no encontrado"));
        return userCreationService.converUser(user, "");
    }

    @Transactional
    public void updatePassword(Long id, String newPassword, String motive) {
        final User user = userCreationService.updatePassword(id, newPassword);
        // 3. Registrar en Bitácora
        auditService.logAction(
                user.getUsername(),
                "CHANGE_PASSWORD",
                "El usuario cambió su contraseña por " + motive + ".");
    }

}
