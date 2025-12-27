package com.wralonzo.detail_shop.application.services;

import com.wralonzo.detail_shop.application.repositories.*;
import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.domain.dto.inventory.BulkInventoryRequest;
import com.wralonzo.detail_shop.domain.dto.inventory.InventoryMovementRequest;
import com.wralonzo.detail_shop.domain.dto.inventory.KardexResponse;
import com.wralonzo.detail_shop.domain.dto.inventory.StockResponse;
import com.wralonzo.detail_shop.domain.entities.Inventory;
import com.wralonzo.detail_shop.domain.entities.InventoryDetail;
import com.wralonzo.detail_shop.domain.entities.Product;
import com.wralonzo.detail_shop.domain.entities.Warehouse;
import com.wralonzo.detail_shop.domain.enums.StockStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryDetailRepository detailRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final InventoryDetailRepository inventoryDetailRepository;

    // --- 1. PROCESAR MOVIMIENTO (ENTRADAS, SALIDAS, AJUSTES)
    @Transactional
    public void processMovement(InventoryMovementRequest request) {
        // Buscar el registro de inventario (saldo actual)
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElseGet(() -> createInitialInventory(request.getProductId(), request.getWarehouseId()));

        int quantityBefore = inventory.getQuantity();
        int quantityNew = 0;
        InventoryDetail.TipoMovimiento tipo = InventoryDetail.TipoMovimiento.valueOf(request.getType());

        // Lógica según el tipo de movimiento
        switch (tipo) {
            case ENTRADA, TRASLADO_ENTRADA, DEVOLUCION -> quantityNew = quantityBefore + request.getQuantity();

            case SALIDA, TRASLADO_SALIDA -> {
                quantityNew = quantityBefore - request.getQuantity();
                if (quantityNew < 0) throw new ResourceConflictException("Stock insuficiente");
            }

            case AJUSTE ->
                // En un ajuste, la cantidad del request es el valor REAL contado físicamente
                    quantityNew = request.getQuantity();
        }

        // Actualizar saldo en la tabla Inventories
        inventory.setQuantity(quantityNew);
        inventoryRepository.save(inventory);

        // Registrar en el historial (InventoryDetail)
        saveMovementDetail(inventory, request, tipo, quantityBefore, quantityNew);
    }

    // --- 2. CONSULTAR STOCK ACTUAL (POR PRODUCTO Y SUCURSAL)
    @Transactional(readOnly = true)
    public StockResponse getCurrentStock(Long productId, Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new ResourceConflictException("El almacén con ID " + warehouseId + " no existe en el sistema.");
        }

        StockStatus status = StockStatus.NO_INICIALIZADO;

        // 2. Validar si el Producto existe físicamente
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceConflictException("El producto con ID " + productId + " no existe."));

        Inventory inv = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
                .orElse(null);

        // 4. Si no hay registro de inventario, significa que el stock es 0 (pero el almacén sí existe)
        if (inv == null) {
            return StockResponse.builder()
                    .productName(product.getName())
                    .sku(product.getSku())
                    .currentStock(0)
                    .stockMinim(product.getStockMinim())
                    .quantityReserved(0)
                    .quantityAvailable(0)
                    .status(status)
                    .build();
        }

        // 5. Lógica normal si el registro sí existe
        // Dentro de getCurrentStock en InventoryService
        if (inv.getQuantity() <= 0) {
            status = StockStatus.SIN_STOCK;
        } else if (inv.getQuantity() <= product.getStockMinim()) {
            status = StockStatus.BAJO_STOCK;
        } else {
            status = StockStatus.OK;
        }
        return StockResponse.builder()
                .productName(product.getName())
                .sku(product.getSku())
                .warehouseName(inv.getWarehouse().getName())
                .currentStock(inv.getQuantity())
                .stockMinim(product.getStockMinim())
                .quantityReserved(inv.getQuantityReserved())
                .quantityAvailable(inv.getQuantityFree())
                .status(status)
                .build();
    }

    // --- 3. ALERTAS DE STOCK BAJO
    @Transactional(readOnly = true)
    public List<StockResponse> getLowStockAlerts(Long warehouseId) {
        // Este query lo definiremos en el repositorio
        return inventoryRepository.findLowStockByWarehouse(warehouseId).stream()
                .map(inv -> getCurrentStock(inv.getProduct().getId(), warehouseId))
                .toList();
    }

    // --- HELPERS
    private Inventory createInitialInventory(Long productId, Long warehouseId) {
        return Inventory.builder()
                .product(productRepository.getReferenceById(productId))
                .warehouse(warehouseRepository.getReferenceById(warehouseId))
                .quantity(0)
                .quantityReserved(0)
                .build();
    }

    private void saveMovementDetail(Inventory inv, InventoryMovementRequest req,
                                    InventoryDetail.TipoMovimiento tipo, int before, int after) {
        InventoryDetail detail = InventoryDetail.builder()
                .product(inv.getProduct())
                .warehouse(inv.getWarehouse())
                .type(tipo)
                .quantity(req.getQuantity())
                .quantityAfter(before)
                .quantityNew(after)
                .reference(req.getReference())
                .notes(req.getNotes())
                .user(userRepository.getReferenceById(req.getUserId()))
                .build();
        detailRepository.save(detail);
    }

    @Transactional
    public Map<String, Object> bulkInitialize(BulkInventoryRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceConflictException("Almacén no encontrado"));

        // 1. Obtener los productos a inicializar
        List<Product> productsToInit;
        if (request.getCategoryId() != null) {
            productsToInit = productRepository.findByCategoryIdAndActiveTrue(request.getCategoryId());
        } else {
            productsToInit = productRepository.findByActiveTrue();
        }

        int createdCount = 0;
        int skippedCount = 0;

        // 2. Procesar cada producto
        for (Product product : productsToInit) {
            // Verificar si ya existe el registro para no duplicar
            boolean exists = inventoryRepository.existsByProductIdAndWarehouseId(product.getId(), warehouse.getId());

            if (!exists) {
                Inventory newInventory = Inventory.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity(0)
                        .quantityReserved(0)
                        .build();
                inventoryRepository.save(newInventory);
                createdCount++;
            } else {
                skippedCount++;
            }
        }

        // 3. Devolver un resumen del proceso
        Map<String, Object> stats = new HashMap<>();
        stats.put("warehouse", warehouse.getName());
        stats.put("totalProcessed", productsToInit.size());
        stats.put("recordsCreated", createdCount);
        stats.put("recordsSkipped", skippedCount);
        stats.put("message", "Inicialización masiva completada");

        return stats;
    }

    public List<KardexResponse> getKardex(Long productId, Long warehouseId, LocalDate startDate, LocalDate endDate) {
        // Convertir LocalDate a LocalDateTime (inicio del día y fin del día)
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<InventoryDetail> movements = inventoryDetailRepository
                .findByProductIdAndWarehouseIdAndCreatedAtBetweenOrderByCreatedAtDesc(productId, warehouseId, start, end);

        return movements.stream()
                .map(m -> KardexResponse.builder()
                        .date(m.getCreatedAt())
                        .type(m.getType().toString())
                        .quantity(m.getQuantity())
                        .reference(m.getReference())
                        .notes(m.getNotes())
                        .username(m.getUser().getUsername())
                        .build())
                .collect(Collectors.toList());
    }
}