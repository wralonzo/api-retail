package com.wralonzo.detail_shop.modules.inventory.application.inventory;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryMovementRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.StockResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.MovementType;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.StockStatus;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Inventory;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.InventoryBatch;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Supplier;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryBatchRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementService inventoryMovementService;
    private final ProductRepository productRepository;
    private final InventoryBatchRepository batchRepository;
    private final WarehouseService warehouseService;

    // --- 1. CONSULTA DE KARDEX (HISTORIAL DETALLADO) ---

    @Transactional(readOnly = true)
    public StockResponse getCurrentStock(Long productId, Long warehouseId) {
        validateWarehouseAccess(warehouseId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceConflictException("Producto no encontrado"));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElse(null);

        if (inv == null) {
            return buildEmptyStockResponse(product);
        }

        // StockStatus status = determineStockStatus(inv.getQuantity(),
        // product.getStockMinim());

        return StockResponse.builder()
                .productName(product.getName())
                .sku(product.getSku())
                .currentStock(inv.getQuantity())
                .quantityReserved(inv.getQuantityReserved())
                .quantityAvailable(inv.getQuantityFree())
                .build();
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getLowStockAlerts() {
        UserBusinessContext context = warehouseService.getUserBusinessContext();
        List<Inventory> lowStocks = inventoryRepository.findLowStockByWarehouseIds(context.warehouseIds());

        return lowStocks.stream()
                .map(inv -> StockResponse.builder()
                        .productName(inv.getProduct().getName())
                        .sku(inv.getProduct().getSku())
                        .currentStock(inv.getQuantity())
                        .status(StockStatus.BAJO_STOCK)
                        .build())
                .toList();
    }

    @Transactional
    public void receiveStockWithBatch(Long productId, Long supplierId, Integer qty,
            String batchNo, LocalDateTime expDate, Long warehouseIdParam) {

        UserBusinessContext context = warehouseService.getUserBusinessContext();
        Long targetWarehouseId = determineTargetWarehouse(context, warehouseIdParam);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceConflictException("Producto no encontrado"));

        // 1. Registro del Lote (Trazabilidad específica)
        InventoryBatch batch = InventoryBatch.builder()
                .product(product)
                .supplier(Supplier.builder().id(supplierId).build())
                .warehouseId(targetWarehouseId)
                .batchNumber(batchNo)
                .expirationDate(expDate)
                .build();
        batchRepository.save(batch);

        // 2. Actualización de Saldo Maestro
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(productId, targetWarehouseId)
                .orElseGet(() -> inventoryRepository
                        .save(inventoryMovementService.createInitialInventory(productId, targetWarehouseId)));

        int quantityBefore = inventory.getQuantity();
        int quantityNew = quantityBefore + qty;

        inventory.setQuantity(quantityNew);
        inventoryRepository.save(inventory);

        // 3. Crear el Request para el Kardex (Ajuste para que coincida con tu nuevo
        // saveMovementDetail)
        InventoryMovementRequest historyRequest = new InventoryMovementRequest();
        historyRequest.setProductId(productId);
        historyRequest.setWarehouseId(targetWarehouseId);
        historyRequest.setQuantity(qty);
        historyRequest.setReference("Lote: " + batchNo);
        historyRequest.setNotes("Ingreso por proveedor a través de carga de lotes.");

        // 4. Auditoría en Kardex usando la nueva firma del método
        inventoryMovementService.saveMovementDetail(inventory, historyRequest, quantityBefore, quantityNew,
                MovementType.ENTRADA_COMPRA);
    }

    // --- MÉTODOS AUXILIARES Y PRIVADOS ---
    private StockResponse buildEmptyStockResponse(Product product) {
        return StockResponse.builder()
                .productName(product.getName())
                .sku(product.getSku())
                .currentStock(0)
                .quantityReserved(0)
                .quantityAvailable(0)
                .status(StockStatus.SIN_STOCK)
                .build();
    }

    private Long determineTargetWarehouse(UserBusinessContext context, Long requestedId) {
        if (context.isSuperAdmin()) {
            return (requestedId != null) ? requestedId : context.warehouseIds().get(0);
        }
        Long userWhId = context.user().getEmployee().getWarehouseId();
        if (requestedId != null && !requestedId.equals(userWhId)) {
            throw new ResourceConflictException("No tiene permisos para operar en este almacén.");
        }
        return userWhId;
    }

    private void validateWarehouseAccess(Long warehouseId) {
        UserBusinessContext context = warehouseService.getUserBusinessContext();
        if (context.isSuperAdmin())
            return;

        if (!context.warehouseIds().contains(warehouseId)) {
            throw new ResourceConflictException(
                    "Acceso denegado: Usted no tiene permisos para gestionar el almacén con ID " + warehouseId);
        }
    }
}