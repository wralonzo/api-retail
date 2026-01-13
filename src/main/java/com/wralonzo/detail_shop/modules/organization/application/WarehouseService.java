package com.wralonzo.detail_shop.modules.organization.application;

import com.wralonzo.detail_shop.modules.auth.domain.jpa.projections.WarehouseProjection;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.WarehouseRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    public List<WarehouseProjection> getAll() {
        return this.warehouseRepository.findAllProjectedBy();
    }

    public Warehouse getById(long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceConflictException("No existe el almacén"));
    }

    public Warehouse getByCode(String code) {
        return warehouseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceConflictException("No existe el almacén"));
    }

    public Map<Long, Warehouse> getWarehousesMap(List<Long> ids) {
        if (ids.isEmpty())
            return Map.of();
        return warehouseRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity()));
    }
}
