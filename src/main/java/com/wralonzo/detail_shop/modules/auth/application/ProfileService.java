package com.wralonzo.detail_shop.modules.auth.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.coyote.BadRequestException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.ProfileRepository;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.specs.ProfileSpecifications;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserCreateRequest;

@Service
@AllArgsConstructor
@Builder
public class ProfileService {
  private final ProfileRepository profileRepository;

  @Transactional
  public Profile save(UserCreateRequest request) {
    emailExist(request.getEmail());
    Profile profile = Profile.builder()
        .fullName(request.getFullName())
        .phone(request.getPhone())
        .address(request.getAddress())
        .avatar(request.getAvatar())
        .email((request.getEmail()))
        .build();
    return profileRepository.save(profile);
  }

  public Profile saveUser(Profile profile) {
    return profileRepository.save(profile);
  }

  public Profile update(Long id, UserCreateRequest request) {
    Profile profile = profileRepository.findById(id)
        .orElseThrow(() -> new ResourceConflictException("Recurso no encontrado"));
    if (request.getEmail() != null
        && !request.getEmail().equalsIgnoreCase(profile.getEmail())) {
      this.emailExist(request.getEmail());
    }

    if (request.getFullName() != null) {
      profile.setFullName(request.getFullName());
    }

    if (request.getPhone() != null) {
      profile.setPhone(request.getPhone());
    }

    if (request.getAddress() != null) {
      profile.setAddress(request.getAddress());
    }

    if (request.getAvatar() != null) {
      profile.setAvatar(request.getAvatar());
    }

    profileRepository.save(profile);

    return profile;
  }

  public void delete(Long id) {
    Profile user = this.profileRepository.findById(id).orElse(null);
    if (user != null) {
      user.setUpdateAt(LocalDateTime.now());
      user.setDeletedAt(LocalDateTime.now());
      profileRepository.save(user);
      return;
    }
    return;
  }

  @Transactional(readOnly = true)
  public Map<Long, Profile> getProfilesMap(List<Long> profileIds) {
    // 1. Verificación de seguridad para evitar consultas vacías
    if (profileIds == null || profileIds.isEmpty()) {
      return Map.of();
    }

    // 2. Buscamos todos los perfiles en una sola consulta SQL (SELECT * FROM
    // profiles WHERE id IN (...))
    List<Profile> profiles = profileRepository.findAllById(profileIds);

    // 3. Convertimos la lista en un Mapa usando Streams
    return profiles.stream()
        .collect(Collectors.toMap(
            Profile::getId, // Clave: ID del perfil
            Function.identity(), // Valor: La entidad completa
            (existing, replacement) -> existing // Por si acaso hay duplicados (aunque el ID es único)
        ));
  }

  public Profile getById(Long id) {
    return this.profileRepository.findById(id).orElse(null);
  }

  public void emailExist(String email) {
    // Asumiendo que tienes este método en profileService o profileRepository
    if (this.profileRepository.existsByEmail(email)) {
      throw new ResourceConflictException("El correo " + email + " ya está registrado.");
    }
  }

  public void validateEmailForUpdate(Long currentUserId, String newEmail) {
    profileRepository.findByEmail(newEmail)
        .ifPresent(existingProfile -> {
          new BadRequestException(
              "El correo electrónico " + newEmail + " ya está registrado por otro usuario.");

        });
  }

  /**
   * Busca perfiles que coincidan con el término en fullName, email o phone
   * y retorna solo sus IDs.
   */
  @Transactional(readOnly = true)
  public List<Long> findIdsByTerm(String term) {
    if (term == null || term.isBlank()) {
      return List.of();
    }

    // Usamos especificaciones para una búsqueda flexible
    Specification<Profile> spec = Specification
        .where(ProfileSpecifications.isNotDeleted())
        .and(ProfileSpecifications.containsTerm(term));

    // Obtenemos las entidades y extraemos los IDs
    return profileRepository.findAll(spec)
        .stream()
        .map(Profile::getId)
        .collect(Collectors.toList());
  }
}
