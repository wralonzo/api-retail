package com.wralonzo.detail_shop.modules.customers.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserAuthDto;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserShortResponse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role; // Asegúrate de importar Role
import com.wralonzo.detail_shop.modules.customers.domain.dto.client.ClientResponse;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.profile.ProfileResponse;

// unmappedTargetPolicy = ReportingPolicy.IGNORE quita los warnings de "provide", "notes", etc.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

  @Mapping(target = "id", source = "client.id")
  @Mapping(target = "code", source = "client.code")
  @Mapping(target = "birthDate", source = "profile.birthDate")
  @Mapping(target = "profile", source = "profile")
  @Mapping(target = "user", source = "authData")
  ClientResponse toResponse(Client client, Profile profile, UserAuthDto authData);

  @Mapping(target = "id", source = "client.id")
  @Mapping(target = "code", source = "client.code")
  @Mapping(target = "profile", source = "profile")
  @Mapping(target = "birthDate", source = "profile.birthDate")
  @Mapping(target = "user", source = "user")
  ClientResponse toResponse(Client client, Profile profile, User user);

  ProfileResponse toProfileResponse(Profile profile);

  // AQUÍ ESTÁ EL TRUCO: Definimos explícitamente el mapeo de roles para User y
  // UserAuthDto
  @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))")
  UserShortResponse toUserShortResponse(User user);

  @Mapping(target = "roles", expression = "java(mapAuthDtoRoles(authData.getRoles()))")
  UserShortResponse toUserShortResponse(UserAuthDto authData);

  // Métodos de ayuda para convertir colecciones de Roles a Strings
  default List<String> mapRoles(Set<Role> roles) {
    if (roles == null)
      return List.of();
    return roles.stream()
        .map(Role::getName)
        .collect(Collectors.toList());
  }

  default List<String> mapAuthDtoRoles(List<String> roles) {
    return roles; // Si ya es una lista de strings en el DTO, solo la retornamos
  }
}