package com.wralonzo.detail_shop.modules.inventory.application.workorder;

import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.workorder.*;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Product;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.WorkOrder;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.WorkOrderDetails;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.ProductRepository;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.WorkOrderRepository;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;
import com.wralonzo.detail_shop.modules.organization.domain.records.UserBusinessContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final ProductRepository productRepository;
    private final WarehouseService warehouseService;

    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> getAll(Pageable pageable) {
        return workOrderRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getById(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .filter(wo -> wo.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada"));
        return mapToResponse(workOrder);
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderRequest request) {
        UserBusinessContext context = warehouseService.getUserBusinessContext();

        WorkOrder workOrder = WorkOrder.builder()
                .referenceNumber("WO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .name(request.getName())
                .prefix("WO")
                .description(request.getDescription())
                .userId(context.user().getId())
                .clientId(request.getClientId())
                .warehouseId(request.getWarehouseId())
                .status(request.getStatus() != null ? request.getStatus() : WorkOrder.WorkOrderStatus.PENDING)
                .notes(request.getNotes())
                .createdBy(context.user().getId())
                .details(new ArrayList<>())
                .build();

        if (request.getDetails() != null) {
            for (WorkOrderDetailRequest detailDto : request.getDetails()) {
                Product product = productRepository.findById(detailDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producto no encontrado ID: " + detailDto.getProductId()));

                WorkOrderDetails detail = WorkOrderDetails.builder()
                        .workOrder(workOrder)
                        .product(product)
                        .quantity(BigDecimal.valueOf(detailDto.getQuantity()))
                        .price(product.getBasePrice()) // Simplificación
                        .discount(BigDecimal.ZERO)
                        .taxes(BigDecimal.ZERO)
                        .total(product.getBasePrice().multiply(BigDecimal.valueOf(detailDto.getQuantity())))
                        .build();

                workOrder.getDetails().add(detail);
            }
        }

        return mapToResponse(workOrderRepository.save(workOrder));
    }

    @Transactional
    public WorkOrderResponse update(Long id, WorkOrderRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada"));

        UserBusinessContext context = warehouseService.getUserBusinessContext();

        workOrder.setName(request.getName());
        workOrder.setDescription(request.getDescription());
        workOrder.setClientId(request.getClientId());
        workOrder.setWarehouseId(request.getWarehouseId());
        if (request.getStatus() != null)
            workOrder.setStatus(request.getStatus());
        workOrder.setNotes(request.getNotes());
        workOrder.setUpdatedBy(context.user().getId());

        // Manejo simple de detalles (reemplazar todos)
        if (request.getDetails() != null) {
            workOrder.getDetails().clear();
            for (WorkOrderDetailRequest detailDto : request.getDetails()) {
                Product product = productRepository.findById(detailDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Producto no encontrado ID: " + detailDto.getProductId()));

                WorkOrderDetails detail = WorkOrderDetails.builder()
                        .workOrder(workOrder)
                        .product(product)
                        .quantity(BigDecimal.valueOf(detailDto.getQuantity()))
                        .price(product.getBasePrice())
                        .discount(BigDecimal.ZERO)
                        .taxes(BigDecimal.ZERO)
                        .total(product.getBasePrice().multiply(BigDecimal.valueOf(detailDto.getQuantity())))
                        .build();

                workOrder.getDetails().add(detail);
            }
        }

        return mapToResponse(workOrderRepository.save(workOrder));
    }

    @Transactional
    public void delete(Long id) {
        WorkOrder workOrder = workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de trabajo no encontrada"));
        workOrder.setDeletedAt(LocalDateTime.now());
        workOrderRepository.save(workOrder);
    }

    private WorkOrderResponse mapToResponse(WorkOrder wo) {
        List<WorkOrderDetailResponse> detailResponses = wo.getDetails() != null ? wo.getDetails().stream()
                .map(d -> WorkOrderDetailResponse.builder()
                        .id(d.getId())
                        .productId(d.getProduct().getId())
                        .productName(d.getProduct().getName())
                        .quantity(d.getQuantity().intValue())
                        .build())
                .toList() : new ArrayList<>();

        return WorkOrderResponse.builder()
                .id(wo.getId())
                .referenceNumber(wo.getReferenceNumber())
                .name(wo.getName())
                .description(wo.getDescription())
                .userId(wo.getUserId())
                .clientId(wo.getClientId())
                .warehouseId(wo.getWarehouseId())
                .status(wo.getStatus())
                .notes(wo.getNotes())
                .details(detailResponses)
                .createdAt(wo.getCreatedAt())
                .updatedAt(wo.getUpdatedAt())
                .build();
    }
}
