package com.wralonzo.detail_shop.modules.inventory.infraestructure;

import com.wralonzo.detail_shop.modules.inventory.application.workorder.WorkOrderService;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.workorder.WorkOrderRequest;
import com.wralonzo.detail_shop.modules.inventory.domain.dtos.workorder.WorkOrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public ResponseEntity<Page<WorkOrderResponse>> getAll(Pageable pageable) {
        return ResponseEntity.ok(workOrderService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workOrderService.getById(id));
    }

    @PostMapping
    public ResponseEntity<WorkOrderResponse> create(@Valid @RequestBody WorkOrderRequest request) {
        return new ResponseEntity<>(workOrderService.create(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> update(@PathVariable Long id,
            @Valid @RequestBody WorkOrderRequest request) {
        return ResponseEntity.ok(workOrderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workOrderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
