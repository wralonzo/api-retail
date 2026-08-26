package com.wralonzo.detail_shop.modules.inventory.application.inventory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryMovementRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.MovementType;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.ProductType;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Inventory;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.InventoryMovement;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
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

  @Transactional
  public void processGenericMovement(InventoryMovementRequest request) {

    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
        .orElseGet(() -> inventoryRepository
            .save(createInitialInventory(request.getProductId(), request.getWarehouseId())));

    int before = inv.getQuantity();
    int after;

    MovementType tipo = MovementType.valueOf(request.getType().toUpperCase());

    if (tipo == MovementType.ENTRADA_COMPRA) {
      after = before + request.getQuantity();
    } else if (tipo == MovementType.SALIDA_VENTA) {
      if (before < request.getQuantity()) {
        throw new ResourceConflictException("Stock insuficiente para realizar la salida");
      }
      after = before - request.getQuantity();
    } else {
      after = request.getQuantity();
    }

    inv.setQuantity(after);
    inventoryRepository.save(inv);

    saveMovementDetail(inv, request, before, after, tipo);
  }

  public Inventory createInitialInventory(Long productId, Long warehouseId) {
    Inventory.InventoryBuilder builder = Inventory.builder()
        .warehouseId(warehouseId)
        .quantity(0)
        .quantityReserved(0)
        .alertQuantity(5);

    if (productId != null) {
      builder.product(productRepository.getReferenceById(productId));
    }

    return builder.build();
  }

  @Transactional
  public void processSalesMovement(Long productId, Long warehouseId, int quantity, String reference) {
    if (productId == null) return;

    Product product = productRepository.findById(productId).orElse(null);
    if (product != null && (Boolean.TRUE.equals(product.getIsService()) || product.getType() == ProductType.SERVICE)) {
      return;
    }

    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(productId, warehouseId)
        .orElseGet(() -> inventoryRepository.save(createInitialInventory(productId, warehouseId)));

    int before = inv.getQuantity();
    int after = Math.max(0, before - quantity);
    inv.setQuantity(after);
    inventoryRepository.save(inv);

    saveMovementDetail(inv, quantity, before, after, MovementType.SALIDA_VENTA, reference);
  }

  public void saveMovementDetail(Inventory inv, InventoryMovementRequest request,
      int before, int after, MovementType tipo) {
    saveMovementDetail(inv, request.getQuantity(), before, after, tipo, request.getReference());
  }

  public void saveMovementDetail(Inventory inv, int quantity, int before, int after, MovementType tipo,
      String reference) {
    Long userId = 1L;
    try {
      UserBusinessContext context = warehouseService.getUserBusinessContext();
      if (context != null && context.user() != null) {
        userId = context.user().getId();
      }
    } catch (Exception e) {
      // Cliente público
    }

    InventoryMovement detail = InventoryMovement.builder()
        .product(inv.getProduct())
        .warehouseId(inv.getWarehouseId())
        .movementType(tipo)
        .quantity(quantity)
        .previousStock(before)
        .currentStock(after)
        .reference(reference)
        .userId(userId)
        .build();

    inventoryMovementRepository.save(detail);
  }

  @Transactional
  public void reserveStock(Long productId, Long warehouseId, int quantity) {
    if (productId == null) return;

    Product product = productRepository.findById(productId).orElse(null);
    if (product != null && (Boolean.TRUE.equals(product.getIsService()) || product.getType() == ProductType.SERVICE)) {
      return;
    }

    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(productId, warehouseId)
        .orElseGet(() -> inventoryRepository.save(createInitialInventory(productId, warehouseId)));

    inv.setQuantityReserved(inv.getQuantityReserved() + quantity);
    inventoryRepository.save(inv);
  }

  @Transactional
  public void releaseReservedStock(Long productId, Long warehouseId, int quantity) {
    if (productId == null) return;

    Product product = productRepository.findById(productId).orElse(null);
    if (product != null && (Boolean.TRUE.equals(product.getIsService()) || product.getType() == ProductType.SERVICE)) {
      return;
    }

    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(productId, warehouseId)
        .orElse(null);

    if (inv != null) {
      inv.setQuantityReserved(Math.max(0, inv.getQuantityReserved() - quantity));
      inventoryRepository.save(inv);
    }
  }

  @Transactional
  public void confirmReservedStock(Long productId, Long warehouseId, int quantity, String reference) {
    if (productId == null) return;

    Product product = productRepository.findById(productId).orElse(null);
    if (product != null && (Boolean.TRUE.equals(product.getIsService()) || product.getType() == ProductType.SERVICE)) {
      return;
    }

    Inventory inv = inventoryRepository
        .findByProductIdAndWarehouseId(productId, warehouseId)
        .orElseGet(() -> inventoryRepository.save(createInitialInventory(productId, warehouseId)));

    inv.setQuantityReserved(Math.max(0, inv.getQuantityReserved() - quantity));

    int before = inv.getQuantity();
    int after = Math.max(0, before - quantity);
    inv.setQuantity(after);

    inventoryRepository.save(inv);

    saveMovementDetail(inv, quantity, before, after, MovementType.SALIDA_VENTA, reference);
  }
}
