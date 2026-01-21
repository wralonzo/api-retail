package com.wralonzo.detail_shop.modules.inventory.domain.jpa.specs;

import org.springframework.data.jpa.domain.Specification;

import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Inventory;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;

public class InventorySpecifications {

    public static Specification<Inventory> filterByContext(UserBusinessContext context) {
        return (root, query, cb) -> {
            if (context.isSuperAdmin()) return null; // El jefe ve todo el stock de todos lados

            // Si es Admin/Vendedor, solo ve el stock de los almacenes de su compañía
            return root.get("warehouseId").in(context.warehouseIds());
        };
    }

    public static Specification<Inventory> searchByTerm(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) return null;
            String pattern = "%" + term.toLowerCase() + "%";
            
            // Join con Product para buscar por nombre o código
            return cb.or(
                cb.like(cb.lower(root.get("product").get("name")), pattern),
                cb.like(cb.lower(root.get("product").get("sku")), pattern)
            );
        };
    }
}