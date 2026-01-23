package com.wralonzo.detail_shop.modules.auth.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.infrastructure.utils.PasswordUtils;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserCreateRequest;
import com.wralonzo.detail_shop.modules.auth.domain.enums.ProviderRegister;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
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

  @Transactional
  public User createClient(UserCreateRequest request) {
    // Validar que NO exista el usuario antes de crearlo
    this.userRepository.findByUsername(request.getUsername())
        .ifPresent(existingUser -> {
          throw new ResourceConflictException(
              "Ya existe el usuario: " + request.getUsername() + ", recupera tus credenciales");
        });
    // 2. Crear la entidad Profile
    Profile profile = Profile.builder()
        .fullName(request.getFullName())
        .phone(request.getPhone())
        .address(request.getAddress())
        .avatar(request.getAvatar())
        .email(request.getEmail())
        .birthDate(request.getBirthDate())
        // El email p
        // odrías guardarlo en el perfil o en el usuario
        .build();

    // 3. Buscar los roles (o asignar uno por defecto)
    List<String> roleNames = List.of("ROLE_CLIENTE");

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

  public User updateClient(UserCreateRequest request, Long id) {
    User userFind = this.userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

    if (request.getUsername() != null)
      userFind.setUsername(request.getUsername());
    if (request.getPhone() != null)
      userFind.getProfile().setPhone(request.getPhone());
    if (request.getAddress() != null)
      userFind.getProfile().setAddress(request.getAddress());
    if (request.getAvatar() != null)
      userFind.getProfile().setAvatar(request.getAvatar());
    if (request.getEmail() != null)
      userFind.getProfile().setEmail(request.getEmail());
    if (request.getFullName() != null)
      userFind.getProfile().setFullName(request.getFullName());
    if (request.getBirthDate() != null)
      userFind.getProfile().setBirthDate(request.getBirthDate());

    userFind.setUpdateAt(LocalDateTime.now());
    userFind.getProfile().setUpdateAt(LocalDateTime.now());
    return userRepository.save(userFind);
  }

  public User searchById(Long id) {
    User user = userRepository.findById(id).orElse(null);
    return user;
  }

  public User createUser(Profile profile) {
    User userExist = userRepository.findByUsername(profile.getEmail()).orElse(null);
    if (userExist != null) {
      profile.setEmail((int) (Math.random() * 10000) + "_" + profile.getEmail());
    }
    List<String> rolesNames = List.of("ROLE_CLIENTE");
    String rawPassword = PasswordUtils.generateRandomPassword(12);
    String hashedPassword = passwordEncoder.encode(rawPassword);
    Set<Role> userRoles = roleService.getRolesFromRequest(rolesNames);

    User user = User.builder()
        .username(profile.getEmail())
        .profile(profile)
        .password(hashedPassword)
        .passwordInit(rawPassword)
        .provider(ProviderRegister.LOCAL)
        .enabled(true)
        .roles(userRoles)
        .passwordLastChangedAt(LocalDateTime.now())
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
