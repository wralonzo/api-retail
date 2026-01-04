package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.*;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.domain.dto.auth.EmployeeShortResponse;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.dto.user.UserClient;
import com.wralonzo.detail_shop.domain.dto.user.UserRequest;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.entities.Employee;
import com.wralonzo.detail_shop.domain.entities.Role;
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
    private final RoleService roleService;
    private final WarehouseRepository warehouseRepository;
    private final PositionTypeRepository positionTypeRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public User SaveClient(UserClient request, boolean flagValidateCliente) {
        if (flagValidateCliente) {
            if (this.clientRepository.existsByEmail(request.getClient().getEmail())) {
                throw new ResourceConflictException("Cliente " + request.getClient().getEmail() + " ya registrado.");
            }
        }

        this.userExist(request.getClient().getEmail());

        String newCode = generateClientCode();
        request.getClient().setCode(newCode);
        Client client = this.clientRepository.save(request.getClient());
        User user = User.builder()
                .fullName(client.getName())
                .username(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .enabled(true)
                .client(client)
                .roles(this.roleService.getRolesFromRequest(request.getRoles()))
                .build();
        return this.createUser(user);
    }

    private void userExist(String user) {
        final Optional<User> userFound = this.userRepository.findByUsername(user);
        if (userFound.isPresent()) {
            throw new ResourceConflictException("Email " + user + " existente.");
        }
    }

    @Transactional
    public LoginResponse SaveUser(UserRequest request) {
        this.userExist(request.getUser().getUsername());
        Employee employeeDB = this.createEmployee(request);
        request.getUser().setEmployee(employeeDB);
        request.getUser().setRoles(roleService.getRolesFromRequest(request.getRoles()));
        User createdUser = this.createUser(request.getUser());
        return converUser(createdUser, null);
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

    public String generateClientCode() {
        String prefix = "CLI-";
        int defaultStart = 1;

        // 1. Buscar el último código en la DB
        return clientRepository.findFirstByOrderByCodeDesc()
                .map(lastClient -> {
                    // 2. Extraer el número del código (ej: de "CLI-0005" obtener "0005")
                    String lastCode = lastClient.getCode();
                    String numericPart = lastCode.substring(prefix.length());

                    // 3. Incrementar el número
                    int nextNumber = Integer.parseInt(numericPart) + 1;

                    // 4. Formatear de nuevo con ceros a la izquierda (CLI-0006)
                    return prefix + String.format("%04d", nextNumber);
                })
                // Si no hay clientes, empezamos con el primero
                .orElse(prefix + String.format("%04d", defaultStart));
    }

    public LoginResponse converUser(User user, String jwt) {
        return LoginResponse.builder()
                .id(user.getId())
                .token(jwt)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .avatar(user.getAvatar())
                .createdAt(user.getCreatedAt())
                .updateAt(user.getUpdateAt())
                .deletedAt(user.getDeletedAt())
                .enabled(user.isEnabled())
                .passwordInit(user.getPasswordInit())
                .clientId(mapToClientResponse(user.getClient()))
                .provider(user.getProvider())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .toList())
                .employee(mapToEmployeeResponse(user.getEmployee()))
                .build();
    }

    private EmployeeShortResponse mapToEmployeeResponse(Employee emp) {
        if (emp == null)
            return null;

        return EmployeeShortResponse.builder()
                .id(emp.getId())
                // Mapeo de Warehouse
                .warehouseId(emp.getWarehouse() != null ? emp.getWarehouse().getId() : null)
                .warehouseName(emp.getWarehouse() != null ? emp.getWarehouse().getName() : null)
                // Mapeo de PositionType
                .positionId(emp.getPositionType() != null ? emp.getPositionType().getId() : null)
                .positionName(emp.getPositionType() != null ? emp.getPositionType().getName() : null)
                .build();
    }

    private Long mapToClientResponse(Client client) {
        if (client == null)
            return null;
        return client.getId();
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
