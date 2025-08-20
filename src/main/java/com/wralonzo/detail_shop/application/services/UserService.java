package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.UserRepository;
import com.wralonzo.detail_shop.domain.dto.auth.LoginRequest;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.entities.User;
import com.wralonzo.detail_shop.infrastructure.exceptions.ResourceConflictException;
import com.wralonzo.detail_shop.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;


    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       UserDetailsService userDetailsService ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    public User createUser(User user) {
        // Lógica de validación
        final Optional<User> userExist = this.userRepository.findByUsername(user.getUsername());
        if(userExist.isEmpty()){
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
            return this.userRepository.save(user);
        }
        throw new ResourceConflictException("El usuario con email " + user.getUsername() + " ya existe.");
    }

    public LoginResponse login(LoginRequest request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Carga los detalles del usuario (ya es el objeto User completo)
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        // 3. Verifica si el objeto UserDetails es una instancia de tu clase User
        if (userDetails instanceof User user) {
            // 4. Genera el token
            final String jwt = jwtUtil.generateToken(userDetails);

            // 5. Construye la respuesta con el objeto User obtenido
            LoginResponse response = LoginResponse.builder()
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

            return response;

        } else {
            // En caso de que el usuario no se encuentre (aunque no debería pasar después de la autenticación)
            throw new ResourceConflictException("Credenciales incorrectas");
        }
    }

    public Optional<User>  findUserByEmail(String email){
       return this.userRepository.findByUsername(email);
    }
}
