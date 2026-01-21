package com.wralonzo.detail_shop.modules.inventory.application.sale;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.entities.Client;
import com.wralonzo.detail_shop.modules.customers.domain.jpa.repositories.ClientRepository;
import com.wralonzo.detail_shop.modules.inventory.application.inventory.InventoryMovementService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleDetailRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleDetailResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.sale.SaleResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.enums.ProductType;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.*;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductBranchConfigRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.SaleDetailRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.SaleRepository;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleDetailRepository saleDetailRepository;
    private final ProductRepository productRepository;
    private final ProductBranchConfigRepository branchConfigRepository;
    private final ClientRepository clientRepository;
    private final InventoryMovementService inventoryMovementService;
    private final WarehouseService warehouseService;

    @Transactional
    public SaleResponse createSale(SaleRequest request) {
        UserBusinessContext context = warehouseService.getUserBusinessContext();

        // 1. Validar Cliente
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // 2. Crear Cabecera de Venta
        Sale sale = Sale.builder()
                .prefix("FAC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()) // Generar prefijo simple
                                                                                             // por ahora
                .userId(context.user().getId())
                .clientId(client.getId())
                .warehouseId(request.getWarehouseId())
                .tipoVenta(request.getType())
                .notes(request.getNotes())
                .subtotal(BigDecimal.ZERO)
                .taxes(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .state(Sale.Estado.COMPLETADA)
                .build();

        sale = saleRepository.save(sale);

        BigDecimal subtotal = BigDecimal.ZERO;
        List<SaleDetailResponse> detailsResponse = new ArrayList<>();

        // 3. Procesar Items
        for (SaleDetailRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto ID " + itemRequest.getProductId() + " no encontrado"));

            ProductUnit unit = product.getUnits().stream()
                    .filter(u -> u.getId().equals(itemRequest.getUnitId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Unidad ID " + itemRequest.getUnitId()
                            + " no válida para el producto " + product.getName()));

            // Buscar precio Configurado en la Sucursal
            ProductBranchConfig branchConfig = branchConfigRepository
                    .findByProductIdAndBranchId(product.getId(), request.getWarehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "El producto " + product.getName() + " no está habilitado en esta sucursal"));

            BigDecimal unitPrice = branchConfig.getPrices().stream()
                    .filter(p -> p.getUnit().getId().equals(unit.getId()))
                    .findFirst()
                    .map(ProductBranchPrice::getPrice)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Precio no configurado para " + product.getName() + " en unidad " + unit.getUnitName()));

            // Crear Detalle
            SaleDetail detail = SaleDetail.builder()
                    .sale(sale)
                    .product(product)
                    .unit(unit)
                    .quantity(itemRequest.getQuantity())
                    .priceUnit(unitPrice)
                    .discount(itemRequest.getDiscount() != null ? itemRequest.getDiscount() : BigDecimal.ZERO)
                    .build();

            detail.calcularSubtotal();
            saleDetailRepository.save(detail);
            subtotal = subtotal.add(detail.getSubtotal());

            // Agregar a respuesta
            detailsResponse.add(SaleDetailResponse.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitId(unit.getId())
                    .unitName(unit.getUnitName())
                    .quantity(detail.getQuantity())
                    .priceUnit(detail.getPriceUnit())
                    .discount(detail.getDiscount())
                    .subtotal(detail.getSubtotal())
                    .build());

            // 4. DESCUENTO DE INVENTARIO
            if (product.getType() == ProductType.BUNDLE) {
                // Descontar componentes
                for (ProductBundle component : product.getBundleItems()) {
                    int quantityToDeduct = itemRequest.getQuantity() * component.getQuantity(); // Asume 1 Combo = X
                                                                                                // items.
                    // TODO: Qué pasa si las unidades del combo son diferentes de la base?
                    // Actualmente ProductBundle solo guarda ID de producto y cantidad. Asumimos
                    // cantidad base.
                    inventoryMovementService.processSalesMovement(
                            component.getComponentProduct().getId(),
                            request.getWarehouseId(),
                            quantityToDeduct,
                            "Venta Combo #" + sale.getPrefix());
                }
            } else if (product.getType() == ProductType.STANDARD) {
                // Descontar Producto Standard
                BigDecimal conversionFactor = unit.getConversionFactor();
                int totalBaseUnits = conversionFactor.multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                        .intValue();

                inventoryMovementService.processSalesMovement(
                        product.getId(),
                        request.getWarehouseId(),
                        totalBaseUnits,
                        "Venta #" + sale.getPrefix());
            }
        }

        // 5. Actualizar Totales Sale
        sale.setSubtotal(subtotal);
        sale.setTotal(subtotal); // Sumar impuestos si aplica
        saleRepository.save(sale);

        // 6. Retornar Respuesta
        return SaleResponse.builder()
                .id(sale.getIdSale())
                .prefix(sale.getPrefix())
                .clientId(sale.getClientId())
                // .clientName(client.getName() + " " + client.getLastName())
                .warehouseId(sale.getWarehouseId())
                .saleDate(sale.getDateSale())
                .type(sale.getTipoVenta())
                .state(sale.getState())
                .subtotal(sale.getSubtotal())
                .total(sale.getTotal())
                .details(detailsResponse)
                .build();
    }
}
