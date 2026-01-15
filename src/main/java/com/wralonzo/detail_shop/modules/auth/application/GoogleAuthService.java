package com.wralonzo.detail_shop.modules.auth.application;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

import com.wralonzo.detail_shop.configuration.exception.ResourceUnauthorizedException;
import com.wralonzo.detail_shop.infrastructure.utils.PasswordUtils;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.auth.GoogleRequest;
import com.wralonzo.detail_shop.modules.auth.domain.mapper.records.LoginResponse;
import com.wralonzo.detail_shop.modules.auth.domain.enums.ProviderRegister;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.modules.customers.domain.enums.ClientType;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.auth.domain.mapper.user.UserMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

  private final UserRepository userRepository;
  private final UserCreationService userCreationService;
  private final PasswordEncoder passwordEncoder;
  private final RoleService roleService;
  private final UserMapper userMapper;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String googleClientId;

  public LoginResponse authenticateWithGoogle(GoogleRequest request) throws GeneralSecurityException, IOException {
    GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
        .setAudience(Collections.singletonList(googleClientId))
        .setAcceptableTimeSkewSeconds(60)
        .build();

    GoogleIdToken idToken = verifier.verify(request.getIdToken());

    if (idToken == null) {
      throw new ResourceUnauthorizedException("El token de Google no pudo ser verificado.");
    }

    Payload payload = idToken.getPayload();
    String email = payload.getEmail();
    String name = (String) payload.get("name");
    String googleId = payload.getSubject();
    String avatarUrl = (String) payload.get("picture");

    User user = userRepository.findByProviderAndProviderId(ProviderRegister.GOOGLE, googleId)
        .map(existingUser -> updateExistingUser(existingUser, email, name, avatarUrl))
        .orElseGet(() -> userRepository.findByUsername(email)
            .map(manualUser -> linkGoogleToExistingUser(manualUser, googleId, name, avatarUrl))
            .orElseGet(() -> createNewUserAndClient(email, name, googleId, avatarUrl)));
    return userMapper.toLoginResponse(user, "");
  }

  // --- MÉTODOS DE APOYO PARA LIMPIEZA DE CÓDIGO ---
  private User updateExistingUser(User user, String email, String name, String avatarUrl) {
    user.setUsername(email);

    if (user.getProfile() != null) {
      user.getProfile().setFullName(name);
      user.getProfile().setAvatar(avatarUrl);
      user.getProfile().setUpdateAt(LocalDateTime.now());
      user.setLastLoginAt(LocalDateTime.now());
    }
    return userRepository.save(user);
  }

  private User linkGoogleToExistingUser(User user, String googleId, String name, String avatarUrl) {
    user.setProvider(ProviderRegister.GOOGLE);
    user.setProviderId(googleId);
    user.getProfile().setAvatar(avatarUrl);
    user.setLastLoginAt(LocalDateTime.now());
    user.setUsername(user.getUsername());

    if (user.getProfile() != null) {
      user.getProfile().setFullName(name);
      user.getProfile().setEmail(user.getUsername());
      user.getProfile().setUpdateAt(LocalDateTime.now());
    }
    return userRepository.save(user);
  }

  private User createNewUserAndClient(String email, String name, String googleId, String avatarUrl) {
    // 1. buscar cliente
    List<String> roles = List.of("ROLE_CLIENTE");

    // 2. Creas el User
    String rawPassword = PasswordUtils.generateRandomPassword(12);
    String hashedPassword = passwordEncoder.encode(rawPassword);
    User user = User.builder()
        .username(email)
        .roles(this.roleService.getRolesFromRequest(roles))
        .password(hashedPassword)
        .passwordInit(rawPassword)
        .provider(ProviderRegister.GOOGLE)
        .providerId(googleId)
        .enabled(true)
        .lastLoginAt(LocalDateTime.now())
        .build();

    // 3. crea profile
    user.getProfile().setFullName(name);
    user.getProfile().setEmail(email);
    user.getProfile().setAvatar(avatarUrl);

    // 4 crea usuario

    User newUser = userRepository.save(user);

    // 5. Un solo save para todo el grafo de objetos
    Client client = Client.builder()
        .userId(newUser.getId())
        .profileId(newUser.getProfile().getId())
        .active(true)
        .clientType(ClientType.REGULAR)
        .code("G-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
        .build();
    this.userCreationService.SaveClient(client);
    return newUser;
  }
}