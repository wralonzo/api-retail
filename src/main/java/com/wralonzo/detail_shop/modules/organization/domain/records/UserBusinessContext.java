package com.wralonzo.detail_shop.modules.organization.domain.records;

import java.util.List;

public record UserBusinessContext(
    Long companyId,
    List<Long> warehouseIds,
    boolean isSuperAdmin) {
}