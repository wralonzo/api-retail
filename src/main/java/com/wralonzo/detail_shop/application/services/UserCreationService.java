package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.*;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.domain.dto.user.UserClient;
import com.wralonzo.detail_shop.domain.dto.user.UserRequest;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.entities.Employee;
import com.wralonzo.detail_shop.domain.entities.User;
import com.wralonzo.detail_shop.infrastructure.utils.PasswordUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Builder
public class UserCreationService {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final RoleService roleService;
    private final WarehouseRepository warehouseRepository;
    private final PositionTypeRepository positionTypeRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public User SaveClient(UserClient request) {
        if (this.clientRepository.existsByEmail(request.getClient().getEmail())) {
            throw new ResourceConflictException("Cliente " + request.getClient().getEmail() + " ya registrado.");
        }
        this.userExist(request.getClient().getEmail());
        Client client = this.clientRepository.save(request.getClient());
        User user = new User(
                null,
                request.getClient().getName(),
                request.getClient().getEmail(),
                request.getClient().getPhone(),
                request.getClient().getAddress(),
                "",
                "",
                "",
                true,
                null,
                null,
                null,
                null,
                client,
                this.roleService.getRolesFromRequest(request.getRoles())
        );
        return this.createUser(user);
    }

    private void userExist(String user) {
        final Optional<User> userFound = this.userRepository.findByUsername(user);
        if (userFound.isPresent()) {
            throw new ResourceConflictException("Email " + user + " existente.");
        }
    }

    @Transactional
    public User SaveUser(UserRequest request) {
        this.userExist(request.getUser().getUsername());
        Employee employeeDB = this.createEmployee(request);
        request.getUser().setEmployee(employeeDB);
        request.getUser().setRoles(roleService.getRolesFromRequest(request.getRoles()));
        return this.createUser(request.getUser());
    }

    private User createUser(User user) {
        String rawPassword = PasswordUtils.generateRandomPassword(12);
        String hashedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(hashedPassword);
        user.setPasswordInit(rawPassword);
        return this.userRepository.save(user);
    }

    private Employee createEmployee(UserRequest request) {
        Employee employee = Employee.builder()
                .warehouse(warehouseRepository.findById(request.getWarehouse())
                        .orElseThrow(() -> new ResourceNotFoundException("Warehouse no encontrado")))
                .positionType(positionTypeRepository.findById(request.getPositionType())
                        .orElseThrow(() -> new ResourceNotFoundException("Position no encontrado")))
                .build();
        return this.employeeRepository.save(employee);
    }

}
