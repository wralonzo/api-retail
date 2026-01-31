package com.wralonzo.detail_shop.modules.inventory.application.product;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.PurchaseOrderReceptionRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.product.ReceptionItemDTO;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.InventoryBatch;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductBranchConfig;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Supplier;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.ProductBranchPrice;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.InventoryBatchRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductBranchConfigRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.SupplierRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProcessReceptionService {
  private final InventoryBatchRepository batchRepository;
  private final ProductBranchConfigRepository branchConfigRepository;
  private final ProductRepository productRepository;
  private final SupplierRepository supplierRepository;

  @Transactional
  public void processReception(PurchaseOrderReceptionRequest request) {
    // 1. Validar Proveedor
    Supplier supplier = supplierRepository.findById(request.getSupplierId())
        .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

    for (ReceptionItemDTO item : request.getItems()) {
      // 2. Validar que el producto existe
      Product product = productRepository.findById(item.getProductId())
          .orElseThrow(() -> new ResourceNotFoundException("Producto ID " + item.getProductId() + " no encontrado"));

      // 3. SEGURIDAD: Validar que el producto tenga configuración en esta sucursal
      // Si no existe, podrías crear una por defecto con el basePrice del maestro
      ensureBranchConfigExists(product, request.getBranchId());

      // 4. CREAR EL LOTE (InventoryBatch)
      InventoryBatch newBatch = InventoryBatch.builder()
          .product(product)
          .supplier(supplier)
          .warehouseId(request.getWarehouseId())
          .batchNumber(item.getBatchNumber())
          .initialQuantity(item.getQuantity())
          .currentQuantity(item.getQuantity())
          .costPrice(item.getCostPrice())
          .expirationDate(item.getExpirationDate())
          .build();

      batchRepository.save(newBatch);

      // 5. Opcional: Actualizar el 'pricePurchase' sugerido en el maestro
      // para que la próxima orden de compra tenga el último costo registrado.
      product.setPricePurchase(item.getCostPrice());
      productRepository.save(product);
    }
  }

  private void ensureBranchConfigExists(Product product, Long branchId) {
    boolean exists = branchConfigRepository.existsByProductIdAndBranchId(product.getId(), branchId);
    if (!exists) {
      // Crear configuración automática
      ProductBranchConfig config = ProductBranchConfig.builder()
          .product(product)
          .branchId(branchId)
          .active(true)
          .stockMinim(5) // Valor por defecto corporativo
          .build();

      // Intentar establecer precio para la unidad base si existe
      product.getUnits().stream()
          .filter(u -> u.getUnitProduct().getId().equals(1L))
          .findFirst()
          .ifPresent(baseUnit -> {
            ProductBranchPrice price = ProductBranchPrice.builder()
                .branchConfig(config)
                .unit(baseUnit.getUnitProduct())
                .price(product.getBasePrice())
                .active(true)
                .build();
            config.getPrices().add(price);
          });

      branchConfigRepository.save(config);
    }
  }
}
