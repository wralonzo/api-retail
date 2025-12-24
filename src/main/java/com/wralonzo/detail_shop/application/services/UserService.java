package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.EmployeeRepository;
import com.wralonzo.detail_shop.application.repositories.PositionTypeRepository;
import com.wralonzo.detail_shop.application.repositories.UserRepository;
import com.wralonzo.detail_shop.application.repositories.WarehouseRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.domain.dto.auth.LoginRequest;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.dto.user.UserClient;
import com.wralonzo.detail_shop.domain.dto.user.UserRequest;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.entities.Employee;
import com.wralonzo.detail_shop.domain.entities.User;
import com.wralonzo.detail_shop.infrastructure.utils.PasswordUtils;
import com.wralonzo.detail_shop.security.jwt.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final WarehouseRepository warehouseRepository;
    private final PositionTypeRepository positionTypeRepository;
    private final ClientService clientService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserDetailsService userDetailsService,
            EmployeeRepository employeeRepository,
            WarehouseRepository warehouseRepository,
            PositionTypeRepository positionTypeRepository, ClientService clientService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.employeeRepository = employeeRepository;
        this.warehouseRepository = warehouseRepository;
        this.positionTypeRepository = positionTypeRepository;
        this.clientService = clientService;
    }

    @Transactional
    public User SaveUser(UserRequest request) {
        this.userExist(request.getUser().getUsername());
        Employee employeeDB = this.createEmployee(request);
        request.getUser().setEmployee(employeeDB);
        return this.createUser(request.getUser());
    }


    @Transactional
    public User SaveClient(UserClient request) {
        this.userExist(request.getClient().getEmail());
        if (this.clientService.getClientByEmail(request.getClient().getEmail())) {
            throw new ResourceConflictException("Cliente " + request.getClient().getEmail() + " ya registrado.");
        }
        Client client = this.clientService.createWithUser(request.getClient());
        User user = new User(
                null,
                request.getClient().getName(),
                request.getClient().getEmail(),
                request.getClient().getPhone(),
                request.getClient().getAddress(),
                "",
                "",
                null,
                null,
                null,
                null,
                client
        );
        return this.createUser(user);
    }

    public LoginResponse login(LoginRequest request) {
        // 1. Autenticar (Si falla, Spring lanza BadCredentialsException automáticamente)
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Obtener el usuario directamente del objeto de autenticación
        // Esto evita el problema de casteo si tu UserDetailsService devuelve tu Entidad
        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            final String jwt = jwtUtil.generateToken(user);

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
                    .build();
        }

        // Si llegas aquí, es que tu UserDetailsService devuelve algo que no es tu Entidad User
        throw new ResourceConflictException("El sistema no pudo recuperar el perfil del usuario.");
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

    public Optional<User> findUserByEmail(String email) {
        return this.userRepository.findByUsername(email);
    }

    private User createUser(User user) {
        String rawPassword = PasswordUtils.generateRandomPassword(12);
        String hashedPassword = passwordEncoder.encode(rawPassword);

        user.setPassword(hashedPassword);
        return this.userRepository.save(user);
    }

    private Optional<User> userExist(String user){
        final Optional<User> userFound = this.userRepository.findByUsername(user);
        if (userFound.isPresent()) {
            throw new ResourceConflictException("Email " + user + " ya registrado.");
        }
        return  userFound;
    }
}
