package com.wralonzo.detail_shop.modules.inventory.application.invoice;

import com.wralonzo.detail_shop.configuration.exception.ResourceConflictException;
import com.wralonzo.detail_shop.configuration.exception.ResourceNotFoundException;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.invoice.*;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.*;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.repositories.*;
import com.wralonzo.detail_shop.modules.organization.application.WarehouseService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseService warehouseService;

    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        // Validar que el número de factura no existe
        if (invoiceRepository.findByInvoiceNumber(request.getInvoiceNumber()).isPresent()) {
            throw new ResourceConflictException("Ya existe una factura con el número: " + request.getInvoiceNumber());
        }

        // Validar proveedor
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        // Validar almacén
        warehouseService.getById(request.getWarehouseId());

        // Crear factura
        Invoice invoice = Invoice.builder()
                .invoiceNumber(request.getInvoiceNumber())
                .supplierId(request.getSupplierId())
                .warehouseId(request.getWarehouseId())
                .branchId(request.getBranchId())
                .invoiceDate(request.getInvoiceDate())
                .dueDate(request.getDueDate())
                .status(Invoice.InvoiceStatus.PENDING)
                .notes(request.getNotes())
                .subtotal(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();

        // Agregar items
        for (InvoiceItemRequest itemRequest : request.getItems()) {
            // Validar producto
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado: " + itemRequest.getProductId()));

            InvoiceItem item = InvoiceItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .tax(itemRequest.getTax())
                    .batchNumber(itemRequest.getBatchNumber())
                    .build();

            item.calculateTotals();
            invoice.addItem(item);
        }

        // Calcular totales de la factura
        invoice.calculateTotals();

        // Guardar
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return buildInvoiceResponse(savedInvoice);
    }

    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest update) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));

        // No permitir actualizar facturas pagadas o canceladas
        if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING) {
            throw new ResourceConflictException("No se puede actualizar una factura " + invoice.getStatus());
        }

        // Validar número de factura si cambió
        if (!invoice.getInvoiceNumber().equals(update.getInvoiceNumber())) {
            if (invoiceRepository.findByInvoiceNumber(update.getInvoiceNumber()).isPresent()) {
                throw new ResourceConflictException(
                        "Ya existe una factura con el número: " + update.getInvoiceNumber());
            }
            invoice.setInvoiceNumber(update.getInvoiceNumber());
        }

        // Actualizar campos básicos
        invoice.setInvoiceDate(update.getInvoiceDate());
        invoice.setDueDate(update.getDueDate());
        invoice.setNotes(update.getNotes());

        // Limpiar items existentes y agregar nuevos
        invoice.getItems().clear();

        for (InvoiceItemRequest itemRequest : update.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado: " + itemRequest.getProductId()));

            InvoiceItem item = InvoiceItem.builder()
                    .productId(itemRequest.getProductId())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(itemRequest.getUnitPrice())
                    .tax(itemRequest.getTax())
                    .batchNumber(itemRequest.getBatchNumber())
                    .build();

            item.calculateTotals();
            invoice.addItem(item);
        }

        // Recalcular totales
        invoice.calculateTotals();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        return buildInvoiceResponse(savedInvoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));
        return buildInvoiceResponse(invoice);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable)
                .map(this::buildInvoiceResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listBySupplier(Long supplierId, Pageable pageable) {
        return invoiceRepository.findBySupplierId(supplierId, pageable)
                .map(this::buildInvoiceResponse);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> listByStatus(Invoice.InvoiceStatus status, Pageable pageable) {
        return invoiceRepository.findByStatus(status, pageable)
                .map(this::buildInvoiceResponse);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listPendingInvoices() {
        return invoiceRepository.findByStatus(Invoice.InvoiceStatus.PENDING, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::buildInvoiceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InvoiceResponse markAsPaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));

        if (invoice.getStatus() != Invoice.InvoiceStatus.PENDING) {
            throw new ResourceConflictException("La factura no está pendiente");
        }

        invoice.setStatus(Invoice.InvoiceStatus.PAID);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return buildInvoiceResponse(savedInvoice);
    }

    @Transactional
    public InvoiceResponse cancelInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura no encontrada"));

        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new ResourceConflictException("No se puede cancelar una factura pagada");
        }

        invoice.setStatus(Invoice.InvoiceStatus.CANCELLED);
        Invoice savedInvoice = invoiceRepository.save(invoice);

        return buildInvoiceResponse(savedInvoice);
    }

    // Helper method to build response
    private InvoiceResponse buildInvoiceResponse(Invoice invoice) {
        Supplier supplier = supplierRepository.findById(invoice.getSupplierId()).orElse(null);
        String supplierName = supplier != null ? supplier.getName() : "Desconocido";

        List<InvoiceItemResponse> itemResponses = invoice.getItems().stream()
                .map(this::buildInvoiceItemResponse)
                .collect(Collectors.toList());

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .supplierId(invoice.getSupplierId())
                .supplierName(supplierName)
                .warehouseId(invoice.getWarehouseId())
                .branchId(invoice.getBranchId())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .total(invoice.getTotal())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .items(itemResponses)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }

    private InvoiceItemResponse buildInvoiceItemResponse(InvoiceItem item) {
        Product product = productRepository.findById(item.getProductId()).orElse(null);
        String productName = product != null ? product.getName() : "Desconocido";
        String productSku = product != null ? product.getSku() : "";

        return InvoiceItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(productName)
                .productSku(productSku)
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subtotal(item.getSubtotal())
                .tax(item.getTax())
                .total(item.getTotal())
                .batchNumber(item.getBatchNumber())
                .build();
    }
}
