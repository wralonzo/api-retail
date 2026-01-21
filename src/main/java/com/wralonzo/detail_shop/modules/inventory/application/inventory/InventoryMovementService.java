package com.wralonzo.detail_shop.modules.inventory.application.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryMovementRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.MovementType;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Inventory;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.InventoryMovement;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryMovementRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryMovementService {
  private final InventoryRepository inventoryRepository;
  private final ProductRepository productRepository;
  private final InventoryMovementRepository inventoryMovementRepository;
  private final WarehouseService warehouseService;
  // --- PUNTO DE ENTRADA PARA EL CONTROLADOR ---

  @Transactional
  public void processGenericMovement(InventoryMovementRequest request) {

    // 1. Obtener o crear maestro de inventario
    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(request.getProductId(),
            request.getWarehouseId())
        .orElseGet(() -> inventoryRepository
            .save(createInitialInventory(request.getProductId(),
                request.getWarehouseId())));

    int before = inv.getQuantity();
    int after;

    // 2. Determinar lógica según el tipo
    MovementType tipo = MovementType.valueOf(request.getType().toUpperCase());

    if (tipo == MovementType.ENTRADA_COMPRA) {
      after = before + request.getQuantity();
    } else if (tipo == MovementType.SALIDA_VENTA) {
      if (before < request.getQuantity()) {
        new ResourceConflictException("Stock insuficiente para realizar la salida");
      }
      after = before - request.getQuantity();
    } else {
      // AJUSTE: El valor que viene es el stock real contado
      after = request.getQuantity();
    }

    // 3. Actualizar saldo maestro
    inv.setQuantity(after);
    inventoryRepository.save(inv);

    // 4. Registrar en Kardex (Auditoría)
    saveMovementDetail(inv, request, before, after, tipo);
  }

  public Inventory createInitialInventory(Long productId, Long warehouseId) {
    return Inventory.builder()
        .product(productRepository.getReferenceById(productId))
        .warehouseId(warehouseId)
        .quantity(0)
        .quantityReserved(0)
        .alertQuantity(5)
        .build();
  }

  @Transactional
  public void processSalesMovement(Long productId, Long warehouseId, int quantity, String reference) {
    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(productId, warehouseId)
        .orElseThrow(() -> new ResourceConflictException("Inventario no encontrado para el producto " + productId));

    int before = inv.getQuantity();
    if (before < quantity) {
      throw new ResourceConflictException(
          "Stock insuficiente para el producto " + productId + ". Disponible: " + before + ", Requerido: " + quantity);
    }

    int after = before - quantity;
    inv.setQuantity(after);
    inventoryRepository.save(inv);

    saveMovementDetail(inv, quantity, before, after, MovementType.SALIDA_VENTA, reference);
  }

  // --- HELPER DE AUDITORÍA (PÚBLICO para uso interno) ---

  public void saveMovementDetail(Inventory inv, InventoryMovementRequest request,
      int before, int after, MovementType tipo) {
    saveMovementDetail(inv, request.getQuantity(), before, after, tipo, request.getReference());
  }

  public void saveMovementDetail(Inventory inv, int quantity, int before, int after, MovementType tipo,
      String reference) {
    UserBusinessContext context = warehouseService.getUserBusinessContext();

    InventoryMovement detail = InventoryMovement.builder()
        .product(inv.getProduct())
        .warehouseId(inv.getWarehouseId())
        .movementType(tipo)
        .quantity(quantity)
        .previousStock(before) // FIXED: Before
        .currentStock(after) // FIXED: After
        .reference(reference)
        .userId(context.user().getId())
        .build();

    inventoryMovementRepository.save(detail);
  }

  @Transactional
  public void reserveStock(Long productId, Long warehouseId, int quantity) {
    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(productId, warehouseId)
        .orElseThrow(() -> new ResourceConflictException("Inventario no encontrado para el producto " + productId));

    if (inv.getQuantityFree() < quantity) {
      throw new ResourceConflictException("Stock insuficiente para reservar. Disponible: " + inv.getQuantityFree());
    }

    inv.setQuantityReserved(inv.getQuantityReserved() + quantity);
    inventoryRepository.save(inv);
  }

  @Transactional
  public void releaseReservedStock(Long productId, Long warehouseId, int quantity) {
    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(productId, warehouseId)
        .orElseThrow(() -> new ResourceConflictException("Inventario no encontrado para el producto " + productId));

    inv.setQuantityReserved(Math.max(0, inv.getQuantityReserved() - quantity));
    inventoryRepository.save(inv);
  }

  @Transactional
  public void confirmReservedStock(Long productId, Long warehouseId, int quantity, String reference) {
    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(productId, warehouseId)
        .orElseThrow(() -> new ResourceConflictException("Inventario no encontrado para el producto " + productId));

    // 1. Liberar la reserva
    inv.setQuantityReserved(Math.max(0, inv.getQuantityReserved() - quantity));

    // 2. Descontar el stock real
    int before = inv.getQuantity();
    if (before < quantity) {
      throw new ResourceConflictException("Inconsistencia de inventario. Stock físico menor al reservado confirmado.");
    }
    int after = before - quantity;
    inv.setQuantity(after);

    inventoryRepository.save(inv);

    // 3. Registrar Movimiento
    saveMovementDetail(inv, quantity, before, after, MovementType.SALIDA_VENTA, reference);
  }
}
