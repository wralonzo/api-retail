package com.wralonzo.detail_shop.modules.auth.application;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.infrastructure.utils.PasswordUtils;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserCreateRequest;
import com.wralonzo.detail_shop.modules.auth.domain.enums.ProviderRegister;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.RoleRepository;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Service
@AllArgsConstructor
@Builder
public class UserClientService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleService roleService;

  public User createClient(UserCreateRequest request) {
    this.userRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new ResourceConflictException(
            "Ya existe el usuario: " + request.getUsername() + ", recupera tus credenciales"));
    // 2. Crear la entidad Profile
    Profile profile = Profile.builder()
        .fullName(request.getFullName())
        .phone(request.getPhone())
        .address(request.getAddress())
        .avatar(request.getAvatar())
        // El email p
        // odrías guardarlo en el perfil o en el usuario
        .build();

    // 3. Buscar los roles (o asignar uno por defecto)
    List<String> roleNames = List.of("ROLE_CLIENT");

    // Buscamos las entidades reales en la DB
    Set<Role> userRoles = roleService.getRolesFromRequest(roleNames);

    // 4. Crear la entidad User
    User user = User.builder()
        .username(request.getUsername())
        .password(passwordEncoder.encode(request.getPassword())) // ¡Cifrado siempre!
        .passwordInit(request.getPassword()) // Guardar temporal si es necesario para el primer login
        .profile(profile) // Vinculación OneToOne
        .roles(userRoles)
        .enabled(true)
        .provider(ProviderRegister.LOCAL) // Asumiendo registro manual
        .build();

    // 5. Guardar y retornar el ID
    // Debido a CascadeType.ALL en la entidad User, esto guarda automáticamente el
    // Profile
    User savedUser = userRepository.save(user);
    return savedUser;
  }

  public void updateClient(User user, Long id) {
    User userFind = this.userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

    if (user.getUsername() != null)
      userFind.setUsername(user.getUsername());
    if (user.getProfile().getPhone() != null)
      userFind.getProfile().setPhone(user.getProfile().getPhone());
    if (user.getProfile().getAddress() != null)
      userFind.getProfile().setAddress(user.getProfile().getAddress());
    if (user.getProfile().getAvatar() != null)
      userFind.getProfile().setAvatar(user.getProfile().getAvatar());

    user.setUpdateAt(LocalDateTime.now());
    user.getProfile().setUpdateAt(LocalDateTime.now());
    userRepository.save(user);
  }

  public User searchById(Long id) {
    User user = userRepository.findById(id).orElse(null);
    return user;
  }

  public User createUser(String username) {
    User userExist = userRepository.findByUsername(username).orElse(null);
    if (userExist != null) {
      userExist.setUsername((int) (Math.random() * 10000) + "_" + username);
    }
    List<String> rolesNames = List.of("ROLE_CLIENT");
    String rawPassword = PasswordUtils.generateRandomPassword(12);
    String hashedPassword = passwordEncoder.encode(rawPassword);
    Set<Role> userRoles = roleService.getRolesFromRequest(rolesNames);

    User user = User.builder()
        .password(hashedPassword)
        .passwordInit(rawPassword)
        .provider(ProviderRegister.LOCAL)
        .enabled(true)
        .roles(userRoles)
        .build();
    return userRepository.save(user);
  }

  public void deleteUser(Long id) {
    User user = this.userRepository.findById(id).orElse(null);
    if (user != null) {
      user.setEnabled(false);
      user.setUpdateAt(LocalDateTime.now());
      user.setDeletedAt(LocalDateTime.now());
      userRepository.save(user);
      return;
    }
    return;
  }
}
