package com.wralonzo.detail_shop.modules.auth.application;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.profile.ProfileDto;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserAuthDto;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.repositories.UserRepository;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.specs.UserSpecifications;

import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Service
@AllArgsConstructor
@Builder
public class AuthService {
  private final UserRepository userRepository;
  private final EntityManager entityManager;

  public List<Long> findUserIdsByProfileOrUsername(String term) {
    if (term == null || term.isBlank())
      return Collections.emptyList();

    // 1. Creamos el CriteriaBuilder y la Query de tipo Long
    var cb = entityManager.getCriteriaBuilder();
    var query = cb.createQuery(Long.class);
    var root = query.from(User.class);

    // 2. Aplicamos la lógica de nuestra Specification manual
    var spec = Specification
        .where(UserSpecifications.isNotDeleted())
        .and(UserSpecifications.searchByTerm(term));

    // 3. Configuramos la query: SELECT id FROM ... WHERE ...
    query.select(root.get("id"));
    query.where(spec.toPredicate(root, query, cb));

    // 4. Ejecutamos y obtenemos solo la lista de números
    return entityManager.createQuery(query).getResultList();
  }

  /**
   * Obtiene un mapa de perfiles indexados por el ID de usuario.
   * Ideal para evitar el problema N+1 en listas paginadas.
   */
  public Map<Long, ProfileDto> getProfilesMapByUserIds(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Collections.emptyMap();
    }

    // 1. Buscamos los Usuarios por sus IDs.
    // Spring Data JPA ya tiene 'findAllById' que es ultra eficiente.
    List<User> users = userRepository.findAllById(userIds);

    // 2. Construimos el mapa: la llave es User::getId y el valor es el DTO del
    // perfil
    return users.stream()
        .filter(user -> user.getProfile() != null) // Evitamos NullPointerException
        .collect(Collectors.toMap(
            User::getId,
            user -> mapToProfileDTO(user.getProfile()),
            (existing, replacement) -> existing));
  }

  public User findById(long id) {
    return userRepository.findById(id).orElse(null);
  }

  public Map<Long, UserAuthDto> getUsersAuthData(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty())
      return Collections.emptyMap();

    return userRepository.findAllById(userIds).stream()
        .map(user -> UserAuthDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .fullName(user.getProfile().getFullName())
            .email(user.getProfile().getEmail())
            .build())
        .collect(Collectors.toMap(UserAuthDto::getId, Function.identity()));
  }

  private ProfileDto mapToProfileDTO(Profile profile) {
    return ProfileDto.builder()
        .fullName(profile.getFullName())
        .avatar(profile.getAvatar())
        .phone(profile.getPhone())
        .build();
  }
}
