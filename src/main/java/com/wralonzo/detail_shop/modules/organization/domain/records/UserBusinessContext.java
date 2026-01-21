package com.wralonzo.detail_shop.modules.organization.domain.records;

import java.util.List;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.entities.User;

public record UserBusinessContext(
        Long companyId,
        List<Long> warehouseIds,
        boolean isSuperAdmin,
        User user,
        Long branchId) {
}