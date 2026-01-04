package com.wralonzo.detail_shop.application.services;

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

import com.wralonzo.detail_shop.application.repositories.UserRepository;
import com.wralonzo.detail_shop.configuration.exception.ResourceUnauthorizedException;
import com.wralonzo.detail_shop.domain.dto.auth.GoogleRequest;
import com.wralonzo.detail_shop.domain.dto.auth.LoginResponse;
import com.wralonzo.detail_shop.domain.entities.Client;
import com.wralonzo.detail_shop.domain.entities.User;
import com.wralonzo.detail_shop.domain.enums.ClientType;
import com.wralonzo.detail_shop.domain.enums.ProviderRegister;
import com.wralonzo.detail_shop.infrastructure.utils.PasswordUtils;
import com.wralonzo.detail_shop.security.jwt.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

  private final UserRepository userRepository;
  private final JwtUtil jwtUtil;
  private final UserCreationService userCreationService;
  private final PasswordEncoder passwordEncoder;
  private final RoleService roleService;

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

    String token = jwtUtil.generateToken(user);
    return userCreationService.converUser(user, token);
  }

  // --- MÉTODOS DE APOYO PARA LIMPIEZA DE CÓDIGO ---
  private User updateExistingUser(User user, String email, String name, String avatarUrl) {
    user.setUsername(email);
    user.setFullName(name);
    user.setAvatar(avatarUrl);
    user.setLastLoginAt(LocalDateTime.now());

    if (user.getClient() != null) {
      user.getClient().setName(name);
      user.getClient().setEmail(email);
    }
    return userRepository.save(user);
  }

  private User linkGoogleToExistingUser(User user, String googleId, String name, String avatarUrl) {
    user.setProvider(ProviderRegister.GOOGLE);
    user.setProviderId(googleId);
    user.setAvatar(avatarUrl);
    user.setLastLoginAt(LocalDateTime.now());

    if (user.getClient() != null) {
      user.getClient().setName(name);
      user.getClient().setEmail(user.getUsername());
      user.getClient().setUpdatedAt(LocalDateTime.now());
    }
    return userRepository.save(user);
  }

  private User createNewUserAndClient(String email, String name, String googleId, String avatarUrl) {
    // 1. Solo creas el objeto Client (SIN guardar en repo)
    Client client = Client.builder()
        .name(name)
        .email(email)
        .clientType(ClientType.REGULAR)
        .code("G-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
        .build();

    List<String> roles = List.of("ROLE_CLIENTE");

    // 2. Creas el User y le asignas el cliente
    String rawPassword = PasswordUtils.generateRandomPassword(12);
    String hashedPassword = passwordEncoder.encode(rawPassword);
    User newUser = User.builder()
        .username(email)
        .roles(this.roleService.getRolesFromRequest(roles))
        .fullName(name)
        .password(hashedPassword)
        .passwordInit(rawPassword)
        .provider(ProviderRegister.GOOGLE)
        .providerId(googleId)
        .enabled(true)
        .client(client) // El cascade se encarga de guardar este cliente
        .avatar(avatarUrl)
        .lastLoginAt(LocalDateTime.now())
        .build();

    // 3. Un solo save para todo el grafo de objetos
    return userRepository.save(newUser);
  }
}