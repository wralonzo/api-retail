package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.auth.domain.mapper.records.LoginResponse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Employee;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.PositionType;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.repositories.ClientRepository;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;

import org.apache.coyote.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserStaffCreateRequest;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;
import com.wralonzo.detail_shop.modules.auth.domain.mapper.user.UserMapper;

@Service
@AllArgsConstructor
@Builder
public class UserCreationService {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final WarehouseService warehouseService;
    private final PositionTypeService positionTypeService;
    private final UserMapper userMapper;
    private final ProfileService profileService;

    @Transactional
    public Client SaveClient(Client request) {
        return this.clientRepository.save(request);
    }

    private void userExist(String username) {
        if (this.userRepository.existsByUsername(username)) {
            throw new ResourceConflictException("El usuario " + username + " ya existe.");
        }
    }

    @Transactional
    public LoginResponse SaveUser(UserStaffCreateRequest request) {
        // 1. Validaciones de existencia
        this.userExist(request.getUsername());
        this.profileService.emailExist(request.getEmail());

        // 2. Obtener referencias necesarias
        Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
        PositionType positionType = positionTypeService.findById(request.getPositionTypeId());

        // 3. Construir Profile
        Profile profile = Profile.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .birthDate(request.getBirthDate())
                .avatar(request.getAvatar())
                .build();

        // 4. Construir Employee
        Employee employee = Employee.builder()
                .warehouseId(warehouse.getId())
                .positionType(positionType)
                .build();

        // 5. Construir User y vincular ambos
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .passwordInit(request.getPassword())
                .enabled(true)
                .profile(profile) // Vinculación
                .employee(employee) // Vinculación
                .roles(roleService.getRolesFromRequest(request.getRoles()))
                .build();

        // 6. Persistencia única (Cascade guarda Profile y Employee automáticamente)
        User newUser = userRepository.save(user);

        return userMapper.toLoginResponse(newUser, "");
    }

    @Transactional
    public LoginResponse updateUser(long id, UserStaffCreateRequest request) {
        // 1. Obtener el usuario actual
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Validaciones de unicidad (Solo si los valores cambiaron)
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            this.validateUsernameForUpdate(id, request.getUsername());
            user.setUsername(request.getUsername());
        }

        // Dentro de updateUser(long id, UserStaffCreateRequest request)

        if (user.getProfile() != null) {
            // 1. Validación de Email (Crítico)
            if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getProfile().getEmail())) {
                this.profileService.validateEmailForUpdate(id, request.getEmail());
                user.getProfile().setEmail(request.getEmail().toLowerCase());
            }

            // 2. Campos de texto básicos
            if (request.getFullName() != null) {
                user.getProfile().setFullName(request.getFullName().trim());
            }

            if (request.getPhone() != null) {
                user.getProfile().setPhone(request.getPhone());
            }

            if (request.getAddress() != null) {
                user.getProfile().setAddress(request.getAddress());
            }

            if (request.getAvatar() != null) {
                user.getProfile().setAvatar(request.getAvatar());
            }

            // 3. Fecha de nacimiento
            if (request.getBirthDate() != null) {
                user.getProfile().setBirthDate(request.getBirthDate());
            }
        }
        // 3. Actualización de Referencias (Warehouse y Position)
        if (request.getWarehouseId() != null) {
            Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
            user.getEmployee().setWarehouseId(warehouse.getId()); // Suponiendo que la relación existe
        }

        if (user.getEmployee() != null) {
            // Actualización de la posición (Cargo)
            if (request.getPositionTypeId() != null) {
                // Solo buscamos si el ID es diferente al actual para ahorrar una consulta
                if (user.getEmployee().getPositionType() == null ||
                        !request.getPositionTypeId().equals(user.getEmployee().getPositionType().getId())) {

                    PositionType positionType = positionTypeService.findById(request.getPositionTypeId());
                    user.getEmployee().setPositionType(positionType);
                }
            }

            // Auditoría automática
            user.getEmployee().setUpdateAt(LocalDateTime.now());
        }

        // 4. Actualización de campos básicos
        if (request.getPhone() != null)
            user.getProfile().setPhone(request.getPhone());
        if (request.getAddress() != null)
            user.getProfile().setAddress(request.getAddress());

        // 5. Roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(this.roleService.getRolesFromRequest(request.getRoles()));
        }

        user.setUpdateAt(LocalDateTime.now());
        user.getEmployee().setUpdateAt(LocalDateTime.now());

        return userMapper.toLoginResponse(userRepository.save(user), "");
    }

    @Transactional
    public User updatePassword(Long id, String newPassword, User userToken) {
        // 1. Buscar usuario
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordInit(null);

        return userRepository.save(user);
    }

    // En tu UserService o Validations
    public void validateUsernameForUpdate(Long currentUserId, String newUsername) {
        userRepository.findByUsername(newUsername)
                .ifPresent(existingUser -> {
                    if (!existingUser.getId().equals(currentUserId)) {
                        new BadRequestException("El nombre de usuario ya está en uso por otra cuenta.");
                    }
                });
    }
}
