package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import com.wralonzo.detail_shop.modules.inventory.application.invoice.InvoiceService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.invoice.InvoiceRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.invoice.InvoiceResponse;
import com.wralonzo.detail_shop.modules.inventory.domain.jpa.entities.Invoice;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoice(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.getInvoice(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> listInvoices(
            @PageableDefault(size = 20, sort = "invoiceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<InvoiceResponse> response = invoiceService.listInvoices(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Page<InvoiceResponse>> listBySupplier(
            @PathVariable Long supplierId,
            @PageableDefault(size = 20, sort = "invoiceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<InvoiceResponse> response = invoiceService.listBySupplier(supplierId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<InvoiceResponse>> listByStatus(
            @PathVariable Invoice.InvoiceStatus status,
            @PageableDefault(size = 20, sort = "invoiceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<InvoiceResponse> response = invoiceService.listByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<InvoiceResponse>> listPending() {
        List<InvoiceResponse> response = invoiceService.listPendingInvoices();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<InvoiceResponse> markAsPaid(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelInvoice(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Factura cancelada exitosamente",
                "invoice", response));
    }
}
