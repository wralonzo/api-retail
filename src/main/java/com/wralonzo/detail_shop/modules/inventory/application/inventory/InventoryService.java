package com.wralonzo.detail_shop.modules.inventory.application.inventory;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryLoadBulkRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryLoadRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryLoadResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.InventoryMovementRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.inventory.StockResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.MovementType;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.StockStatus;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.*;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryBatchRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductBranchConfigRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Branch;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.entities.Warehouse;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.BranchRepository;
import com.wralonzo.detail_shop.modules.organization.domain.jpa.repositories.WarehouseRepository;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementService inventoryMovementService;
    private final ProductRepository productRepository;
    private final InventoryBatchRepository batchRepository;
    private final WarehouseService warehouseService;
    private final BranchRepository branchRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductBranchConfigRepository productBranchConfigRepository;

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

    @Transactional
    public void initializeBranchInventory(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada con ID: " + branchId));

        Long companyId = branch.getCompany().getId();
        List<Warehouse> warehouses = warehouseRepository.findAllByBranchId(branchId);
        List<Product> products = productRepository.findAllByCompanyId(companyId);

        for (Product product : products) {
            // 1. Asegurar ProductBranchConfig
            if (!productBranchConfigRepository.existsByProductIdAndBranchId(product.getId(), branchId)) {
                ProductBranchConfig config = ProductBranchConfig.builder()
                        .product(product)
                        .branchId(branchId)
                        .active(true)
                        .stockMinim(5)
                        .build();
                productBranchConfigRepository.save(config);
            }

            // 2. Asegurar Inventory para cada Warehouse
            for (Warehouse warehouse : warehouses) {
                if (!inventoryRepository.existsByProductIdAndWarehouseId(product.getId(), warehouse.getId())) {
                    Inventory inventory = inventoryMovementService.createInitialInventory(product.getId(),
                            warehouse.getId());
                    inventoryRepository.save(inventory);
                }
            }
        }
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

    // --- INVENTORY LOADING METHODS ---

    @Transactional
    public InventoryLoadResponse loadInventory(
            InventoryLoadRequest request) {

        validateWarehouseAccess(request.getWarehouseId());

        try {
            // If has batch information, use the existing batch method
            if (request.getBatchNumber() != null && !request.getBatchNumber().isBlank()) {
                receiveStockWithBatch(
                        request.getProductId(),
                        request.getSupplierId(),
                        request.getQuantity(),
                        request.getBatchNumber(),
                        request.getExpirationDate(),
                        request.getWarehouseId());
            } else {
                // Direct inventory update without batch
                loadInventoryDirect(request);
            }

            return InventoryLoadResponse.builder()
                    .success(true)
                    .message("Inventario cargado exitosamente")
                    .itemsProcessed(1)
                    .build();

        } catch (Exception e) {
            return InventoryLoadResponse.builder()
                    .success(false)
                    .message("Error al cargar inventario: " + e.getMessage())
                    .itemsProcessed(0)
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    @Transactional
    public InventoryLoadResponse loadInventoryBulk(
            InventoryLoadBulkRequest bulkRequest) {

        InventoryLoadResponse response = InventoryLoadResponse
                .builder()
                .success(true)
                .itemsProcessed(0)
                .build();

        int processedCount = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < bulkRequest.getItems().size(); i++) {
            InventoryLoadRequest item = bulkRequest
                    .getItems().get(i);

            // Apply default warehouse if not specified
            if (item.getWarehouseId() == null && bulkRequest.getWarehouseId() != null) {
                item.setWarehouseId(bulkRequest.getWarehouseId());
            }

            try {
                if (item.getBatchNumber() != null && !item.getBatchNumber().isBlank()) {
                    receiveStockWithBatch(
                            item.getProductId(),
                            item.getSupplierId(),
                            item.getQuantity(),
                            item.getBatchNumber(),
                            item.getExpirationDate(),
                            item.getWarehouseId());
                } else {
                    loadInventoryDirect(item);
                }
                processedCount++;
            } catch (Exception e) {
                errors.add("Item " + (i + 1) + ": " + e.getMessage());
            }
        }

        response.setItemsProcessed(processedCount);
        response.setErrors(errors);
        response.setMessage(processedCount + " de " + bulkRequest.getItems().size() + " items procesados");
        response.setSuccess(errors.isEmpty());

        return response;
    }

    @Transactional
    public InventoryLoadResponse loadInventoryFromExcel(
            MultipartFile file, Long defaultWarehouseId) {

        InventoryLoadResponse response = InventoryLoadResponse
                .builder()
                .success(false)
                .itemsProcessed(0)
                .build();

        try {
            // Parse Excel file using Apache POI
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            List<InventoryLoadRequest> items = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Skip header row, start from row 1
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowIndex);
                if (row == null)
                    continue;

                try {
                    InventoryLoadRequest item = parseRowToInventoryLoadRequest(
                            row, defaultWarehouseId);
                    if (item != null) {
                        items.add(item);
                    }
                } catch (Exception e) {
                    errors.add("Fila " + (rowIndex + 1) + ": " + e.getMessage());
                }
            }

            workbook.close();

            // Process items using bulk method
            if (!items.isEmpty()) {
                InventoryLoadBulkRequest bulkRequest = InventoryLoadBulkRequest
                        .builder()
                        .items(items)
                        .warehouseId(defaultWarehouseId)
                        .build();

                response = loadInventoryBulk(bulkRequest);
                response.getErrors().addAll(0, errors); // Add parsing errors at the beginning
            } else {
                response.setMessage("No se encontraron items válidos en el archivo Excel");
                response.setErrors(errors);
            }

        } catch (Exception e) {
            response.setMessage("Error al procesar archivo Excel: " + e.getMessage());
            response.getErrors().add(e.getMessage());
        }

        return response;
    }

    // --- PRIVATE HELPER METHODS ---

    private void loadInventoryDirect(
            InventoryLoadRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceConflictException(
                        "Producto no encontrado con ID: " + request.getProductId()));

        // Get or create inventory
        Inventory inventory = inventoryRepository
                .findByProductIdAndWarehouseId(request.getProductId(), request.getWarehouseId())
                .orElseGet(() -> inventoryRepository
                        .save(inventoryMovementService.createInitialInventory(request.getProductId(),
                                request.getWarehouseId())));

        int quantityBefore = inventory.getQuantity();
        int quantityNew = quantityBefore + request.getQuantity();

        inventory.setQuantity(quantityNew);
        inventoryRepository.save(inventory);

        // Create movement request for Kardex
        InventoryMovementRequest movementRequest = new InventoryMovementRequest();
        movementRequest.setProductId(request.getProductId());
        movementRequest.setWarehouseId(request.getWarehouseId());
        movementRequest.setQuantity(request.getQuantity());
        movementRequest.setReference("Carga de inventario");
        movementRequest.setNotes(request.getNotes() != null ? request.getNotes() : "Carga manual de inventario");

        // Record in Kardex
        inventoryMovementService.saveMovementDetail(inventory, movementRequest, quantityBefore, quantityNew,
                MovementType.ENTRADA_COMPRA);
    }

    private InventoryLoadRequest parseRowToInventoryLoadRequest(
            Row row, Long defaultWarehouseId) {

        // Expected columns: SKU | Product Name | Warehouse ID | Quantity | Supplier ID
        // | Batch | Exp Date | Notes

        org.apache.poi.ss.usermodel.Cell skuCell = row.getCell(0);
        org.apache.poi.ss.usermodel.Cell warehouseCell = row.getCell(2);
        org.apache.poi.ss.usermodel.Cell quantityCell = row.getCell(3);

        if (skuCell == null || quantityCell == null) {
            return null; // Skip empty rows
        }

        String sku = getCellValueAsString(skuCell);
        if (sku == null || sku.isBlank()) {
            return null;
        }

        // Find product by SKU
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceConflictException("Producto no encontrado con SKU: " + sku));

        Long warehouseId = defaultWarehouseId;
        if (warehouseCell != null) {
            warehouseId = (long) getCellValueAsNumeric(warehouseCell);
        }

        Integer quantity = (int) getCellValueAsNumeric(quantityCell);

        InventoryLoadRequest request = InventoryLoadRequest
                .builder()
                .productId(product.getId())
                .warehouseId(warehouseId)
                .quantity(quantity)
                .build();

        // Optional fields
        org.apache.poi.ss.usermodel.Cell supplierCell = row.getCell(4);
        if (supplierCell != null) {
            try {
                request.setSupplierId((long) getCellValueAsNumeric(supplierCell));
            } catch (Exception ignored) {
            }
        }

        org.apache.poi.ss.usermodel.Cell batchCell = row.getCell(5);
        if (batchCell != null) {
            request.setBatchNumber(getCellValueAsString(batchCell));
        }

        org.apache.poi.ss.usermodel.Cell notesCell = row.getCell(7);
        if (notesCell != null) {
            request.setNotes(getCellValueAsString(notesCell));
        }

        return request;
    }

    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null)
            return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private double getCellValueAsNumeric(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null)
            return 0;

        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    yield 0;
                }
            }
            default -> 0;
        };
    }
}