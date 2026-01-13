package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.auth.LoginRequest;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.auth.LoginResponse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Employee;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.specs.UserSpecifications;
import com.wralonzo.detail_shop.security.jwt.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.auth.domain.mapper.user.UserMapper;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;

@Service
@AllArgsConstructor
@Builder
public class UserService {

    private final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserCreationService userCreationService;
    private final AuditService auditService;
    private final WarehouseService warehouseService;
    private final UserMapper userMapper;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            final String jwt = jwtUtil.generateToken(user);
            Warehouse warehouse = warehouseService.getById(user.getEmployee().getWarehouseId());
            return LoginResponse.builder()
                    .id(user.getId())
                    .token(jwt)
                    .user(userMapper.toShortResponse(user)) // Mapper que ya configuramos
                    .employee(user.getEmployee())
                    .warehouse(warehouse)
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

    @Transactional(readOnly = true)
    public Page<LoginResponse> getAll(String term, String roleName, Pageable pageable) {
        // 1. Obtener la página de usuarios con sus empleados (usando Specifications)
        Specification<User> spec = UserSpecifications.filterUsers(term, roleName);
        Page<User> users = userRepository.findAll(spec, pageable);

        // 2. Optimización: Extraer todos los IDs de Warehouse únicos de la página
        // actual
        List<Long> warehouseIds = users.getContent().stream()
                .map(u -> u.getEmployee() != null ? u.getEmployee().getWarehouseId() : null)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

        // 3. Cargar todos los Almacenes necesarios en una sola consulta
        Map<Long, Warehouse> warehouseMap = warehouseService.getWarehousesMap(warehouseIds);

        // 4. Mapear la página de User a LoginResponse
        return users.map(user -> {
            Employee employee = user.getEmployee();
            Warehouse warehouse = (employee != null) ? warehouseMap.get(employee.getWarehouseId()) : null;

            return LoginResponse.builder()
                    .id(user.getId())
                    .token("") // En listados no se suele enviar el token
                    .user(userMapper.toShortResponse(user))
                    .employee(employee)
                    .warehouse(warehouse)
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public LoginResponse getById(Long id) {
        User user = this.userRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceConflictException("Usuario no encontrado"));
        Warehouse warehouse = warehouseService.getById(user.getEmployee().getWarehouseId());
        return LoginResponse.builder()
                .id(user.getId())
                .token("")
                .user(userMapper.toShortResponse(user)) // Mapper que ya configuramos
                .employee(user.getEmployee())
                .warehouse(warehouse)
                .build();
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
