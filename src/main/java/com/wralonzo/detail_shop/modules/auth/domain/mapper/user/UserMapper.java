package com.wralonzo.detail_shop.modules.auth.domain.mapper.user;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Employee;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;
import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.Role;
import com.wralonzo.detail_shop.modules.auth.domain.mapper.records.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {

  // CAMBIO AQUÍ: Especificamos que para el target "user", use tanto la entidad
  // como el token
  @Mapping(target = "profile", source = "entity")
  @Mapping(target = "user", expression = "java(toUserBaseResponse(entity, token))")
  @Mapping(target = "employee", source = "entity.employee")
  LoginResponse toLoginResponse(User entity, String token);

  @Mapping(target = "id", source = "user.profile.id")
  @Mapping(target = "username", source = "user.username")
  @Mapping(target = "provide", source = "user.provider")
  @Mapping(target = "passwordInit", source = "user.passwordInit")
  @Mapping(target = "fullName", source = "user.profile.fullName")
  @Mapping(target = "avatar", source = "user.profile.avatar")
  @Mapping(target = "address", source = "user.profile.address")
  @Mapping(target = "phone", source = "user.profile.phone")
  @Mapping(target = "email", source = "user.profile.email")
  @Mapping(target = "birthDate", source = "user.profile.birthDate")
  ProfileResponse toProfileResponse(User user);

  @Mapping(target = "id", source = "user.id")
  @Mapping(target = "enabled", source = "user.enabled")
  @Mapping(target = "provider", source = "user.provider")
  @Mapping(target = "token", source = "token") // Aquí ya se mapeará correctamente
  @Mapping(target = "roles", source = "user.roles", qualifiedByName = "toRolesList")
  UserBaseResponse toUserBaseResponse(User user, String token);

  @Mapping(target = "id", source = "employee.id")
  @Mapping(target = "warehouseId", source = "employee.warehouseId")
  @Mapping(target = "positionName", source = "employee.positionType.name")
  @Mapping(target = "positionId", source = "employee.positionType.id")
  EmployeeShortResponse toEmployeeShortResponse(Employee employee);

  @Named("toRolesList")
  default List<String> mapRoles(Set<Role> roles) {
    if (roles == null)
      return List.of();
    return roles.stream()
        .map(Role::getName)
        .collect(Collectors.toList());
  }
}