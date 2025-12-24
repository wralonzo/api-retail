package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.projections.WarehouseProjection;
import com.wralonzo.detail_shop.application.repositories.WarehouseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    public List<WarehouseProjection> getAll(){
        return this.warehouseRepository.findAllProjectedBy();
    }
}
