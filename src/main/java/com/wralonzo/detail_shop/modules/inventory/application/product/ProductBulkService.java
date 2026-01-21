package com.wralonzo.detail_shop.modules.inventory.application.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.modules.inventory.application.inventory.InventoryMovementService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.BulkInventoryRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Inventory;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductBranchConfigRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductBulkService {

  private final ProductBranchConfigRepository branchConfigRepository;
  private final ProductRepository productRepository;
  private final InventoryRepository inventoryRepository;
  private final InventoryMovementService inventoryMovementService;

  @Transactional
  public int bulkPriceAdjustment(Long warehouseId, Long categoryId, Double percentageIncrement) {
    // 1. Validar que el porcentaje no sea nulo para evitar errores matemáticos
    if (percentageIncrement == null)
      return 0;

    // 2. Convertir incremento (ej: 10% -> 1.10)
    Double factor = 1 + (percentageIncrement / 100);

    // 3. Ejecutar actualización masiva a través del repositorio personalizado
    // El repositorio debe extender de JpaRepository y ProductBranchCustomRepository
    int affectedRows = branchConfigRepository.bulkUpdatePrice(warehouseId, categoryId, factor);

    // 4. Auditoría Corporativa
    log.info("AJUSTE MASIVO: Sucursal ID: {} | Categoría ID: {} | Factor aplicado: {} | Registros afectados: {}",
        warehouseId, categoryId, factor, affectedRows);

    return affectedRows;
  }

  @Transactional
  public Map<String, Object> bulkInitialize(BulkInventoryRequest request) {
    // 1. Validar seguridad: ¿Tiene el usuario permiso sobre este almacén?
    // validateWarehouseAccess(request.getWarehouseId());

    // 2. Obtener lista de productos activos (filtrados por categoría si se
    // requiere)
    List<Product> products = (request.getCategoryId() != null)
        ? productRepository.findByCategoryIdAndActiveTrue(request.getCategoryId())
        : productRepository.findByActiveTrue();

    int createdCount = 0;
    int skippedCount = 0;

    // 3. Iterar y crear solo los que no existen
    for (Product product : products) {
      boolean exists = inventoryRepository.existsByProductIdAndWarehouseId(product.getId(),
          request.getWarehouseId());

      if (!exists) {
        // Usamos el helper que ya creamos
        Inventory newInventory = inventoryMovementService.createInitialInventory(product.getId(),
            request.getWarehouseId());
        inventoryRepository.save(newInventory);
        createdCount++;
      } else {
        skippedCount++;
      }
    }

    // 4. Retornar estadísticas de la operación
    Map<String, Object> stats = new HashMap<>();
    stats.put("totalProcessed", products.size());
    stats.put("recordsCreated", createdCount);
    stats.put("recordsSkipped", skippedCount);
    stats.put("warehouseId", request.getWarehouseId());
    stats.put("status", "SUCCESS");

    return stats;
  }
}