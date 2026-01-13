package com.wralonzo.detail_shop.modules.customers.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserAuthDto;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserShortResponse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Profile;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.customers.domain.dto.client.ClientResponse;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.auth.domain.dtos.profile.ProfileResponse;

@Mapper(componentModel = "spring")
public interface ClientMapper {

  // CASO 1: Para searchFullClients (Usa DTO de búsqueda masiva)
  @Mapping(target = "id", source = "client.id")
  @Mapping(target = "code", source = "client.clientCode")
  @Mapping(target = "profile", source = "profile")
  @Mapping(target = "user", source = "authData")
  ClientResponse toResponse(Client client, Profile profile, UserAuthDto authData);

  // CASO 2: Para update y create (Usa Entidad User directamente)
  @Mapping(target = "id", source = "client.id")
  @Mapping(target = "code", source = "client.clientCode")
  @Mapping(target = "profile", source = "profile")
  @Mapping(target = "user", source = "user")
  ClientResponse toResponse(Client client, Profile profile, User user);

  // Métodos de apoyo para convertir los objetos internos
  ProfileResponse toProfileResponse(Profile profile);

  UserShortResponse toUserShortResponse(User user);

  UserShortResponse toUserShortResponse(UserAuthDto authData);
}