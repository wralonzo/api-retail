package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.*;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.domain.dto.auth.LoginRequest;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.dto.user.UserUpdateRequest;
import com.wralonzo.detail_shop.domain.entities.Employee;
import com.wralonzo.detail_shop.domain.entities.Role;
import com.wralonzo.detail_shop.domain.entities.User;
import com.wralonzo.detail_shop.security.jwt.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
@Builder
public class UserService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final WarehouseRepository warehouseRepository;
    private final PositionTypeRepository positionTypeRepository;
    private final EmployeeRepository employeeRepository;
    private  final RoleService roleService;

    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            final String jwt = jwtUtil.generateToken(user);
            List<String> roleNames = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            return LoginResponse.builder()
                    .token(jwt)
                    .id(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .avatar(user.getAvatar())
                    .createdAt(user.getCreatedAt())
                    .updateAt(user.getUpdateAt())
                    .deletedAt(user.getDeletedAt())
                    .roles(roleNames)
                    .build();
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
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());
        Employee employee = user.getEmployee();

        if (request.getWarehouseId() != null) {
            employee.setWarehouse(warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bodega no encontrada")));
        }

        if (request.getPositionTypeId() != null) {
            employee.setPositionType(positionTypeRepository.findById(request.getPositionTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado")));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(this.roleService.getRolesFromRequest(request.getRoles()));
        }
        employee.setUpdateAt(LocalDateTime.now());
        user.setUpdateAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
