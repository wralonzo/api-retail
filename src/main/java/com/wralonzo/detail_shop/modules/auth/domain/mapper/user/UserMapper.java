package com.wralonzo.detail_shop.modules.auth.domain.mapper.user;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.core.GrantedAuthority;

import com.wralonzo.detail_shop.modules.auth.domain.dtos.user.UserShortResponse;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "fullName", source = "profile.fullName")
  @Mapping(target = "avatar", source = "profile.avatar")
  @Mapping(target = "address", source = "profile.address")
  @Mapping(target = "phone", source = "profile.phone")
  @Mapping(target = "email", source = "profile.email")
  @Mapping(target = "birthDate", source = "profile.birthDate")
  // Mapeo de roles/authorities
  @Mapping(target = "roles", expression = "java(mapAuthorities(user))")
  UserShortResponse toShortResponse(User user);

  default List<String> mapAuthorities(User user) {
    if (user == null || user.getAuthorities() == null)
      return List.of();
    return user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());
  }
}