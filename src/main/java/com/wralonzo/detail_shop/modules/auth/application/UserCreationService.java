package com.wralonzo.detail_shop.modules.auth.application;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.infrastructure.utils.PasswordUtils;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.auth.LoginResponse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Employee;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.EmployeeRepository;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.repositories.ClientRepository;

import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.PositionType;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserStaffCreateRequest;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
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
    private final EmployeeRepository employeeRepository;
    private final UserMapper userMapper;

    @Transactional
    public Client SaveClient(Client request) {
        return this.clientRepository.save(request);
    }

    private void userExist(String user) {
        final Optional<User> userFound = this.userRepository.findByUsername(user);
        if (userFound.isPresent()) {
            throw new ResourceConflictException("El usuario " + user + " ya existe.");
        }
    }

    @Transactional
    public LoginResponse SaveUser(UserStaffCreateRequest request) {
        this.userExist(request.getUsername());
        User user = User.builder()
                .enabled(true)
                .username(request.getUsername())
                .roles(roleService.getRolesFromRequest(request.getRoles()))
                .build();
        User newUser = this.createUser(user);
        Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
        Employee employeeDB = this.createEmployee(request, user, warehouse);
        return LoginResponse.builder()
                .id(newUser.getId())
                .token("")
                .user(userMapper.toShortResponse(newUser)) // Mapper que ya configuramos
                .employee(employeeDB)
                .warehouse(warehouse)
                .build();
    }

    public LoginResponse updateUser(long id, UserStaffCreateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (request.getUsername() != null)
            user.setUsername(request.getUsername());
        if (request.getPhone() != null)
            user.getProfile().setPhone(request.getPhone());
        if (request.getAddress() != null)
            user.getProfile().setAddress(request.getAddress());
        if (request.getAvatar() != null)
            user.getProfile().setAvatar(request.getAvatar());
        Employee employee = user.getEmployee();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            // pendiente enviar roles
            user.setRoles(this.roleService.getRolesFromRequest(request.getRoles()));
        }
        employee.setUpdateAt(LocalDateTime.now());
        user.setUpdateAt(LocalDateTime.now());
        Warehouse warehouse = warehouseService.getById(request.getWarehouseId());
        Employee employeeDB = this.createEmployee(request, user, warehouse);
        User newUser = userRepository.save(user);
        return LoginResponse.builder()
                .id(newUser.getId())
                .token("")
                .user(userMapper.toShortResponse(newUser)) // Mapper que ya configuramos
                .employee(employeeDB)
                .warehouse(warehouse)
                .build();
    }

    private User createUser(User user) {
        String rawPassword = PasswordUtils.generateRandomPassword(12);
        String hashedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(hashedPassword);
        user.setPasswordInit(rawPassword);
        return this.userRepository.save(user);
    }

    private Employee createEmployee(UserStaffCreateRequest request, User user, Warehouse warehouse) {
        PositionType positionType = positionTypeService.findById(request.getPositionTypeId());
        Employee employee = Employee.builder()
                .warehouseId(warehouse.getId())
                .positionType(positionType)
                .user(user)
                .build();
        return this.employeeRepository.save(employee);
    }

    @Transactional
    public User updatePassword(Long id, String newPassword) {
        // 1. Buscar usuario
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordInit(null);

        return userRepository.save(user);
    }
}
