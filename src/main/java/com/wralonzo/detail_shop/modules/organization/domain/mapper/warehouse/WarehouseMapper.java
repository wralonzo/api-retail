package com.wralonzo.detail_shop.modules.organization.domain.mapper.warehouse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.wralonzo.detail_shop.modules.organization.domain.dtos.warehouse.WarehouseResponse;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
  @Mapping(target = "branchName", source = "branch.name")
  WarehouseResponse toWarehouseResponse(Warehouse warehouse);
}